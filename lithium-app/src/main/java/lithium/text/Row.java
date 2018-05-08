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

package lithium.text;

import java.util.*;

public class Row {
	private List<Paragraph> paragraphs;

	public Row() {
		paragraphs = new ArrayList<Paragraph>();
	}

	public void addParagraph(Paragraph paragraph) {
		paragraphs.add(paragraph);
	}

	public void removeParagraph(Paragraph paragraph) {
		paragraphs.remove(paragraph);
	}

	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}

	public void updateLayout(Document document) {
		for (Paragraph paragraph : paragraphs) {
			paragraph.updateLayout(document);
		}
	}
}
