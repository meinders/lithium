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

import java.util.regex.*;

import lithium.catalog.*;

import static java.util.regex.Pattern.*;

/**
 * A search query that searches for words in the contents of a lyric.
 *
 * @since 0.8
 * @version 0.9 (2006.02.22)
 * @author Gerrit Meinders
 */
public class ContentSearchQuery implements SearchQuery {
    /** An enumeration of content search methods. */
    public static enum Method {
        /** Matches lyrics that contain at least one of the specified words. */
        ANY_WORD {
            protected String createRegularExpression(String words) {
                String space = "\\E|\\Q";
                return "(.*)(\\Q" + words.replace(" ", space) + "\\E)(.*)";
            }
        },

        /** Matches lyrics that contain all of the specified words. */
        ALL_WORDS {
            protected String createRegularExpression(String words) {
                String space = "\\E(.*)\\Q";
                return "(.*)(\\Q" + words.replace(" ", space) + "\\E)(.*)";
            }
        },

        /** Matches lyrics that contain all of the specified words in order. */
        EXACT_PHRASE {
            protected String createRegularExpression(String words) {
                String space = "\\E([\\p{M}[\\p{C}[\\p{Z}[\\p{P}]]]]*)\\Q";
                return "(.*)(\\Q" + words.replace(" ", space) + "\\E)(.*)";
            }
        };

        /**
         * Constructs a regular expression that will match the given search
         * phrase as defined by the specific method.
         *
         * @param words the search phrase
         * @return the regular expression
         */
        protected abstract String createRegularExpression(String words);
    }

    /** The search phrase. */
    private String words;

    /** The content search method used. */
    private Method method;

    /** A flag indicating whether titles are searched. */
    private boolean titleSearched;

    /** A flag indicating whether texts are searched. */
    private boolean textSearched;

    /** A flag indicating whether original titles are searched. */
    private boolean originalTitleSearched;

    /** A flag indicating whether copyrights are searched. */
    private boolean copyrightsSearched;

    /** The compiled regular expression used to match the search phrase. */
    private Pattern pattern;

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
    public ContentSearchQuery(String words, Method method,
            boolean titleSearched, boolean textSearched,
            boolean originalTitleSearched, boolean copyrightsSearched) {
        this.words = words;
        this.method = method;
        this.titleSearched = titleSearched;
        this.textSearched = textSearched;
        this.originalTitleSearched = originalTitleSearched;
        this.copyrightsSearched = copyrightsSearched;
    }

    /**
     * Returns the query's search phrase.
     *
     * @return the search phrase
     */
    public String getWords() {
        return words;
    }

    /**
     * Returns the query's search method.
     *
     * @return the search method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns whether titles are searched.
     *
     * @return {@code true} if titles are searched; {@code false} otherwise
     */
    public boolean isTitleSearched() {
        return titleSearched;
    }

    /**
     * Returns whether texts are searched.
     *
     * @return {@code true} if texts are searched; {@code false} otherwise
     */
    public boolean isTextSearched() {
        return textSearched;
    }

    /**
     * Returns whether original titles are searched.
     *
     * @return {@code true} if original titles are searched; {@code false}
     *         otherwise
     */
    public boolean isOriginalTitleSearched() {
        return originalTitleSearched;
    }

    /**
     * Returns whether copyrights are searched.
     *
     * @return {@code true} if copyrights are searched; {@code false} otherwise
     */
    public boolean isCopyrightsSearched() {
        return copyrightsSearched;
    }

    /**
     * Performs lengthy initialization of the query, for example compiling a
     * regular expression.
     */
    public void compile() {
        pattern = Pattern.compile(method.createRegularExpression(words),
                CASE_INSENSITIVE | DOTALL | UNICODE_CASE);
    }

    /**
     * Matches the given lyric against the search query and returns a double
     * indicating how closely the lyric matched.
     *
     * @param lyric the lyric being matched
     * @return a double in the range from 0.0 (worst) to 1.0 (best).
     */
    public double match(Lyric lyric) {
        boolean matches = false;
        matches |= isTitleSearched() && match(lyric.getTitle());
        matches |= isTextSearched() && match(lyric.getText());
        matches |= isOriginalTitleSearched() && match(lyric.getOriginalTitle());
        matches |= isCopyrightsSearched() && match(lyric.getCopyrights());
        return matches ? 1.0 : 0.0;
    }

    /**
     * Matches the given text.
     *
     * @param text the text
     * @return whether the text matches the query
     */
    private boolean match(String text) {
        if (text == null) {
            return false;
        } else {
            return pattern.matcher(text).matches();
        }
    }
}
