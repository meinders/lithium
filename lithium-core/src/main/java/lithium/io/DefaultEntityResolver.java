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

package lithium.io;

import java.io.*;

import org.xml.sax.*;

/**
 * Entity resolver for the document type definitions used by the I/O classes.
 *
 * @since 0.2
 * @version 0.9 (2006.04.14)
 * @author Gerrit Meinders
 */
public class DefaultEntityResolver implements EntityResolver
{
	/** Constructs a new entity resolver. */
	public DefaultEntityResolver()
	{
	}

	/**
	 * Checks if the system ID matches a known system ID and if so returns the
	 * appropriate input source.
	 *
	 * @param publicId the public identifier of the external entity being
	 *            referenced, or null if none was supplied.
	 * @param systemId the system identifier of the external entity being
	 *            referenced.
	 */
	public InputSource resolveEntity(String publicId, String systemId)
	{
		String name = systemId.substring( systemId.lastIndexOf( '/' ) + 1 );
		InputStream in = getClass().getResourceAsStream( "/dtds/" + name );
		if ( in == null )
		{
			if ( systemId.endsWith( ".dtd" ) )
			{
				return new InputSource( new StringReader( "" ) );
			}
			else
			{
				return null;
			}
		}
		else
		{
			return new InputSource( in );
		}
	}
}
