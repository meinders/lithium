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

package lithium;

import com.github.meinders.common.*;

/**
 * Provides access to the currently loaded resource strings.
 *
 * @since 0.7
 * @version 0.9 (2006.02.22)
 * @author Gerrit Meinders
 */
public class Resources {
    /** The currently loaded resource utilities. */
    private static ResourceUtilities resources;

    /**
     * Returns the resource utilities to access the currently loaded resources.
     *
     * @return the resource utilities
     */
    public static ResourceUtilities get() {
        assert resources != null : "Resources not loaded yet.";
        return resources;
    }

    /**
     * Returns a wrapper of the currently loaded resources that automatically
     * uses the specified prefix.
     *
     * @param prefix string to be prepended to resource keys
     *
     * @return resource utilities
     */
    public static ResourceUtilities get(String prefix) {
        return new ResourceUtilities(get(), prefix);
    }

    /**
     * Sets the resource utilities to access the currently loaded resources.
     *
     * @param resources the resource utilities
     */
    public static void set(ResourceUtilities resources) {
        assert resources != null : "Invalid resources: " + resources;
        Resources.resources = resources;
    }
}

