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

import lithium.*;

/**
 * A reference that uses a bundle name and a number to refer to a lyric. The
 * references can be resolved by a {@link Catalog}. Lyric references are most
 * notably used in {@link lithium.Playlist}s.
 *
 * @since 0.2
 * @author Gerrit Meinders
 */
public class LyricRef {
    private String bundle;

    private int number;

    /**
     * Constructs a new lyric reference to the given bundle and number.
     *
     * @param bundle the name of the bundle
     * @param number the number of the lyric in the bundle
     */
    public LyricRef(String bundle, int number) {
        this.bundle = bundle;
        this.number = number;
    }

    /**
     * Returns the name of the referenced bundle.
     *
     * @return the bundle name
     */
    public String getBundle() {
        return bundle;
    }

    /**
     * Returns the number of the referenced lyric according to the referenced
     * bundle's numbering
     *
     * @return the lyric number
     */
    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return Resources.get()
                .getString("LyricRef.displayName", bundle, number);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LyricRef) {
            LyricRef l = (LyricRef) o;
            return l.bundle.equals(this.bundle) && l.number == this.number;
        } else {
            return false;
        }
    }
}
