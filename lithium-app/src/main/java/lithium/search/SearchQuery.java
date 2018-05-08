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
 * The interface for search queries
 *
 * @since 0.8
 * @version 0.9 (2006.02.22)
 * @author Gerrit Meinders
 */
public interface SearchQuery {
    /**
     * Performs lengthy initialization of the query, for example compiling a
     * regular expression.
     */
    public void compile();

    /**
     * Matches the given lyric against the search query and returns a double
     * indicating how closely the lyric matched.
     *
     * @param lyric the lyric being matched
     * @return a double in the range from 0.0 (worst) to 1.0 (best).
     */
    public double match(Lyric lyric);
}

