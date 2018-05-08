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

package lithium;

/**
 * An interface for objects that can reorder their data.
 *
 * @version 0.9 (2005.10.23)
 * @author Gerrit Meinders
 */
public interface Reorderable {
    /**
     * Swaps the elements at the two indices.
     *
     * @param index1 the first index
     * @param index2 the second index
     */
    public void swap(int index1, int index2);

    /**
     * Moves the element at the source index to the destination index. The
     * ordering of other elements remains the same.
     *
     * @param from the source index
     * @param to the destination index
     */
    public void move(int from, int to);
}

