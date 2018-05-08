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

import lithium.catalog.TypedGroup.*;

/**
 * Provides the default implementation of a mutable catalog.
 *
 * @since 0.4
 * @author Gerrit Meinders
 */
public class DefaultCatalog implements MutableCatalog, PropertyChangeListener
{
	/** The group instance used to implement the group-related methods. */
	private Group contents;

	/** A map of typed groups, which are used to implement legacy methods. */
	private Map<TypedGroup.GroupType, TypedGroup> typedGroups;

	/** Provides support for bounds properties. */
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/** Constructs a new mutable catalog. */
	public DefaultCatalog()
	{
		contents = new ContainerGroup("", "", "");
		contents.addPropertyChangeListener(this);
		typedGroups = new HashMap<TypedGroup.GroupType, TypedGroup>();
	}

	/**
	 * Returns the group with the given name.
	 *
	 * @param name the group name
	 * @return the group
	 */
	public Group getGroup(String name)
	{
		return getGroup(name, contents);
	}

	/**
	 * Finds the first occurance of a group with the given name using a depth
	 * first search on a group hierarchy.
	 *
	 * @param name the group name
	 * @param group the group to search in
	 * @return the group found
	 */
	private Group getGroup(String name, Group group)
	{
		if (group.getName().equals(name))
		{
			return group;
		}
		else
		{
			for (Group subGroup : group.getGroups())
			{
				Group result = getGroup(name, subGroup);
				if (result != null)
				{
					return result;
				}
			}
			return null;
		}
	}

	/**
	 * Returns the set of groups that contain the given lyric, including any
	 * sub-groups that contain the lyric.
	 *
	 * @param lyric the lyric
	 * @return the groups containing the lyric
	 */
	public Set<Group> getGroups(Lyric lyric)
	{
		Set<Group> groups = new LinkedHashSet<Group>();
		for (Group group : contents.getGroups())
		{
			getGroups(group, lyric, groups);
		}
		return groups;
	}

	/**
	 * Recursively searches through the group hierarchy of the given group,
	 * adding any groups that contain the given lyric to the given set of
	 * groups.
	 *
	 * @param group the group to search in
	 * @param lyric the lyric to search for
	 * @param found the set of groups found
	 * @return the set of groups found
	 */
	private Set<Group> getGroups(Group group, Lyric lyric, Set<Group> found)
	{
		if (!found.contains(group))
		{
			if (group.getLyrics().contains(lyric))
			{
				found.add(group);
			}
			for (Group subGroup : group.getGroups())
			{
				getGroups(subGroup, lyric, found);
			}
		}
		return found;
	}

	/**
	 * Returns the set of all groups in the catalog, excluding any sub-groups.
	 *
	 * @return the groups contained directly by the catalog
	 */
	public Set<Group> getGroups()
	{
		return contents.getGroups();
	}

	/**
	 * Returns whether the catalog was modified since the last load or save.
	 *
	 * @return <code>true</code> if the catalog was modified; <code>false</code>
	 *         otherwise
	 */
	public boolean isModified()
	{
		return contents.isModified();
	}

	/**
	 * Sets whether the catalog was modified since the last load or save.
	 *
	 * @param modified whether the catalog was modified
	 */
	public void setModified(boolean modified)
	{
		contents.setModified(modified);
	}

	/**
	 * Adds the given group to the catalog.
	 *
	 * @param group the group to be added
	 * @return <code>true</code> if the catalog did not already contain the
	 *         group
	 */
	public boolean addGroup(Group group)
	{
		boolean exists = contents.getGroups().contains(group);
		if (!exists)
		{
			contents.addGroup(group);
			if (group instanceof TypedGroup)
			{
				TypedGroup typedGroup = (TypedGroup) group;
				typedGroups.put(typedGroup.getType(), typedGroup);
			}
		}
		return !exists;
	}

