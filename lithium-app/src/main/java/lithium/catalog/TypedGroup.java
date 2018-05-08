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
 * An extension of the container group class that allows for special types of
 * group containers.
 *
 * @author Gerrit Meinders
 */
public class TypedGroup extends ContainerGroup {
    /**
     * An enumeration of the different types of groups.
     */
    public static enum GroupType {
        /** A group containing bundles. */
        BUNDLES {
            @Override
            protected String getDisplayName() {
                return "Bundles.displayName";
            }
        },

        /** A group containing categories. */
        CATEGORIES {
            @Override
            protected String getDisplayName() {
                return "Categories.displayName";
            }
        },

        /** A group containing cds. */
        CDS {
            @Override
            protected String getDisplayName() {
                return "CDs.displayName";
            }
        };

        /**
         * Returns the appropriate display name for the group type.
         *
         * @return the display name, as a resource name
         */
        protected abstract String getDisplayName();
    }

    /** The type of the group. */
    private GroupType type;

    /**
     * Constructs a new typed group of the given type. The name and version of
     * the group are set to the appropriate default values for the specified
     * type.
     *
     * @param type the type of group
     */
    public TypedGroup(GroupType type) {
        this(type, null, null);
    }

    /**
     * Constructs a new typed group of the given type, name and version.
     *
     * @param type the type of group
     * @param name the name
     * @param version the version
     */
    public TypedGroup(GroupType type, String name, String version) {
        super(type.getDisplayName(), name == null ? type.getDisplayName()
                : name, version == null ? "" : version);
        this.type = type;
    }

    /**
     * Returns the type of the group.
     *
     * @return the group type
     */
    public GroupType getType() {
        return type;
    }
}
