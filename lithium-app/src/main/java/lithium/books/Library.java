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

public interface Library {

    /**
     * Returns the name of the libary.
     *
     * @return The name of the library.
     */
    public String getName();

    /**
     * Sets the name of the library.
     *
     * @param name The name to be set.
     */
    public void setName(String name);

    /**
     * Returns the books in the library.
     *
     * @return The books in the library. (unmodifiable)
     */
    public Set<Book> getBooks();

    /**
     * Adds the given book to the library.
     *
     * @param book The book to be added.
     */
    public void addBook(Book book);

    /**
     * Removes the specified book from the library.
     *
     * @param name The name of the book to be removed.
     */
    public void removeBook(String name);

    /**
     * Returns the book with the given name, or <code>null</code> if no such
     * book is found in the library. Note that this name is not per se the title
     * of the book.
     *
     * @param name A name identifying the book.
     *
     * @return The book, or <code>null</code> if not found.
     */
    public Book getBook(String name);

    /**
     * Returns the verse with the given number from the given book and chapter,
     * or <code>null</code> if the verse wasn't found.
     *
     * @param book the book
     * @param chapter the chapter
     * @param verse the number of the verse
     *
     * @return the verse, or <code>null</code> if not found
     */
    @Deprecated
    public Verse getVerse(String book, int chapter, int verse);

}