	/**
	 * Removes the given group from the catalog.
	 *
	 * @param group the group to be removed
	 * @return <code>true</code> if the catalog contained the group
	 */
	public boolean removeGroup(Group group)
	{
		boolean exists = contents.getGroups().contains(group);
		if (exists)
		{
			contents.removeGroup(group);
			if (group instanceof TypedGroup)
			{
				TypedGroup typedGroup = (TypedGroup) group;
				TypedGroup.GroupType type = typedGroup.getType();
				typedGroups.remove(type);
				TypedGroup similarTypedGroup = findTypedGroup(type);
				if (similarTypedGroup != null)
				{
					typedGroups.put(type, similarTypedGroup);
				}
			}
		}
		return exists;
	}

	/**
	 * Returns the active typed group of the given type.
	 *
	 * @param type the type of group
	 * @return a typed group of the given type, or <code>null</code> if no such
	 *         group was found
	 */
	protected TypedGroup getTypedGroup(TypedGroup.GroupType type)
	{
		TypedGroup group = typedGroups.get(type);
		if (group == null)
		{
			group = new TypedGroup(type);
			addGroup(group);
		}
		return group;
	}

	/**
	 * Finds a typed group with the given type amongst the groups contained in
	 * the catalog.
	 *
	 * @return a typed group of the given type, or <code>null</code> if no such
	 *         group was found
	 */
	private TypedGroup findTypedGroup(TypedGroup.GroupType type)
	{
		for (Group group : getGroups())
		{
			if (group instanceof TypedGroup)
			{
				TypedGroup typedGroup = (TypedGroup) group;
				if (typedGroup.getType() == type)
				{
					return typedGroup;
				}
			}
		}
		return null;
	}

	/**
	 * Finds a lyric matching the given reference in one of the groups or
	 * sub-groups of the catalog and returns the lyric, if found.
	 *
	 * @param lyricRef the reference
	 * @return the lyric, or <code>null</code> if no matching lyric is found
	 */
	public Lyric getLyric(LyricRef lyricRef)
	{
		Group group = getGroup(lyricRef.getBundle());
		if (group == null)
		{
			return null;
		}
		else
		{
			return group.getLyric(lyricRef.getNumber());
		}
	}

	/**
	 * Returns the first bundle-type group with the given name. The returned
	 * group, if any, will be from the set returned by {@link #getBundles}.
	 *
	 * @param name the name of the group
	 * @return the group, or <code>null</code> if no matching group was found
	 */
	public Group getBundle(String name)
	{
		for (Group bundle : getBundles())
		{
			if (bundle.getName().equals(name))
			{
				return bundle;
			}
		}
		return null;
	}

	/**
	 * Returns the first bundle-type group containing the given lyric instance.
	 * The returned group, if any, will be from the set returned by
	 * {@link #getBundles}.
	 *
	 * @param lyric the lyric
	 * @return the group, or <code>null</code> if no matching group was found
	 */
	public Group getBundle(Lyric lyric)
	{
		for (Group bundle : getBundles())
		{
			if (bundle.getLyrics().contains(lyric))
			{
				return bundle;
			}
		}
		return null;
	}

	/**
	 * Returns the set of bundle-type groups. The set consists of all groups
	 * contained directly (that is, not in a sub-group) in the most recently
	 * added {@link TypedGroup} of the {@code BUNDLES} type.
	 *
	 * @return the set of bundle groups
	 */
	public Set<Group> getBundles()
	{
		return getTypedGroup(GroupType.BUNDLES).getGroups();
	}

	/**
	 * Adds the given group to the catalog as a bundle. The group will be added
	 * to the most recently added {@link TypedGroup} of the {@code BUNDLES}
	 * type.
	 *
	 * @param bundle the group to be added
	 * @return <code>true</code> if the catalog did not already contain the
	 *         group
	 */
	public boolean addBundle(Group bundle)
	{
		Group group = getTypedGroup(GroupType.BUNDLES);
		boolean exists = group.getGroups().contains(bundle);
		if (!exists)
		{
			group.addGroup(bundle);
		}
		return !exists;
	}

