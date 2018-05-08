/*
 * Copyright 2008 Gerrit Meinders
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

public class Range {
    private Reference start;

    private Reference end;

    /**
     * Constructs a new range from two references.
     *
     * @param start The start of the range.
     * @param end The end of the range (inclusive). May be <code>null</code>
     *        if equal to <code>start</code>.
     */
    public Range(Reference start, Reference end) {
        super();
        if (start == null)
            throw new NullPointerException("start");
        this.start = start;
        this.end = (end == null) ? start : (end.equals(start) ? start : end);
    }

    /**
     * Returns the start of the range.
     *
     * @return The start of the range (inclusive).
     */
    public Reference getStart() {
        return start;
    }

    /**
     * Returns the end of the range, the last part which is still contained in
     * the range. If the end equals the start of the range, both are guaranteed
     * to be the same object.
     *
     * @return The end of the range (inclusive).
     */
    public Reference getEnd() {
        return end;
    }

    public boolean isSingular() {
        return start == end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Range) {
            Range other = (Range) obj;
            return start.equals(other.start)
                    && ((start == end) ? other.start == other.end : end.equals(other.end));
        } else {
            return false;
        }
    }
}
