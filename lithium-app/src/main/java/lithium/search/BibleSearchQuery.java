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

package lithium.search;

import lithium.catalog.*;

/**
 * A search query that searches for a specific bible reference and nearby
 * references.
 *
 * @since 0.8
 * @version 0.9 (2006.02.22)
 * @author Gerrit Meinders
 */
public class BibleSearchQuery implements SearchQuery {
    /** The reference to search for. */
    private BibleRef reference;

    /**
     * Constructs a new search query that searches for the given bible
     * reference.
     *
     * @param reference the bible reference
     */
    public BibleSearchQuery(BibleRef reference) {
        this.reference = reference;
    }

    /**
     * Bible search queries require no initialization, so this method does
     * nothing.
     */
    public void compile() {
        // no initialization required
    }

    /**
     * Matches the given lyric against the search query and returns a double
     * indicating how closely the lyric matched.
     *
     * @param lyric the lyric being matched
     * @return a double in the range from 0.0 (worst) to 1.0 (best).
     */
    public double match(Lyric lyric) {
        double result = 0.0;
        for (BibleRef reference : lyric.getBibleRefs()) {
            double thisResult = this.reference.contains(reference) ? 1.0 : 0.0;
            if (thisResult > result) {
                result = thisResult;
            }
        }
        return result;
    }
}
