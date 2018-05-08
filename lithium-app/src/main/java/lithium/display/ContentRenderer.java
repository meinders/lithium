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

/**
 * Provides the means to visualize content objects, which may need to be
 * prepared before rendering. Sub-interfaces define specific rendering methods.
 *
 * @author Gerrit Meinders
 */
public interface ContentRenderer
{
	/**
	 * Returns whether the renderer can render the given content.
	 *
	 * @param content the content to be checked
	 *
	 * @return whether the content can be rendered by this renderer
	 */
	boolean accept(Object content);

	/**
	 * Prepares the given content for rendering, returning an opaque object that
	 * may be used to perform the actual rendering. The prepared content can be
	 * re-used to render the object or any part thereof any number of times.
	 *
	 * <p>
	 * The preparation of content may be performed asynchronously. The
	 * {@link #ready} method should be used to check if the process of preparing
	 * the content is completed.
	 *
	 * @param content the content to be prepared
	 *
	 * @return an object representing a renderable version of the content
	 *
	 * @throws IllegalArgumentException if {@link #accept} returns
	 *             <code>false</code> for the given content
	 */
	PreparedContent prepare(Object content);

	/**
	 * Returns whether the given prepared content, produced by {@link #prepare},
	 * is ready to be used for rendering. Even if this method returns
	 * <code>false</code>, it is still safe to call a render method, though the
	 * resulting rendering may be incomplete.
	 *
	 * @param prepared the prepared content to be checked
	 *
	 * @return whether the given prepared content is ready to be rendered
	 */
	boolean ready(Object prepared);

	/**
	 * Whether the view should render a default background when this renderer is
	 * active. Some content is best viewed without a background, i.e. on a black
	 * background.
	 *
	 * @return whether the view should provide a background
	 */
	boolean isViewBackgroundVisible();
}
