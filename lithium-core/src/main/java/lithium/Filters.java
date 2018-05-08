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
 * FIXME Need comment
 *
 * @author G. Meinders
 */
public class Filters
{
	public static void registerFilters()
	{
		FilterManager filterManager = FilterManager.getInstance();

		/**
		 * Utilities.
		 */
		filterManager.addFilter( FilterType.UTILITY, new FilterImpl( new FileFilter[] {
		new ExtensionFileFilter( Resources.get().getString(
		"filter.executable" ), "exe" )
		}, null ) );

		/**
		 * Audio and video files.
		 */
		filterManager.addFilter( FilterType.MEDIA_FILES, new FilterImpl( new FileFilter[] {
		new ExtensionFileFilter( Resources.get().getString(
		"media.mediaFiles" ), new String[] {
		"avi", "mpg", "mpeg", "wmv", "ogm", "mkv", "mp4", "wav",
		"mp3", "ogg"
		} )
		}, null ) );

		/**
		 * Plain-text files.
		 */
		filterManager.addFilter( FilterType.PLAIN_TEXT, new FilterImpl( new FileFilter[] {
		new ExtensionFileFilter(
		Resources.get().getString( "media.plainText" ), "txt" )
		}, null ) );
	}
}
