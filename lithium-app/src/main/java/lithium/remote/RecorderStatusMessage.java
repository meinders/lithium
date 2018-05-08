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

public class RecorderStatusMessage extends Message
{
	public static final String NAME = "rs";

	private final boolean recording;

	private byte[] levels;

	public RecorderStatusMessage(boolean recording, byte[] levels)
	{
		super(NAME);
		this.recording = recording;
		this.levels = levels;

	}

	public RecorderStatusMessage(DataInput in) throws IOException
	{
		super(NAME);
		this.recording = in.readBoolean();
		int channels = in.readByte();
		levels = new byte[channels];
		for (int i = 0; i < channels; i++)
		{
			levels[i] = in.readByte();
		}
	}

	protected void writeData(DataOutput out) throws IOException
	{
		out.writeBoolean(recording);
		out.writeByte(levels.length);
		for (int i = 0; i < levels.length; i++)
		{
			out.writeByte(levels[i]);
		}
	}

	public boolean isRecording()
	{
		return recording;
	}

	public byte[] getLevels()
	{
		return levels;
	}
}
