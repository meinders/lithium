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

public class ContentMessage extends Message
{
	public static final String NAME = "c";

	private final String[] lines;

	private final float[] tops;

	public ContentMessage(String[] lines, float[] tops)
	{
		super(NAME);
		this.lines = lines;
		this.tops = tops;
	}

	public ContentMessage(DataInput in) throws IOException
	{
		super(NAME);

		int lineCount = in.readInt();

		lines = new String[lineCount];
		tops = new float[lineCount];

		for (int i = 0; i < lineCount; i++)
		{
			lines[i] = in.readUTF();
			tops[i] = in.readFloat();
		}
	}

	protected void writeData(DataOutput out) throws IOException
	{
		out.writeInt(lines.length);
		for (int i = 0; i < lines.length; i++)
		{
			out.writeUTF(lines[i]);
			out.writeFloat(tops[i]);
		}
	}

	public int getLineCount()
	{
		return lines.length;
	}

	public String getLine(int index)
	{
		return lines[index];
	}

	public float getTop(int index)
	{
		return tops[index];
	}

	public String toString()
	{
		return super.toString() + "[" + getLineCount() + " lines]";
	}
}
