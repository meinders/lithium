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

import lithium.*;

/**
 * A group of lyrics.
 *
 * @see ContainerGroup
 * @see ReferenceGroup
 *
 * @version 0.9 (2006.02.20)
 * @author Gerrit Meinders
 */
public abstract class Group implements Comparable<Group>, PropertyChangeListener {
    /**
     * This property indicates whether the group of any of its contents has been
     * modified.
     */
    public static final String MODIFIED_PROPERTY = "modified";

    /** The property of the lyrics in this group. */
    public static final String LYRICS_PROPERTY = "lyrics";

    /** The property of the sub-groups in this group. */
    public static final String GROUPS_PROPERTY = "groups";

    /**
     * Creates a bundle, a group that can contain lyrics.
     *
     * @return the created {@link Bundle}
     */
    public static Group createBundle() {
        String name = Resources.get().getString("bundle.new.name");
        String version = Resources.get().getString("bundle.new.version", new Date());
        return new Bundle(name, version);
        // return new ContainerGroup("Bundle.displayName", name, version);
    }

    /**
     * Creates a category, a group that references lyrics.
     *
     * @return the created {@link Category}
     */
    public static Group createCategory() {
        String name = Resources.get().getString("category.new.name");
        String version = Resources.get().getString("category.new.version", new Date());
        return new Category(name, version);
        // return new ReferenceGroup("Category.displayName", name, version);
    }

    /**
     * Creates a cd, a group that references lyrics.
     *
     * @return the created {@link CD}
     */
    public static CD createCD() {
        String name = Resources.get().getString("cd.new.name");
        String version = Resources.get().getString("cd.new.version", new Date());
        return new CD(name, version);
        // return new ReferenceGroup("CD.displayName", name, version);
    }

    /** Provides support for bounds properties. */
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * The name of the resource that contains the group's display name format.
     */
    private String displayName;

    /** The name of the group. */
    private String name;

    /** The version of the group. */
    private String version;

    /** Indicates whether the group or any of its contents has been modified. */
    private boolean modified = false;

    /** The sub-groups contained in this group. */
    private SortedSet<Group> groups;

    /**
     * Constructs a new group with the given attributes.
     *
     * @param displayName the name of the resource that contains the group's
     *        display name format
     * @param name the name of the group
     * @param version the group's version
     */
    public Group(String displayName, String name, String version) {
        groups = new TreeSet<Group>();

        this.displayName = displayName;
        setName(name);
        setVersion(version);
    }

