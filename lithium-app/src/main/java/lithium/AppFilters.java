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

import javax.swing.*;

import com.github.meinders.common.*;
import lithium.editor.*;
import lithium.io.*;

/**
 * FIXME Need comment
 *
 * @author G. Meinders
 */
public class AppFilters
{
	public static void registerFilters()
	{
		FilterManager filterManager = FilterManager.getInstance();

		/**
		 * Lyrics.
		 */
		filterManager.addFilter( FilterType.CATALOG, new FilterImpl( new FileFilter[] {
		new XMLNamespaceFileFilter(
		Resources.get().getString( "catalog" ), CatalogIO.NAMESPACE ),
		new XMLFileFilter( Resources.get().getString( "catalog" ), CatalogIO.PUBLIC_ID )
		}, new ImageIcon( FilterManager.class.getResource( "/images/catalog48.gif" ), Resources.get().getString( "catalog" ) ) ) );

		/**
		 * Playlists.
		 */
		filterManager.addFilter( FilterType.PLAYLIST, new FilterImpl( new FileFilter[] {
		new XMLNamespaceFileFilter(
		Resources.get().getString( "playlist" ),
		PlaylistIO.LITHIUM_PLAYLIST_NS_URI ),
		new XMLFileFilter( Resources.get().getString( "playlist" ),
		                   PlaylistIO.LEGACY_PUBLIC_ID )
		}, new ImageIcon(
		FilterManager.class.getResource( "/images/playlist48.gif" ),
		Resources.get().getString( "playlist" ) ) ) );

		/**
		 * PowerPoint presentations.
		 */
		filterManager.addFilter( FilterType.PPT, new FilterImpl( new FileFilter[] {
		new ExtensionFileFilter( Resources.get().getString(
		"presentation.ppt" ), "ppt" )
		}, null ) );

		/**
		 * OpenOffice presentations.
		 */
		filterManager.addFilter( FilterType.ODP, new FilterImpl( new FileFilter[] {
		new ExtensionFileFilter( Resources.get().getString(
		"presentation.odp" ), "odp" )
		}, null ) );

		/**
		 * Books.
		 */
		filterManager.addFilter( FilterType.COLLECTION, new FilterImpl( new FileFilter[] {
		new BookFileFilter( Resources.get().getString( "books.collection" ) )
		}, null ) );
	}
}
