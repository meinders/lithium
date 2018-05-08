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

package lithium;

import java.io.*;

import lithium.catalog.*;

/**
 * This class initiates loading of configuration settings and creation of the
 * user interface.
 *
 * @version 0.9 (2006.02.15)
 * @author Gerrit Meinders
 */
public class SpellingChecker {
    public static void main(String[] args) {
        CatalogManager.loadDefaultCatalogs(null);

        try {
            checkSpelling(CatalogManager.getCatalog());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkSpelling(Catalog catalog) throws IOException {
        /*
        System.out.println("Loading word list...");
        TreeSet<String> dictionary = new TreeSet<String>();
        BufferedReader in = new BufferedReader(new FileReader("wordlist.txt"));
        while (true) {
            String word = in.readLine();
            if (word == null) {
                break;
            }
            word = word.replaceAll("\u00b7", "");
            dictionary.add(word);
        }
        in.close();
            / *
            if (!dictionary.contains(word) &&
                    !dictionary.contains(word.toLowerCase())) {
                System.out.printf("%s. %s: Unknown word: %s\n",
                        lyric.getNumber(), lyric.getTitle(), word);
            }
            * /
        */

        System.out.println("Checking spelling...");
        final String nonWord = "[\\p{Z}\\p{C}\\p{P}&&[^'\"\u2018\u2019\u201c\u201d]]+";
        final String wordPattern = "([\\p{L}\\p{Sk}'\"\u2018\u2019\u201c\u201d])+|(\\p{N}+[xe]?)";
        int fixedQuotes = 0;
        int ambiguousQuotes = 0;
        for (Group bundle : catalog.getBundles()) {
            for (Lyric lyric : bundle.getLyrics()) {
                String[] words = lyric.getText().split(nonWord);
                for (String word : words) {
                    if (word.length() > 0) {
                        if (!word.matches(wordPattern)) {
                            System.out.printf("%s. %s: Warning: unexpected characters in word: %s\n",
                                    lyric.getNumber(), lyric.getTitle(), word);
                        } else if (word.charAt(word.length() - 1) == '\'') {
                            /*
                            System.out.printf("%s. %s: Notice: typewriter apostrophe -> single close quote: %s\n",
                                    lyric.getNumber(), lyric.getTitle(), word);
                            */
                            fixedQuotes++;
                        } else if (word.charAt(0) == '\'') {
                            /*
                            System.out.printf("%s. %s: Warning: ambiguous typewriter apostrophe: %s\n",
                                    lyric.getNumber(), lyric.getTitle(), word);
                            */
                            ambiguousQuotes++;
                        } else if (word.indexOf('\'') > 0) {
                            /*
                            System.out.printf("%s. %s: Notice: typewriter apostrophe -> single close quote: %s\n",
                                    lyric.getNumber(), lyric.getTitle(), word);
                            */
                            fixedQuotes++;
                        } else if (word.charAt(word.length() - 1) == '"') {
                            /*
                            System.out.printf("%s. %s: Notice: typewriter quote -> double close quote: %s\n",
                                    lyric.getNumber(), lyric.getTitle(), word);
                            */
                            fixedQuotes++;
                        } else if (word.charAt(0) == '"') {
                            /*
                            System.out.printf("%s. %s: Notice: typewriter quote -> double open quote: %s\n",
                                    lyric.getNumber(), lyric.getTitle(), word);
                            */
                            fixedQuotes++;
                        } else if (word.indexOf('"') > -1) {
                            /*
                            System.out.printf("%s. %s: Warning: unexpected typewriter quote: %s\n",
                                    lyric.getNumber(), lyric.getTitle(), word);
                            */
                            ambiguousQuotes++;
                        } else if (word.indexOf('\u2018') > -1) {
                        } else if (word.indexOf('\u2019') > -1) {
                        }
                    }
                }
            }
        }
        System.out.printf("Automatically fixable quotes: %s\n", fixedQuotes);
        System.out.printf("Ambiguous quotes: %s\n", ambiguousQuotes);
    }
}

