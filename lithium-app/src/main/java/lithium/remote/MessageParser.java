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

package lithium.remote;

import java.io.*;

public class MessageParser
{
	public static Message parse(DataInput in) throws IOException
	{
		String name = in.readUTF();

		if (ContentMessage.NAME.equals(name))
		{
			return new ContentMessage(in);
		}
		else if (StateRequestMessage.NAME.equals(name))
		{
			return new StateRequestMessage();
		}
		else if (RecorderStatusMessage.NAME.equals(name))
		{
			return new RecorderStatusMessage(in);
		}
		else if (ScrollMessage.NAME.equals(name))
		{
			return new ScrollMessage(in);
		}
		else
		{
			throw new IOException("Unknown message: " + name);
		}
	}
}
