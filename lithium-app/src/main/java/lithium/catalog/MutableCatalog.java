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

package lithium.catalog;

/**
 * The interface for mutable lyric catalogs.
 *
 * @since 0.9
 * @version 0.9 (2006.04.15)
 * @author Gerrit Meinders
 */
public interface MutableCatalog extends Catalog {
    /**
     * This property indicates whether the catalog or anything it contains has
     * been modified.
     */
    public static final String MODIFIED_PROPERTY = "modified";

    /** The groups property. */
    public static final String GROUPS_PROPERTY = "groups";

    /**
     * Returns whether the catalog was modified since the last load or save.
     *
     * @return <code>true</code> if the catalog was modified;
     *         <code>false</code> otherwise
     */
    public boolean isModified();

    /**
     * Sets whether the catalog was modified. Setting modified to {@code false},
     * will cause modified to be set to {@code false} on all underlying groups
     * and lyrics as well.
     *
     * @param modified whether the catalog was modified
     */
    public void setModified(boolean modified);

    /**
     * Adds the given group to the catalog.
     *
     * @param group the group to be added
     * @return <code>true</code> if the catalog did not already contain the
     *         group
     */
    public boolean addGroup(Group group);

    /**
     * Removes the given group from the catalog.
     *
     * @param group the group to be removed
     * @return <code>true</code> if the catalog contained the group
     */
    public boolean removeGroup(Group group);

    /**
     * Adds the given group to the catalog as a bundle. The group will be added
     * to the most recently added {@link TypedGroup} of the {@code BUNDLES}
     * type.
     *
     * @param bundle the group to be added
     * @return <code>true</code> if the catalog did not already contain the
     *         group
     */
    public boolean addBundle(Group bundle);

    /**
     * Removes the given group from the catalog as a bundle. The group will be
     * removed from the most recently added {@link TypedGroup} of the
     * {@code BUNDLES} type.
     *
     * @param bundle the group to be removed
     * @return <code>true</code> if the catalog contained the group
     */
    public boolean removeBundle(Group bundle);

    /**
     * Adds the given group to the catalog as a category. The group will be
     * added to the most recently added {@link TypedGroup} of the
     * {@code CATEGORIES} type.
     *
     * @param category the group to be added
     * @return <code>true</code> if the catalog did not already contain the
     *         group
     */
    public boolean addCategory(Group category);

    /**
     * Removes the given group from the catalog as a category. The group will be
     * removed from the most recently added {@link TypedGroup} of the
     * {@code CATEGORIES} type.
     *
     * @param name the name of the group to be removed
     * @return <code>true</code> if the catalog contained the group
     */
    public boolean removeCategory(String name);

    /**
     * Adds the given group to the catalog as a cd. The group will be added to
     * the most recently added {@link TypedGroup} of the {@code CDS} type.
     *
     * @param cd the group to be added
     * @return <code>true</code> if the catalog did not already contain the
     *         group
     */
    public boolean addCD(Group cd);

    /**
     * Removes the given group from the catalog as a cd. The group will be
     * removed from the most recently added {@link TypedGroup} of the
     * {@code CDS} type.
     *
     * @param name the name of the group to be removed
     * @return <code>true</code> if the catalog contained the group
     */
    public boolean removeCD(String name);
}
