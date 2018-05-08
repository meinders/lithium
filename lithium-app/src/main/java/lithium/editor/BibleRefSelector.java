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

package lithium.editor;

import java.util.*;

import lithium.io.*;

public class BibleRefSelector implements Selector {
	public Collection<?> select(String text) {
		try {
			return Collections.singleton(BibleRefParser.parse(text));
		} catch (Exception ex) {
			/* Not a valid bible reference. */
		}

		return Collections.emptySet();
	}
}
