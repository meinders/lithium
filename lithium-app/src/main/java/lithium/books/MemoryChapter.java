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

package lithium.books;

import java.util.*;

/**
 * A model of a chapter, the numbered entity containing verses.
 *
 * @author Gerrit Meinders
 */
public class MemoryChapter implements Chapter {
    private String title;

    private SortedMap<Integer, Verse> verses;

    /**
     * Constructs a new chapter with the given number, containing no verses.
     *
     * @param number the number
     */
    public MemoryChapter(int number) {
        this(String.valueOf(number));
    }

    /**
     * Constructs a new chapter with the given title, containing no verses.
     *
     * @param title The title of the chapter.
     */
    public MemoryChapter(String title) {
        this.title = title;
        verses = new TreeMap<Integer, Verse>();
    }

    public String getTitle() {
        return title;
    }

    @Deprecated
    public int getNumber() {
        return Integer.parseInt(title);
    }

    public Collection<Verse> getVerses() {
        return Collections.unmodifiableCollection(verses.values());
    }

    public void addVerse(Verse verse) {
        verses.put(verse.getRangeStart(), verse);
    }

    public Verse getVerse(int number) {
        return verses.get(number);
    }

    @Override
    public String toString() {
        return super.toString() + "[title='" + title + "', " + verses.size()
                + " verse ranges]";
    }
}
