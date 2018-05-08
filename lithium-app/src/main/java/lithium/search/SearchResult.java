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
 * A search result, consisting of a lyric reference and its relevance.
 *
 * @since 0.8
 * @version 0.9 (2006.02.22)
 * @author Gerrit Meinders
 */
public class SearchResult implements Comparable<SearchResult> {
    /** The relevance of the search result. */
    private double relevance;

    /** The lyric reference. */
    private LyricRef reference;

    /**
     * Constructs a new search result of the given lyric reference with the
     * given relevance.
     *
     * @param relevance the relevance of the reference to the search query
     * @param reference the lyric reference
     */
    public SearchResult(double relevance, LyricRef reference) {
        assert relevance >= 0.0 : "Invalid relevance: " + relevance;
        assert reference != null : "Invalid reference: " + reference;
        this.relevance = relevance;
        this.reference = reference;
    }

    /**
     * Returns the relevance of the search result.
     *
     * @return the relevance
     */
    public double getRelevance() {
        return relevance;
    }

    /**
     * Returns the lyric reference of the search result.
     *
     * @return the lyric reference
     */
    public LyricRef getLyricRef() {
        return reference;
    }

    /**
     * Compares the search result to another for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than, equal
     * to, or greater than the specified object.
     *
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object
     */
    public int compareTo(SearchResult other) {
        int difference = Double.compare(getRelevance(), other.getRelevance());
        if (difference == 0) {
            return hashCode() - other.hashCode();
        } else {
            return difference;
        }
    }
}

