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

package lithium.display;

import java.beans.*;
import java.util.*;

/**
 * A container that keeps track of a number of plugins and informs listeners
 * when plugins get added or removed.
 *
 * @version 0.9x (2005.08.25)
 * @author Gerrit Meinders
 */
public class GUIPluginManager {
	public static final String PLUGINS_PROPERTY = "plugins";

	private Set<GUIPlugin> plugins;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public GUIPluginManager() {
		plugins = new LinkedHashSet<GUIPlugin>();
	}

	public void addPlugin(GUIPlugin plugin) {
		if (plugin == null) {
			throw new NullPointerException("plugin");
		}
		plugins.add(plugin);
		pcs.firePropertyChange(PLUGINS_PROPERTY, null, plugin);
	}

	public void removePlugin(GUIPlugin plugin) {
		if (plugin == null) {
			throw new NullPointerException("plugin");
		}
		plugins.remove(plugin);
		pcs.firePropertyChange(PLUGINS_PROPERTY, plugin, null);
	}

	public Set<GUIPlugin> getPlugins() {
		return Collections.unmodifiableSet(plugins);
	}

	/**
	 * Add a PropertyChangeListener to the listener list. The listener is
	 * registered for all properties. The same listener object may be added more
	 * than once, and will be called as many times as it is added. If
	 * <code>listener</code> is null, no exception is thrown and no action is
	 * taken.
	 *
	 * @param listener The PropertyChangeListener to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Remove a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties. If
	 * <code>listener</code> was added more than once to the same event source,
	 * it will be notified one less time after being removed. If
	 * <code>listener</code> is null, or was never added, no exception is thrown
	 * and no action is taken.
	 *
	 * @param listener The PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
}
