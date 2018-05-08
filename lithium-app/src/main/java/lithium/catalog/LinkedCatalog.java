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
 * An implementation of the Catalog interface that allows runtime linking of
 * Catalog several objects and provides access to the data in those objects as
 * if they were all in the same catalog.
 *
 * <p>
 * Currently, a LinkedCatalog containing several Catalog objects might behave
 * differently from a catalog resulting form a merger of the same catalogs.
 *
 * @author Gerrit Meinders
 */
public class LinkedCatalog implements Catalog, PropertyChangeListener {
    /** Provides support for bounds properties. */
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** The catalogs that are linked together by the LinkedCatalog */
    private Set<Catalog> catalogs;

    /**
     * Constructs a new and empty LinkedCatalog.
     */
    public LinkedCatalog() {
        catalogs = new LinkedHashSet<Catalog>();
    }

    /**
     * Constructs a new LinkedCatalog object containing only the given catalog.
     *
     * @param catalog the catalog
     */
    public LinkedCatalog(Catalog catalog) {
        this();
        add(catalog);
    }

    /**
     * Adds the given catalog to the linked catalog.
     *
     * @param catalog the catalog to be added
     */
    public void add(Catalog catalog) {
        if (catalog == null)
            throw new NullPointerException("catalog");
        assert catalog != this : "cyclic linking";
        assert (catalog instanceof LinkedCatalog ? !((LinkedCatalog) catalog)
                .contains(this) : true) : "cyclic linking (" + catalog + " / "
                + this + ")";
        assert !contains(catalog) : "duplicate addition";
        catalogs.add(catalog);
        catalog.addPropertyChangeListener(this);
    }

    /**
     * Returns whether the linked catalog contains the given catalog. This
     * method recursively calls contains on any child linked catalogs.
     *
     * @param catalog the catalog
     * @return <code>true</code> if the linked catalog contains the given
     *         catalog; otherwise <code>false</code>
     */
    public boolean contains(Catalog catalog) {
        for (Catalog child : catalogs) {
            if (catalog == child) {
                return true;
            } else {
                if (child instanceof LinkedCatalog) {
                    LinkedCatalog linkedChild = (LinkedCatalog) child;
                    if (linkedChild.contains(catalog)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Removes the given catalog from the linked catalog. This method
     * recursively calls remove on any child linked catalogs. If the catalog is
     * reference more than once in the data structure, only the first reference
     * found is removed.
     *
     * @param catalog the catalog to be removed
     * @return <code>true</code> if the given catalog was removed from the
     *         linked catalog; otherwise <code>false</code>
     */
    public boolean remove(Catalog catalog) {
        for (Catalog child : catalogs) {
            if (catalog == child) {
                child.removePropertyChangeListener(this);
                catalogs.remove(child);
                return true;
            } else {
                if (child instanceof LinkedCatalog) {
                    LinkedCatalog linkedChild = (LinkedCatalog) child;
                    if (linkedChild.remove(catalog)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Removes all catalogs from the linked catalog.
     */
    public void clear() {
        for (Catalog catalog : catalogs) {
            catalog.removePropertyChangeListener(this);
        }
        catalogs.clear();
    }

    /**
     * Returns the group with the given name.
     *
     * @param name the group name
     * @return the group
     */
    public Group getGroup(String name) {
        throw new AssertionError("not implemented");
    }

    /**
     * Returns the set of groups that contain the given lyric.
     *
     * @param lyric the lyric
     * @return the groups containing the lyric
     */
    public Set<Group> getGroups(Lyric lyric) {
        throw new AssertionError("not implemented");
    }

    /**
     * Returns the set of all groups in the catalog, excluding sub-groups.
     *
     * @return the groups contained directly by the catalog
     */
    public Set<Group> getGroups() {
        throw new AssertionError("not implemented");
    }

    /** @see Catalog#getBundle(String) */
    public Group getBundle(String name) {
        for (Catalog catalog : catalogs) {
            Group bundle = catalog.getBundle(name);
            if (bundle != null) {
                return bundle;
            }
        }
        return null;
    }

    /** @see Catalog#getBundle(String) */
    public Group getBundle(Lyric lyric) {
        for (Catalog catalog : catalogs) {
            Group bundle = catalog.getBundle(lyric);
            if (bundle != null) {
                return bundle;
            }
        }
        return null;
    }

    /** @see Catalog#getBundle(String) */
    public Set<Group> getBundles() {
        Set<Group> result = new LinkedHashSet<Group>();
        for (Catalog catalog : catalogs) {
            result.addAll(catalog.getBundles());
        }
        return result;
    }

    public Group getCategory(String name) {
        for (Catalog catalog : catalogs) {
            Group category = catalog.getCategory(name);
            if (category != null) {
                return category;
            }
        }
        return null;
    }

    /** @see Catalog#getBundle(String) */
    public Set<Group> getCategories() {
        Set<Group> result = new LinkedHashSet<Group>();
        for (Catalog catalog : catalogs) {
            result.addAll(catalog.getCategories());
        }
        return result;
    }

    /** @see Catalog#getBundle(String) */
    public Group getCD(String name) {
        for (Catalog catalog : catalogs) {
            Group cd = catalog.getCD(name);
            if (cd != null) {
                return cd;
            }
        }
        return null;
    }

    /** @see Catalog#getBundle(String) */
    public Set<Group> getCDs() {
        Set<Group> result = new LinkedHashSet<Group>();
        for (Catalog catalog : catalogs) {
            result.addAll(catalog.getCDs());
        }
        return result;
    }

    /** @see Catalog#getBundle(String) */
    public Lyric getLyric(LyricRef lyricRef) {
        for (Catalog catalog : catalogs) {
            Lyric lyric = catalog.getLyric(lyricRef);
            if (lyric != null) {
                return lyric;
            }
        }
        return null;
    }

    /**
     * Forwards property change events from the underlying catalogs to the
     * linked catalog's listeners.
     *
     * @param e the event to be forwarded
     */
    public void propertyChange(PropertyChangeEvent e) {
        pcs.firePropertyChange(e);
    }

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param listener the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Returns the property change support instance. This method is used when
     * flexible access to bound properties support is needed.
     *
     * @return the property change support
     */
    public PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }
}
