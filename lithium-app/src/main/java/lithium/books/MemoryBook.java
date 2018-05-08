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
 * A model of a book, the named entity containing chapters.
 *
 * @version 0.9x (2005.07.27)
 * @author Gerrit Meinders
 */
public class MemoryBook implements Book {
	private final String title;

	private Map<String, Chapter> chapters;

	/**
	 * Constructs a new book with the given title. The book is initially empty,
	 * containing no chapters.
	 *
	 * @param title The title of the book.
	 */
	public MemoryBook(String title) {
		chapters = new LinkedHashMap<String, Chapter>();
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public Set<Chapter> getChapters() {
		return new LinkedHashSet<Chapter>(chapters.values());
	}

	public void addChapter(Chapter chapter) {
		chapters.put(chapter.getTitle(), chapter);
	}

	@Deprecated
	public Chapter getChapter(int number) {
		return null;
	}

	@Override
	public String toString() {
		return title;
	}
}
