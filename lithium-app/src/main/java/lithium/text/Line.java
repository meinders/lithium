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

import java.text.*;
import java.text.AttributedCharacterIterator.*;

public class Line
{
	private Paragraph paragraph;

	private int startIndex;

	private int endIndex;

	public Line(Paragraph paragraph, int startIndex, int endIndex)
	{
		super();
		this.paragraph = paragraph;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	private AttributedCharacterIterator getIterator()
	{
		return paragraph.getIterator(new Attribute[0], startIndex, endIndex);
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder(endIndex - startIndex + 1);

		AttributedCharacterIterator iterator = getIterator();
		for (char c = iterator.first(); c != AttributedCharacterIterator.DONE; c = iterator.next())
		{
			switch (c)
			{
			case '\t':
				result.append("    ");
			case '\f':
				result.append(' ');
				break;
			case '\n':
			case '\r':
				break;
			default:
				result.append(c);
			}
		}

		return result.toString();
	}
}
