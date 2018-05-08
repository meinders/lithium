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
 * A reference group for cds.
 *
 * @since 0.1
 * @author Gerrit Meinders
 */
public class CD extends ReferenceGroup {
    /**
     * Constructs a new CD with the given name.
     *
     * @param name the name
     */
    public CD(String name) {
        this(name, "");
    }

    /**
     * Constructs a new CD with the given name and version.
     *
     * @param name the name
     * @param version the version
     */
    public CD(String name, String version) {
        super("CD.displayName", name, version);
    }
}
