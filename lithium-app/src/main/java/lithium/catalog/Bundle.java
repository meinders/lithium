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
 * A bundle of lyrics.
 *
 * @since 0.1
 * @author Gerrit Meinders
 */
public class Bundle extends ContainerGroup {
    /**
     * Constructs a new bundle with the given name and version.
     *
     * @param name the name
     * @param version the version
     */
    public Bundle(String name, String version) {
        super("Bundle.displayName", name, version);
    }
}