    /**
     * Returns the display name of the group. Note that this is a resource
     * identifier. For normal use simply call {@link #toString()}.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the name of the group.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the group.
     *
     * @param name the name to be set
     */
    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.name = name;
        setModified(true);
    }

    /**
     * Returns the version of the group.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the group.
     *
     * @param version the version to be set
     */
    public void setVersion(String version) {
        if (version == null) {
            throw new NullPointerException("version");
        }
        this.version = version;
        setModified(true);
    }

    /**
     * Returns whether the group or any of its contents has been modified.
     *
     * @return <code>true</code> if the group was modified; <code>false</code>
     *         otherwise
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Sets whether the group or any of its contents has been modified.
     *
     * @param modified whether the group was modified
     */
    public void setModified(boolean modified) {
        if (this.modified != modified) {
            boolean oldValue = this.modified;
            this.modified = modified;
            if (!modified) {
                for (Group group : groups) {
                    if (group.isModified()) {
                        group.setModified(false);
                    }
                }

                /*
                 * NOTE: The following works, because all related groups and
                 * lyrics are always saved a the same time. So even though
                 * referenced lyrics will also be unmodified, this causes no
                 * problems, because they would have been unmodified anyway by
                 * the groups containing them.
                 */
                for (Lyric lyric : getLyrics()) {
                    if (lyric.isModified()) {
                        lyric.setModified(false);
                    }
                }
            }
            pcs.firePropertyChange(MODIFIED_PROPERTY, oldValue, modified);
        }
    }

    /**
     * Adds a sub-group to the group and notifies any registered listeners.
     *
     * @param group the group to be added
     */
    public void addGroup(Group group) {
        if (group == null) {
            throw new NullPointerException("group");
        }
        if (groups.add(group)) {
            group.addPropertyChangeListener(this);
            setModified(true);
            pcs.firePropertyChange(GROUPS_PROPERTY, null, null);
        }
    }

    /**
     * Removes a sub-group from the group and notifies any registered listeners.
     *
     * @param group the group to be removed
     */
    public void removeGroup(Group group) {
        if (group == null) {
            throw new NullPointerException("group");
        }
        if (groups.remove(group)) {
            group.removePropertyChangeListener(this);
            setModified(true);
            pcs.firePropertyChange(GROUPS_PROPERTY, null, null);
        }
    }

    /**
     * Removes all of the group's sub-groups and notifies any registered
     * listeners.
     */
    public void removeGroups() {
        if (!groups.isEmpty()) {
            for (Group group : getGroups()) {
                group.removePropertyChangeListener(this);
            }
            groups.clear();
            setModified(true);
            pcs.firePropertyChange(GROUPS_PROPERTY, null, null);
        }
    }

    /**
     * Returns the sub-groups of the group.
     *
     * @return the set of sub-groups
     */
    public Set<Group> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    /**
     * Returns the sub-group with the given name.
     *
     * @param name the name of the group
     *
     * @return the group, if any.
     */
    public Group getGroup(String name) {
        for (Group group : groups) {
            if ((name == null) ? (group.getName() == null) : name.equals(group.getName())) {
                return group;
            }
        }
        return null;
    }

    /**
     * Returns the lyric with the given number. If there are multiple lyrics,
     * any of those may be returned, but the returned lyric should be consistent
     * over multiple calls.
     *
     * @param number the lyric's number
     * @return the lyric, or <code>null</code> if there is no lyric with the
     *         given number
     */
    public abstract Lyric getLyric(int number);

    /**
     * Returns all of the lyrics in this group. This does not include the lyrics
     * from sub-groups.
     *
     * @return the lyrics
     */
    public abstract Collection<Lyric> getLyrics();

    /**
     * Adds the given lyric to the group by some implementation-specific means.
     *
     * @param lyric the lyric to be added
     * @return the lyric that was replaced, if any
     */
    protected abstract Lyric addLyricImpl(Lyric lyric);

    /**
     * Removes the given lyric from the group by some implementation-specific
     * means.
     *
     * @param lyric the lyric to be removed
     * @return whether the lyric existed in the group before removal
     */
    protected abstract boolean removeLyricImpl(Lyric lyric);

    /**
     * Adds the given lyric to the group, notifies any registered listeners and
     * updates the modified property.
     *
     * @param lyric the lyric to be added
     */
    public void addLyric(Lyric lyric) {
        Lyric oldValue = addLyricImpl(lyric);
        if (oldValue != lyric) {
            setModified(true);
            pcs.firePropertyChange(LYRICS_PROPERTY, null, null);
        }
    }

    /**
     * Removes the given lyric from the group.
     *
     * @param lyric the lyric to be removed
     */
    public void removeLyric(Lyric lyric) {
        if (removeLyricImpl(lyric)) {
            setModified(true);
            pcs.firePropertyChange(LYRICS_PROPERTY, null, null);
        }
    }

    /**
     * Removes the lyric with the given number from the group. If there are
     * multiple lyrics with the given number, only one is removed.
     *
     * @param number the number of the lyric to be removed
     */
    public void removeLyric(int number) {
        Lyric lyric = getLyric(number);
        if (lyric != null) {
            removeLyric(lyric);
        }
    }

    /**
     * Removes all lyrics from the group.
     */
    public void removeLyrics() {
        ArrayList<Lyric> lyrics = new ArrayList<Lyric>(getLyrics());
        if (!lyrics.isEmpty()) {
            for (Lyric lyric : lyrics) {
                removeLyricImpl(lyric);
            }
            setModified(true);
            pcs.firePropertyChange(LYRICS_PROPERTY, null, null);
        }
    }

    /**
     * Returns the display name of the group.
     *
     * @return the display name
     */
    @Override
    public String toString() {
        return Resources.get().getString(displayName, name, version, version.length());
    }

    /**
     * Compares the group to another group by name only.
     *
     * @param other the other group
     */
    public int compareTo(Group other) {
        return getName().compareTo(other.getName());
    }

    /**
     * Returns whether the given object is equal to the group, based on its name
     * (if it's a Group).
     *
     * @param o the object
     * @return <code>true</code> if the given object is a group with the same
     *         name; <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Group) {
            Group other = (Group) o;
            return getName().equals(other.getName());
        } else {
            return false;
        }
    }

    /**
     * Handles property changes from sub-groups contained in the group.
     *
     * This provides automatic propagation of modifications upward in the group
     * hierarchy. Propagation of 'unmodification' (e.g. loading or saving) is
     * handled by {@link #setModified(boolean)}.
     */
    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (property == Group.MODIFIED_PROPERTY) {
            boolean modified = (Boolean) e.getNewValue();
            if (modified) {
                setModified(true);
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    public boolean hasListeners(String propertyName) {
        return pcs.hasListeners(propertyName);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}
