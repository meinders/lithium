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

import java.io.*;
import java.util.*;

/**
 * Allows command-line options for LAME to be specified.
 *
 * @author Gerrit Meinders
 */
public class Mp3Options
{
	public enum Mode {
		STEREO, JOINT_STEREO, MONO
	}

	private Mode mode;

	private BitRate bitRate;

	public Mp3Options() {
		mode = null;
		bitRate = null;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public BitRate getBitRate() {
		return bitRate;
	}

	public void setBitRate(BitRate bitRate) {
		this.bitRate = bitRate;
	}

	public String[] getCommandArray(File executable) {
		if (executable == null) {
			throw new NullPointerException("executable");
		}
		if (!executable.canExecute()) {
			throw new IllegalArgumentException("executable");
		}

		Collection<String> command = new ArrayList<String>();
		command.add(executable.getPath());

		if (mode != null) {
			switch (mode) {
			case STEREO:
				command.add("-ms");
				break;
			case JOINT_STEREO:
				command.add("-mj");
				break;
			case MONO:
				command.add("-mm");
				break;
			}
		}

		if (bitRate != null) {
			bitRate.apply(command);
		}

		command.add("--ty"); // audio/song year of issue (1 to 9999)
		Calendar calendar = Calendar.getInstance();
		command.add(String.valueOf(calendar.get(Calendar.YEAR)));
		command.add("--add-id3v2");// force addition of version 2 tag
		command.add("--pad-id3v2");// pad version 2 tag with extra 128 bytes

		command.add("--quiet");
		command.add("-"); // standard input/output

		return command.toArray(new String[command.size()]);
	}

	public abstract static class BitRate {
		BitRate() {
			/* Prevent subclassing outside of this package. */
		}

		abstract void apply(Collection<String> command);
	}

	public static final class ConstantBitRate extends BitRate
	{
		private Integer bitRate;

		public ConstantBitRate(Integer bitRate) {
			this.bitRate = bitRate;
		}

		public Integer getBitRate() {
			return bitRate;
		}

		@Override
		void apply(Collection<String> command) {
			if (bitRate == null) {
				command.add("--cbr");
			} else {
				command.add("-b" + bitRate);
			}
		}
	}

	public static final class AverageBitRate extends BitRate
	{
		private int bitRate;

		public AverageBitRate(int bitRate) {
			this.bitRate = bitRate;
		}

		public int getBitRate() {
			return bitRate;
		}

		@Override
		void apply(Collection<String> command) {
			command.add("--abr");
			command.add(String.valueOf(bitRate));
		}
	}

	public static final class VariableBitRate extends BitRate
	{
		private Integer quality;

		public VariableBitRate(Integer quality) {
			this.quality = quality;
		}

		public Integer getQuality() {
			return quality;
		}

		@Override
		void apply(Collection<String> command) {
			command.add("--vbr-new");
			if (quality != null) {
				command.add("-V" + quality);
			}
		}
	}
}
