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

import java.io.*;
import java.util.*;

/**
 * A model of a verse, the smallest text fragment in a book that can be
 * referenced without requiring a citation.
 *
 * <p>
 * In some cases, several verses may be considered a single entity if the
 * boundaries between them can't be properly be distinguished. In such cases, a
 * single {@code Verse} object may represent all of those verses and take up
 * several verse numbers.
 *
 * @version 0.9x (2005.07.27)
 * @author Gerrit Meinders
 */
public class Verse implements Comparable<Verse> {
    /**
     * Returns a verse with the given number containing no other data. Instances
     * created using this method are used to lookup verses in sorted sets.
     *
     * @param number The number of the verse.
     *
     * @return A verse with the given number, but no further content.
     */
    @Deprecated
    public static Verse getStub(int number) {
        return new Verse(number);
    }

    private int rangeStart;

    private int rangeEnd;

    private List<Fragment> fragments;

    /**
     * Constructs a new verse with the given number.
     *
     * @param number the number
     */
    private Verse(int number) {
        this.rangeStart = number;
        this.rangeEnd = number;
        this.fragments = Collections.emptyList();
    }

    /**
     * Constructs a new verse with the given number and text.
     *
     * @param number the number
     * @param text the text
     */
    public Verse(int number, String text) {
        this(number, number, text);
    }

    /**
     * Constructs a new verse with the given range and text.
     *
     * @param rangeStart the start of the range
     * @param rangeEnd the end of the range
     * @param text the text
     */
    public Verse(int rangeStart, int rangeEnd, String text) {
        assert text != null : "text mustn't be null";
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.fragments = Collections.<Fragment> singletonList(new Text(text));
    }

    /**
     * Constructs a new verse with the given range and content.
     *
     * @param rangeStart The start of the range.
     * @param rangeEnd The end of the range.
     * @param fragments The fragments that make up the verse.
     */
    public Verse(int rangeStart, int rangeEnd, Collection<Fragment> fragments) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.fragments = new ArrayList<Fragment>(fragments);
    }

    /**
     * Returns the start of the verse's range.
     *
     * @return the start of the range
     */
    public int getRangeStart() {
        return rangeStart;
    }

    /**
     * Returns the end of the verse's range.
     *
     * @return the end of the range
     */
    public int getRangeEnd() {
        return rangeEnd;
    }

    public List<Fragment> getFragments() {
        return Collections.unmodifiableList(fragments);
    }

    /**
     * Returns the verse's text.
     *
     * @return the text
     */
    public String getText() {
        StringBuilder builder = new StringBuilder();
        try {
            for (Fragment fragment : fragments) {
                fragment.appendTo(builder);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return builder.toString();
    }

    /**
     * Returns a string representation of the verse's range. If the start and
     * end of the range are the same, only the start is returned. Otherwise, the
     * start and end are returned separated by a dash ('-').
     */
    public String getRange() {
        if (getRangeStart() == getRangeEnd()) {
            return "" + getRangeStart();
        } else {
            return getRangeStart() + "-" + getRangeEnd();
        }
    }

    /**
     * Compares the verse to another, returning the difference in the start of
     * their ranges.
     *
     * @param other the other verse
     */
    public int compareTo(Verse other) {
        return getRangeStart() - other.getRangeStart();
    }

    public String toString() {
        return getText();
    }

    public static interface Fragment {
        public void appendTo(Appendable appendable) throws IOException;
    }

    public static class Text implements Fragment {
        private String text;

        public Text(String text) {
            super();
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void appendTo(Appendable appendable) throws IOException {
            appendable.append(text);
        }
    }

    public static class SmallCaps extends Text {
        public SmallCaps(String text) {
            super(text);
        }
    }

    public static class Implied extends Text {
        public Implied(String text) {
            super(text);
        }
    }

    public static class Literal extends Text {
        public Literal(String text) {
            super(text);
        }
    }

    public static class Role extends Text {
        public Role(String text) {
            super(text);
        }
    }

    public static class Note extends Text {
        public Note(String text) {
            super(text);
        }

        @Override
        public void appendTo(Appendable appendable) throws IOException {
            // Not included in text representation.
        }
    }

    public static class PericopeHeader implements Fragment {
        private String title;

        public PericopeHeader(String title) {
            super();
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void appendTo(Appendable appendable) throws IOException {
            appendable.append(title);
            appendable.append('\n');
            appendable.append('\n');
        }
    }
}
