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

package lithium.catalog;

import java.util.*;

import com.github.meinders.common.*;

/**
 * <p>
 * A reference to part of the Bible. The part can either be a single verse, a
 * range of verses, a chapter, a range of chapters or an entire book.
 *
 * <p>
 * There is no support for references to multiple ranges.
 *
 * @since 0.1
 * @author Gerrit Meinders
 */
public class BibleRef implements Comparable<BibleRef> {
    /**
     * Returns the names of the bible books for the current default locale.
     *
     * @return the bible book names
     */
    public static String[] getBooks() {
        return getBooks(Locale.getDefault());
    }

    /**
     * Returns the names of the bible books for the given locale.
     *
     * @param locale the locale
     * @return the bible book names
     */
    public static String[] getBooks(Locale locale) {
        ResourceBundle bundle = getResourceBundle(locale);
        return ResourceUtilities.getStringArray(bundle, "Bible.books");
    }

    public static String getBook(int index, Locale locale) {
        ResourceBundle bundle = getResourceBundle(locale);
        return ResourceUtilities.getStringArrayElement(bundle, "Bible.books", index);
    }

    public static String[] getAlternativeBookNames(String book, Locale locale) {
        int bookIndex = Arrays.asList(getBooks(locale)).indexOf(book);
        return getAlternativeBookNames(bookIndex, locale);
    }

    public static String[] getAlternativeBookNames(int bookIndex, Locale locale) {
        ResourceBundle bundle = getResourceBundle(locale);
        String key = ResourceUtilities.getArrayElementKey("Bible.books", bookIndex);
        return ResourceUtilities.getStringArrayCS(bundle, key + ".alt");
    }