	/**
	 * Removes the given group from the catalog as a bundle. The group will be
	 * removed from the most recently added {@link TypedGroup} of the {@code
	 * BUNDLES} type.
	 *
	 * @param bundle the group to be removed
	 * @return <code>true</code> if the catalog contained the group
	 */
	public boolean removeBundle(Group bundle)
	{
		if (bundle == null)
		{
			throw new NullPointerException("bundle");
		}

		Group group = getTypedGroup(GroupType.BUNDLES);
		boolean exists = group.getGroups().contains(bundle);
		if (exists)
		{
			group.removeGroup(bundle);
		}
		return exists;
	}

	/**
	 * Returns the first category-type group with the given name. The returned
	 * group, if any, will be from the set returned by {@link #getCategories}.
	 *
	 * @param name the name of the group
	 * @return the group, or <code>null</code> if no matching group was found
	 */
	public Group getCategory(String name)
	{
		for (Group category : getCategories())
		{
			if (category.getName().equals(name))
			{
				return category;
			}
		}
		return null;
	}

	/**
	 * Returns the set of category-type groups. The set consists of all groups
	 * contained directly (that is, not in a sub-group) in the most recently
	 * added {@link TypedGroup} of the {@code CATEGORIES} type.
	 *
	 * @return the set of category groups
	 */
	public Set<Group> getCategories()
	{
		return getTypedGroup(GroupType.CATEGORIES).getGroups();
	}

	/**
	 * Adds the given group to the catalog as a category. The group will be
	 * added to the most recently added {@link TypedGroup} of the {@code
	 * CATEGORIES} type.
	 *
	 * @param category the group to be added
	 * @return <code>true</code> if the catalog did not already contain the
	 *         group
	 */
	public boolean addCategory(Group category)
	{
		Group group = getTypedGroup(GroupType.CATEGORIES);
		boolean exists = group.getGroups().contains(category);
		if (!exists)
		{
			group.addGroup(category);
		}
		return !exists;
	}

	/**
	 * Removes the given group from the catalog as a category. The group will be
	 * removed from the most recently added {@link TypedGroup} of the {@code
	 * CATEGORIES} type.
	 *
	 * @param name the name of the group to be removed
	 * @return <code>true</code> if the catalog contained the group
	 */
	public boolean removeCategory(String name)
	{
		Group typedGroup = getTypedGroup(GroupType.CATEGORIES);
		Group group = getCategory(name);
		if (group != null)
		{
			typedGroup.removeGroup(group);
		}
		return group != null;
	}

	/**
	 * Returns the first cd-type group with the given name. The returned group,
	 * if any, will be from the set returned by {@link #getCDs}.
	 *
	 * @param name the name of the group
	 * @return the group, or <code>null</code> if no matching group was found
	 */
	public Group getCD(String name)
	{
		for (Group cd : getCDs())
		{
			if (cd.getName().equals(name))
			{
				return cd;
			}
		}
		return null;
	}

	/**
	 * Returns the set of cd-type groups. The set consists of all groups
	 * contained directly (that is, not in a sub-group) in the most recently
	 * added {@link TypedGroup} of the {@code CDS} type.
	 *
	 * @return the set of cd groups
	 */
	public Set<Group> getCDs()
	{
		return getTypedGroup(GroupType.CDS).getGroups();
	}

	/**
	 * Adds the given group to the catalog as a cd. The group will be added to
	 * the most recently added {@link TypedGroup} of the {@code CDS} type.
	 *
	 * @param cd the group to be added
	 * @return <code>true</code> if the catalog did not already contain the
	 *         group
	 */
	public boolean addCD(Group cd)
	{
		Group group = getTypedGroup(GroupType.CDS);
		boolean exists = group.getGroups().contains(cd);
		if (!exists)
		{
			group.addGroup(cd);
		}
		return !exists;
	}

