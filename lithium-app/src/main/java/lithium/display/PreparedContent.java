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
 * Provides content in a format that is suitable for efficient rendering by a
 * {@link ContentRenderer}.
 *
 * @author Gerrit Meinders
 * @since 2009-07-08
 */
public class PreparedContent
{
	private final Object value;

	private Flusher flusher;

	/**
	 * Constructs a new prepared content object. The given object is wrapped to
	 * allow it to be invalidated.
	 *
	 * @param value   the actual prepared content
	 * @param flusher used to dispose of resources held by the prepared content
	 */
	public PreparedContent( Object value, Flusher flusher )
	{
		this.value = value;
		this.flusher = flusher;
	}

	public Object getValue()
	{
		return value;
	}

	/**
	 * Returns whether the underlying prepared content is still valid, i.e. is
	 * still consistent with the represented content.
	 *
	 * <p> This implementation always returns {@code true}, assuming that the
	 * prepared content object is immutable. For mutable objects, this method
	 * should be overridden with an appropriate implementation.
	 *
	 * @return Whether the prepared content is still valid.
	 */
	public boolean isValid()
	{
		return true;
	}

	/**
	 * Disposes of any resources that the prepared content may hold.
	 */
	public void flush()
	{
		Object preparedContent = getValue();
		if ( preparedContent != null && flusher != null )
		{
			flusher.flush( preparedContent );
		}
	}
}