    private static ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle("lithium.Bible", locale);
    }

    private int book;

    private Integer startChapter;

    private Integer endChapter;

    private Integer startVerse;

    private Integer endVerse;

    /**
     * Constructs a new bible reference to the specified verses. The name of the
     * book is resolved using the current default locale.
     *
     * @param bookName the name of the book containing the verses
     * @param startChapter the number of the chapter containing the first verse
     * @param endChapter the number of the chapter containing the last verse
     * @param startVerse the number of the first verse
     * @param endVerse the number of the last verse
     */
    public BibleRef(String bookName, Integer startChapter, Integer endChapter,
            Integer startVerse, Integer endVerse) {
        this(bookName, Locale.getDefault(), startChapter, endChapter, startVerse, endVerse);
    }

    /**
     * Constructs a new bible reference to the specified verses.
     *
     * @param bookName the name of the book containing the verses
     * @param locale the locale of the book name
     * @param startChapter the number of the chapter containing the first verse
     * @param endChapter the number of the chapter containing the last verse
     * @param startVerse the number of the first verse
     * @param endVerse the number of the last verse
     */
    public BibleRef(String bookName, Locale locale, Integer startChapter, Integer endChapter,
            Integer startVerse, Integer endVerse) {
        this(resolveBookIndex(bookName, locale), startChapter, endChapter, startVerse,
                endVerse);
    }

    /**
     * Constructs a new bible reference to the specified verses.
     *
     * @param book the number of the book containing the verses
     * @param startChapter the number of the chapter containing the first verse
     * @param endChapter the number of the chapter containing the last verse
     * @param startVerse the number of the first verse
     * @param endVerse the number of the last verse
     */
    public BibleRef(Integer book, Integer startChapter, Integer endChapter,
            Integer startVerse, Integer endVerse) {
        super();
        setBookIndex(book);
        setStartChapter(startChapter);
        if (endChapter != null) {
            setEndChapter(endChapter);
        }
        setStartVerse(startVerse);
        if (endVerse != null) {
            setEndVerse(endVerse);
        }
    }

    private void setBookIndex(Integer book) {
        if (book == null || book < 0) {
            throw new IllegalArgumentException("book: " + book);
        }
        this.book = book;
    }

    /**
     * Set the number of the chapter containing the first verse. This method
     * ensures that startChapter and endChapter are both either null or
     * non-null.
     *
     * @param startChapter the start chapter to be set, or {@code null}.
     */
    private void setStartChapter(Integer startChapter) {
        if (startChapter != null && startChapter < 0) {
            throw new IllegalArgumentException("startChapter: " + startChapter);
        }
        this.startChapter = startChapter;
        if (startChapter == null ^ endChapter == null) {
            endChapter = startChapter;
        }
    }

    /**
     * Set the number of the chapter containing the last verse. This method
     * ensures that startChapter and endChapter are both either null or
     * non-null.
     *
     * @param startChapter the end chapter to be set, or {@code null}.
     */
    private void setEndChapter(Integer endChapter) {
        if (endChapter != null && endChapter < startChapter) {
            throw new IllegalArgumentException("endChapter: " + endChapter
                    + " (startChapter = " + startChapter + ")");
        }
        this.endChapter = endChapter;
        if (startChapter == null ^ endChapter == null) {
            startChapter = endChapter;
        }
    }

    private void setStartVerse(Integer startVerse) {
        if (startVerse != null && endVerse != null && startChapter == endChapter
                && endVerse < startVerse) {
            throw new IllegalArgumentException("startVerse: " + startVerse);
        }
        this.startVerse = startVerse;
        if (startVerse == null ^ endVerse == null) {
            endVerse = startVerse;
        }
    }

    private void setEndVerse(Integer endVerse) {
        if (startVerse != null && endVerse != null && startChapter == endChapter
                && endVerse < startVerse) {
            throw new IllegalArgumentException("endVerse: " + endVerse);
        }
        this.endVerse = endVerse;
        if (startVerse == null ^ endVerse == null) {
            startVerse = endVerse;
        }
    }

    /**
     * Returns the number of the first chapter in the reference, if any.
     *
     * @return the number of the chapter, or {@code null}
     */
    public Integer getStartChapter() {
        return startChapter;
    }

    /**
     * Returns the number of the last chapter in the reference, if any.
     *
     * @return the number of the chapter, or {@code null}
     */
    public Integer getEndChapter() {
        return endChapter;
    }

    /**
     * Returns the number of the first verse in the reference, if any.
     *
     * @return the number of the verse, or {@code null}
     */
    public Integer getStartVerse() {
        return startVerse;
    }

    /**
     * Returns the number of the last verse in the reference, if any.
     *
     * @return the number of the verse, or {@code null}
     */
    public Integer getEndVerse() {
        return endVerse;
    }

    /**
     * Returns the name of the book in this reference, as it appears in
     * references to parts of that book. The default locale is used to look up
     * the name.
     *
     * @see #getBookReferenceName(Locale)
     *
     * @return the name of the book for use in references to part of it
     */
    public String getBookReferenceName() {
        return getBookReferenceName(Locale.getDefault());
    }

    /**
     * <p>
     * Returns the name of the book in this reference, as it appears in
     * references to parts of that book.
     *
     * <p>
     * For example, the first chapter of the book of <strong>Psalms</strong> is
     * typically called <strong>Psalm</strong> 1, not Psalms 1. Therefore, this
     * method would return <code>"Psalm"</code>, unlike
     * <code>getBookName</code> which would return <code>"Psalms"</code>.
     *
     * @see #getBookName(Locale)
     * @param locale the locale to be used
     * @return the name of the book for use in references to part of it
     */
    public String getBookReferenceName(Locale locale) {
        // try to find alternate name for references
        String key = ResourceUtilities.getArrayElementKey("Bible.books", book);
        key = ResourceUtilities.getKey(key, "ref");
        String bookRefName = ResourceUtilities.getRawString(getResourceBundle(locale), key);
        if (bookRefName == null) {
            // fallback to default book name
            return getBookName(locale);
        } else {
            // return alternate name
            return bookRefName;
        }
    }

    /**
     * Returns the name of the book in this bible reference. This method uses
     * the default locale.
     *
     * @see #getBookName(Locale)
     * @return the name of the book
     */
    public String getBookName() {
        return getBookName(Locale.getDefault());
    }

    /**
     * Returns the name of the book in this bible reference.
     *
     * @param locale the locale to be used
     * @return the name of the book
     */
    public String getBookName(Locale locale) {
        return ResourceUtilities.getStringArrayElement(getResourceBundle(locale),
                "Bible.books", book);
    }

    /**
     * Returns the number of the referenced book.
     *
     * @return the number of the book
     */
    public int getBookIndex() {
        return book;
    }

    @Override
    public String toString() {
        return toString(Locale.getDefault());
    }

    /**
     * Converts the bible reference to a string representation suitable for the
     * given locale.
     *
     * @param locale the locale
     * @return a string representation of the bible reference
     */
    public String toString(Locale locale) {
        final String bookName = getBookReferenceName(locale);
        if (startChapter == null) {
            if (startVerse == null) {
                return getBookName(locale);
            } else if (startVerse == endVerse) {
                // Bible.notations[5]={0} verse {1}
                String key = ResourceUtilities.getArrayElementKey("Bible.notations", 5);
                return ResourceUtilities.getString(getResourceBundle(locale), key, bookName,
                        startVerse);
            } else {
                // Bible.notations[6]={0} verse {1}-{2}
                String key = ResourceUtilities.getArrayElementKey("Bible.notations", 6);
                return ResourceUtilities.getString(getResourceBundle(locale), key, bookName,
                        startVerse, endVerse);
            }
        } else if (startChapter == endChapter) {
            if (startVerse == null) {
                // Bible.notations[0]={0} {1}
                String key = ResourceUtilities.getArrayElementKey("Bible.notations", 0);
                return ResourceUtilities.getString(getResourceBundle(locale), key, bookName,
                        startChapter);
            } else if (startVerse == endVerse) {
                // Bible.notations[2]={0} {1}:{2}
                String key = ResourceUtilities.getArrayElementKey("Bible.notations", 2);
                return ResourceUtilities.getString(getResourceBundle(locale), key, bookName,
                        startChapter, startVerse);
            } else {
                // Bible.notations[3]={0} {1}:{2}-{3}
                String key = ResourceUtilities.getArrayElementKey("Bible.notations", 3);
                return ResourceUtilities.getString(getResourceBundle(locale), key, bookName,
                        startChapter, startVerse, endVerse);
            }
        } else {
            if (startVerse == null) {
                // Bible.notations[1]={0} {1}-{2}
                String key = ResourceUtilities.getArrayElementKey("Bible.notations", 1);
                return ResourceUtilities.getString(getResourceBundle(locale), key, bookName,
                        startChapter, endChapter);
            } else {
                // Bible.notations[4]={0} {1}:{2}-{3}:{4}
                String key = ResourceUtilities.getArrayElementKey("Bible.notations", 4);
                return ResourceUtilities.getString(getResourceBundle(locale), key, bookName,
                        startChapter, startVerse, endChapter, endVerse);
            }
        }
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean result;
        if (obj instanceof BibleRef) {
            final BibleRef other = (BibleRef) obj;
            result = book == other.book
                    && (startChapter == null ? other.startChapter == null : startChapter
                            .equals(other.startChapter))
                    && (startVerse == null ? other.startVerse == null : startVerse
                            .equals(other.startVerse))
                    && (endChapter == null ? other.endChapter == null : endChapter
                            .equals(other.endChapter))
                    && (endVerse == null ? other.endVerse == null : endVerse
                            .equals(other.endVerse));
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Compares two BibleRef objects, based on the position in the Bible of the
     * verses they reference.
     *
     * @param b the other bible reference
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object
     */
    public int compareTo(BibleRef b) {
        int result = book - b.book;
        result *= 150;
        if (startChapter != null && b.startChapter != null) {
            result += startChapter - b.startChapter;
        }
        result *= 300;
        if (startVerse != null && b.startVerse != null) {
            result += startVerse - b.startVerse;
        }
        if (endVerse != null && b.endVerse != null) {
            result += endVerse - b.endVerse;
        }
        return result;
    }

    private Integer maximum(final Integer a, final Integer b) {
        if (a == null) {
            return b;
        } else {
            return (b == null) ? a : Math.max(a, b);
        }
    }

    private Integer minimum(final Integer a, final Integer b) {
        if (a == null) {
            return b;
        } else {
            return (b == null) ? a : Math.min(a, b);
        }
    }

    private BibleRef intersection(final BibleRef other) {
        final BibleRef result;

        if (book == other.book) {
            final Integer startChapter = maximum(this.startChapter, other.startChapter);

            final Integer endChapter = minimum(this.endChapter, other.endChapter);

            if (startChapter != null && startChapter > endChapter) {
                result = null;

            } else {
                final Integer startVerse = (startChapter == this.startChapter)
                        ? (startChapter == other.startChapter) ? maximum(this.startVerse,
                                other.startVerse) : this.startVerse : other.startVerse;

                final Integer endVerse = (endChapter == this.endChapter)
                        ? (endChapter == other.endChapter) ? minimum(this.endVerse,
                                other.endVerse) : this.endVerse : other.endVerse;

                result = (startVerse != null && startVerse > endVerse) ? null : new BibleRef(
                        book, startChapter, endChapter, startVerse, endVerse);
            }
        } else {
            result = null;
        }

        return result;
    }

    public boolean contains(final BibleRef other) {
        final BibleRef intersection = intersection(other);
        return intersection != null && intersection.equals(other);
    }

    private static Integer resolveBookIndex(String bookName, Locale locale) {
        // iterate over all book names for the given locale
        String[] books = getBooks(locale);
        for (int i = 0; i < books.length; i++) {
            // match bookName agains this book
            String book = books[i];
            if (book.equalsIgnoreCase(bookName)) {
                return i;
            }

            // match bookName agains alternate spellings for this book
            String[] alternatives = getAlternativeBookNames(i, locale);
            if (alternatives != null) {
                for (String alternative : alternatives) {
                    if (alternative.equalsIgnoreCase(bookName)) {
                        return i;
                    }
                }
            }
        }

        // unable to identify index for bookName
        return null;
    }
}
