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
 * In-memory library.
 *
 * @version 0.9x (2005.07.28)
 * @author Gerrit Meinders
 */
public class MemoryLibrary implements Library {

    private String name;

    private Map<String, Book> books;

    /**
     * Constructs a new collection without a name containing no books.
     */
    public MemoryLibrary() {
        this(null);
    }

    /**
     * Constructs a new collection with the given name containing no books.
     *
     * @param name the name
     */
    public MemoryLibrary(String name) {
        books = new LinkedHashMap<String, Book>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Book> getBooks() {
        return new LinkedHashSet<Book>(books.values());
    }

    public void addBook(Book book) {
        books.put(book.getTitle(), book);
    }

    public void removeBook(String name) {
        books.remove(name);
    }

    public Book getBook(String name) {
        return books.get(name);
    }

    public Verse getVerse(String book, int chapter, int verse) {
        Book theBook = getBook(book);
        if (theBook == null) {
            return null;
        }

        Chapter theChapter = theBook.getChapter(chapter);
        if (theChapter == null) {
            return null;
        }

        return theChapter.getVerse(verse);
    }

    /**
     * Returns the name of the collection.
     */
    public String toString() {
        return getName();
    }
}
