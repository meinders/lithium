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

import java.beans.*;
import java.util.*;


/**
 * <p>
 * The interface for lyric catalogs. As of version 0.9, catalogs consist of
 * groups, which may in turn contain sub-groups and lyrics.
 *
 * <p>
 * The use of bundles, categories and cds is still supported and the
 * corresponding classes have been retrofitted to derive from {@link Group}.
 * However, catalogs should no longer treat these different kinds of groups in a
 * special way. That responsibility is delegated to the {@link TypedGroup}
 * class, which can be added just like any other group. It is not required that
 * the specific group class, e.g. {@link Bundle}, {@link Category} or
 * {@link CD}, matches the type of the typed group. In general it is
 * recommended that common {@code Group}s are used in stead of these classes.
 *
 * @since 0.3
 * @author Gerrit Meinders
 */
public interface Catalog {
    /**
     * Returns the group with the given name.
     *
     * @param name the group name
     * @return the group
     */
    public Group getGroup(String name);

    /**
     * Returns the set of groups that contain the given lyric.
     *
     * @param lyric the lyric
     * @return the groups containing the lyric
     */
    public Set<Group> getGroups(Lyric lyric);

    /**
     * Returns the set of all groups in the catalog, excluding sub-groups.
     *
     * @return the groups contained directly by the catalog
     */
    public Set<Group> getGroups();

    /**
     * Finds a lyric matching the given reference in one of the groups or
     * sub-groups of the catalog and returns the lyric, if found.
     *
     * @param lyricRef the reference
     * @return the lyric, or <code>null</code> if no matching lyric is found
     */
    public Lyric getLyric(LyricRef lyricRef);

    /**
     * Returns the first bundle-type group with the given name. The returned
     * group, if any, will be from the set returned by {@link #getBundles}.
     *
     * @param name the name of the group
     * @return the group, or <code>null</code> if no matching group was found
     */
    public Group getBundle(String name);

    /**
     * Returns the first bundle-type group containing the given lyric instance.
     * The returned group, if any, will be from the set returned by
     * {@link #getBundles}.
     *
     * @param lyric the lyric
     * @return the group, or <code>null</code> if no matching group was found
     */
    public Group getBundle(Lyric lyric);

    /**
     * Returns the set of bundle-type groups. The set consists of all groups
     * contained directly (that is, not in a sub-group) in the most recently
     * added {@link TypedGroup} of the {@code BUNDLES} type.
     *
     * @return the set of bundle groups
     */
    public Set<Group> getBundles();

    /**
     * Returns the first category-type group with the given name. The returned
     * group, if any, will be from the set returned by {@link #getCategories}.
     *
     * @param name the name of the group
     * @return the group, or <code>null</code> if no matching group was found
     */
    public Group getCategory(String name);

    /**
     * Returns the set of category-type groups. The set consists of all groups
     * contained directly (that is, not in a sub-group) in the most recently
     * added {@link TypedGroup} of the {@code CATEGORIES} type.
     *
     * @return the set of category groups
     */
    public Set<Group> getCategories();

    /**
     * Returns the first cd-type group with the given name. The returned group,
     * if any, will be from the set returned by {@link #getCDs}.
     *
     * @param name the name of the group
     * @return the group, or <code>null</code> if no matching group was found
     */
    public Group getCD(String name);

    /**
     * Returns the set of cd-type groups. The set consists of all groups
     * contained directly (that is, not in a sub-group) in the most recently
     * added {@link TypedGroup} of the {@code CDS} type.
     *
     * @return the set of cd groups
     */
    public Set<Group> getCDs();

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param listener the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returns the property change support instance. This method is used when
     * flexible access to bound properties support is needed.
     *
     * @return the property change support
     */
    public PropertyChangeSupport getPropertyChangeSupport();
}
