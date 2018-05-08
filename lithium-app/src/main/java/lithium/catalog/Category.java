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

/**
 * A reference group for categories.
 *
 * @since 0.1
 * @author Gerrit Meinders
 */
public class Category extends ReferenceGroup {
    /**
     * Constructs a new category with the given name. The version of the
     * category will be set to the empty string.
     *
     * @param name the name
     */
    public Category(String name) {
        this(name, "");
    }

    /**
     * Constructs a new category with the given name and version.
     *
     * @param name the name
     * @param version the version
     */
    public Category(String name, String version) {
        super("Category.displayName", name, version);
    }
}