	/**
	 * Removes the given group from the catalog as a cd. The group will be
	 * removed from the most recently added {@link TypedGroup} of the {@code
	 * CDS} type.
	 *
	 * @param name the name of the group to be removed
	 * @return <code>true</code> if the catalog contained the group
	 */
	public boolean removeCD(String name)
	{
		Group typedGroup = getTypedGroup(GroupType.CDS);
		Group group = getCD(name);
		if (group != null)
		{
			typedGroup.removeGroup(group);
		}
		return group != null;
	}

	public static void merge(MutableCatalog destination, Catalog source)
	{
		if (source == destination)
		{
			return;
		}

		for (Group srcGroup : source.getGroups())
		{
			Group destGroup = destination.getGroup(srcGroup.getName());
			if (destGroup == null)
			{
				destination.addGroup(srcGroup);
			}
			else
			{
				merge(destGroup, srcGroup);
			}
		}
	}

	public static void merge(Group destination, Group source)
	{
		if (source == destination)
		{
			return;
		}

		merge(destination, source, false);
	}

	public static void merge(Group destination, Group source,
	        boolean discardOlderVersions)
	{
		if (source == destination)
		{
			return;
		}

		int difference = source.getVersion().compareTo(destination.getVersion());
		if (discardOlderVersions && difference > 0)
		{
			System.out.println("WARNING: replacing group with newer version");
			System.out.println("destination=" + destination);
			System.out.println("source=" + source);

			// newer version -> replace old version completely
			destination.setVersion(source.getVersion());
			destination.removeLyrics();
			for (Lyric lyric : source.getLyrics())
			{
				destination.addLyric(lyric);
			}

			// replace all groups
			destination.removeGroups();
			for (Group group : source.getGroups())
			{
				destination.addGroup(group);
			}

		}
		else if (difference < 0)
		{
			// older version -> add non-existant lyrics
			for (Lyric srcLyric : source.getLyrics())
			{
				Lyric destLyric = destination.getLyric(srcLyric.getNumber());
				if (destLyric == null)
				{
					destination.addLyric(srcLyric);
				}
				else
				{
					// keep existing version
				}

				// merge groups that already exist
				for (Group group : source.getGroups())
				{
					Group destGroup = getGroup(destination, group.getName());
					if (destGroup != null)
					{
						merge(destGroup, group, discardOlderVersions);
					}
				}
			}

		}
		else
		{
			// same version -> add and replace lyrics
			for (Lyric lyric : source.getLyrics())
			{
				destination.addLyric(lyric);
			}

			// merge existing groups and add new groups
			for (Group group : source.getGroups())
			{
				Group destGroup = getGroup(destination, group.getName());
				if (destGroup == null)
				{
					destination.addGroup(group);
				}
				else
				{
					merge(destGroup, group, discardOlderVersions);
				}
			}
		}
	}

	private static Group getGroup(Group parent, String name)
	{
		for (Group group : parent.getGroups())
		{
			if (name.equals(group.getName()))
			{
				return group;
			}
		}
		return null;
	}

	/**
	 * Handles property changes from underlying groups.
	 *
	 * @param e the event describing the change
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		final String name = e.getPropertyName();
		if (name == Group.MODIFIED_PROPERTY)
		{
			pcs.firePropertyChange(//
			        MutableCatalog.MODIFIED_PROPERTY, null, null);
		}
		else if (name == Group.GROUPS_PROPERTY)
		{
			pcs.firePropertyChange(MutableCatalog.GROUPS_PROPERTY, null, null);
		}
	}

	/**
	 * Add a PropertyChangeListener to the listener list.
	 *
	 * @param listener the PropertyChangeListener to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Removes a PropertyChangeListener from the listener list.
	 *
	 * @param listener the PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
	}

	/**
	 * Returns the property change support instance. This method is used when
	 * flexible access to bound properties support is needed.
	 *
	 * @return the property change support
	 */
	public PropertyChangeSupport getPropertyChangeSupport()
	{
		return pcs;
	}
}
