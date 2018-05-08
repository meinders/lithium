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

import java.beans.*;
import java.io.*;
import java.util.*;

import lithium.io.*;

/**
 * This class manages the active playlist.
 *
 * XXX: refactor to singleton
 *
 * @version 0.9 (2006.02.22)
 * @author Gerrit Meinders
 */
public class PlaylistManager
{
	public static final String PLAYLIST_PROPERTY = "playlist";

	public static Playlist playlist = null;

	/** Provides support for bounds properties. */
	private static PropertyChangeSupport pcs = new PropertyChangeSupport(
	        new PlaylistManager());

	private PlaylistManager()
	{
		// private constructor for static-only class
	}

	public static Playlist getPlaylist()
	{
		if (playlist == null)
		{
			Config config = ConfigManager.getConfig();

			playlist = new Playlist();

			Set<File> texts = config.getLoadOnStartupFiles(FilterManager.getFilters( FilterType.PLAIN_TEXT));
			for (File file : texts)
			{
				System.out.println("Loading plain text: " + file);
				try
				{
					playlist.add(new PlaylistItem(TextInput.readPlainText(file)));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			Set<File> playlists = config.getLoadOnStartupFiles(FilterManager.getFilters( FilterType.PLAYLIST));
			for (File file : playlists)
			{
				System.out.println("Loading playlist: " + file);
				Playlist startupPlaylist;
				try
				{
					startupPlaylist = PlaylistIO.read(file);
					for (PlaylistItem item : startupPlaylist.getItems())
					{
						playlist.add(item);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return playlist;
	}

	public static void setPlaylist(Playlist playlist)
	{
		Playlist oldValue = PlaylistManager.playlist;
		PlaylistManager.playlist = playlist;
		pcs.firePropertyChange(PLAYLIST_PROPERTY, oldValue, playlist);
	}

	public static void addPropertyChangeListener(PropertyChangeListener l)
	{
		pcs.addPropertyChangeListener(l);
	}

	public static void removePropertyChangeListener(PropertyChangeListener l)
	{
		pcs.removePropertyChangeListener(l);
	}

	public static PropertyChangeSupport getPropertyChangeSupport()
	{
		return pcs;
	}
}
