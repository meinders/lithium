/*
 * Copyright 2013 Gerrit Meinders
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lithium.search;

import java.util.*;
import java.util.regex.*;

import lithium.*;
import lithium.catalog.*;

/**
 * <p>
 * A search query that searches for words in the contents of a lyric and
 * calculates relevance scores. The exact phrase search method behaves exactly
 * as it would using a {@link ContentSearchQuery}.
 *
 * <p>
 * Lyrics matched by this kind of query should be registered with the
 * {@link CatalogManager}. For other lyrics, the matching behavior reverts to
 * that of a {@code ContentSearchQuery}.
 *
 * @since 0.8
 * @version 0.9 (2006.12.26)
 * @author Gerrit Meinders
 */
public class AdvancedContentSearchQuery extends ContentSearchQuery {
    /** Text statistics about the search query. */
    private TextStatistics queryStats;

    /** Text statistics about all known lyrics. */
    private Map<Lyric, TextStatistics> lyricStats;

    /** The value that relevance scores are multiplied with. */
    private double multiplier = 1.0;

    /** The value that is added to relevance scores. */
    private double offset = 0.0;

    /**
     * Constructs a new content search query with the given search phrase,
     * method and flags indicating which parts of the lyrics are searched.
     *
     * @param words
     * @param method
     * @param titleSearched indicates whether titles are searched
     * @param textSearched indicates whether texts are searched
     * @param originalTitleSearched indicates whether original titles are
     *        searched
     * @param copyrightsSearched indicates whether copyrights are searched
     */
    public AdvancedContentSearchQuery(String words, Method method,
            boolean titleSearched, boolean textSearched,
            boolean originalTitleSearched, boolean copyrightsSearched) {
        super(words, method, titleSearched, textSearched,
                originalTitleSearched, copyrightsSearched);
    }

    /**
     * Performs lengthy initialization of the query, for example compiling a
     * regular expression.
     */
    @Override
    public void compile() {
        if (getMethod() == Method.EXACT_PHRASE) {
            super.compile();
            return;
        }

        queryStats = new TextStatistics();
        queryStats.add(getWords());

        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        lyricStats = new HashMap<Lyric, TextStatistics>();

        // construct lyric statics
        for (Group bundle : CatalogManager.getCatalog().getBundles()) {
            for (Lyric lyric : bundle.getLyrics()) {
                TextStatistics statistics = new TextStatistics();
                if (isTextSearched())
                    statistics.add(lyric.getText(), 2);
                if (isTitleSearched())
                    statistics.add(lyric.getTitle(), 4);
                if (isOriginalTitleSearched()) {
                    String originalTitle = lyric.getOriginalTitle();
                    if (originalTitle != null) {
                        statistics.add(originalTitle, 3);
                    }
                }
                if (isCopyrightsSearched()) {
                    String copyrights = lyric.getCopyrights();
                    if (copyrights != null) {
                        statistics.add(copyrights);
                    }
                }
                lyricStats.put(lyric, statistics);

                double result = match(lyric);
                if (result < minimum) {
                    minimum = result;
                }
                if (result > maximum) {
                    maximum = result;
                }
            }
        }

        if (minimum == maximum) {
            multiplier = 0.0;
            offset = 1.0;
        } else {
            multiplier = 1.0 / (maximum - minimum);
            offset = 0.0 - minimum;
        }
    }

    /**
     * Matches the given lyric against the search query and returns a double
     * indicating how closely the lyric matched.
     *
     * @param lyric the lyric being matched
     * @return a double in the range from 0.0 (worst) to 1.0 (best).
     */
    @Override
    public double match(Lyric lyric) {
        if (getMethod() == Method.EXACT_PHRASE) {
            return super.match(lyric);
        }

        TextStatistics lyricStat = lyricStats.get(lyric);
        if (lyricStat == null) {
            return super.match(lyric);
        }

        double wordCount = lyricStat.getWordCount();
        if (wordCount == 0) {
            return 0.0;
        }

        double result = 1.0;
        switch (getMethod()) {
        case ANY_WORD:
            for (String word : queryStats.getWords()) {
                result += lyricStat.getWordCount(word) / wordCount;
            }
            break;
        case ALL_WORDS:
            for (String word : queryStats.getWords()) {
                double wordScore = lyricStat.getWordCount(word) / wordCount;
                result = Math.min(result, wordScore);
            }
            break;
        default:
            throw new AssertionError("unknown method: " + getMethod());
        }
        return (result + offset) * multiplier;
    }

    /**
     * This class provides several statistics about a collection of words.
     */
    private static class TextStatistics {
        /** The pattern used to match words in the search query. */
        private static Pattern wordPattern = Pattern.compile("[\\p{N}\\p{L}]+",
                Pattern.CASE_INSENSITIVE);

        /** The total number of words. */
        private int wordCount;

        /** The number of occurences of each word. */
        private Map<String, Integer> wordCounts;

        /** Constructs a new text statistics object. */
        public TextStatistics() {
            wordCount = 0;
            wordCounts = new HashMap<String, Integer>();
        }

        /**
         * Returns a set of all words for which statistics are available.
         *
         * @return the set of words
         */
        public Set<String> getWords() {
            return wordCounts.keySet();
        }

        /**
         * Returns the total number of words.
         *
         * @return the number of words
         */
        public int getWordCount() {
            return wordCount;
        }

        /**
         * Returns the number of occurances of the given word.
         *
         * @param word the word
         * @return the number of occurances of the word.
         */
        public int getWordCount(String word) {
            Integer wordCount = wordCounts.get(word);
            return wordCount == null ? 0 : wordCount;
        }

        /**
         * Processes the given text, adding statistics about it.
         *
         * @param text the text
         */
        public void add(String text) {
            add(text, 1);
        }

        /**
         * Processes the given text, adding statistics about it with the given
         * weight. The weight is the number of occurances counted for each
         * occurance of a word in the text.
         *
         * @param text the text
         * @param weight the weight
         */
        public void add(String text, int weight) {
            Matcher matcher = wordPattern.matcher(text);
            while (matcher.find()) {
                String word = text.substring(matcher.start(), matcher.end())
                        .toLowerCase();
                Integer count = wordCounts.get(word);
                count = (count == null) ? weight : count + weight;
                wordCounts.put(word, count);
                wordCount += weight;
            }
        }
    }
}
