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

package lithium.audio;

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

import org.junit.*;

import static org.junit.Assert.*;

public class NormalizingOutputStreamTest
{
	/**
	 * The rolling maximum to run out of buffer space if values aren't properly
	 * removed from it. This test implements one possible worst-case scenario
	 * for this.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Test
	public void testRollingMaximumCapacity() throws IOException
	{
		AudioFormat audioFormat = new AudioFormat(
		        AudioFormat.Encoding.PCM_SIGNED, 4410, 16, 2, 4, 4410, false);
		NormalizingOutputStream out = new NormalizingOutputStream(
		        new NullOutputStream(), audioFormat, 1.0, 30.0, false);

		for (int i = 0; i < 10000; i++)
		{
			int sample = 1;
			out.write(sample);
			out.write(sample >> 8);
		}

		out.close();
	}

	/**
	 * Tests that the stream will correct any DC offset.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Test
	public void testDCOffset() throws IOException
	{
		AudioFormat audioFormat = new AudioFormat(
		        AudioFormat.Encoding.PCM_SIGNED, 4410, 16, 2, 4, 4410, false);
		NormalizingOutputStream out = new NormalizingOutputStream(
		        new NullOutputStream(), audioFormat, 1.0, 30.0, false);

		Random random = new Random(0);

		// Write a random signal with a DC offset of 95.
		for (int i = 0; i < 30000; i++)
		{
			int sample = 90 + random.nextInt(11);
			out.write(sample);
			out.write(sample >> 8);
		}

		assertTrue("Unexpected DC offset: " + out.getDCOffset(0),
		        out.getDCOffset(0) > 90 && out.getDCOffset(0) < 100);
		assertTrue("Unexpected DC offset: " + out.getDCOffset(1),
		        out.getDCOffset(1) > 90 && out.getDCOffset(1) < 100);

		out.flush();

		// Write a random signal with a DC offset of -95.
		for (int i = 0; i < 40000; i++)
		{
			int sample = -90 - random.nextInt(11);
			out.write(sample);
			out.write(sample >> 8);
		}

		assertTrue("Unexpected DC offset: " + out.getDCOffset(0),
		        out.getDCOffset(0) < -90 && out.getDCOffset(0) > -100);
		assertTrue("Unexpected DC offset: " + out.getDCOffset(1),
		        out.getDCOffset(1) < -90 && out.getDCOffset(1) > -100);

		out.close();
	}

	/**
	 * Tests that normalization doesn't break when extreme volume changes occur.
	 * Due to such a change, the volume after amplification may exceed the
	 * allowed range of the output format, if not implemented correctly.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Test
	public void testExtremeVolumeChanges() throws IOException
	{
		AudioFormat audioFormat = new AudioFormat(
		        AudioFormat.Encoding.PCM_SIGNED, 4410, 16, 2, 4, 4410, false);
		NormalizingOutputStream out = new NormalizingOutputStream(
		        new NullOutputStream(), audioFormat, 1.0, 30.0, false);

		Random random = new Random(0);

		int highAmplitude = out.sampleFormat.getMaximumAmplitude();
		int lowAmplitude = highAmplitude / 1000;

		// Write very low volume samples to increase amplification.
		for (int i = 0; i < 20000; i++)
		{
			int sample = lowAmplitude;
			out.write(sample);
			out.write(sample >> 8);
		}

		int sampleCount = 0;
		while (sampleCount < 100000)
		{
			System.out.println("dc " + out.getDCOffset(0) + " | "
			        + out.getDCOffset(0));

			int n = random.nextInt(500);
			int m = random.nextInt(500);
			sampleCount += n + m;

			// Write very high volume samples.
			for (int i = 0; i < n; i++)
			{
				int sample = ((i & 1) * 2 - 1) * highAmplitude;
				out.write(sample);
				out.write(sample >> 8);
			}

			// Write very low volume samples.
			for (int i = 0; i < m; i++)
			{
				int sample = lowAmplitude;
				out.write(sample);
				out.write(sample >> 8);
			}
		}
	}

	/**
	 * This output stream discards any data written to it.
	 */
	private static class NullOutputStream extends OutputStream
	{
		@Override
		public void write(int b) throws IOException
		{
			// Discard written data.
		}

		@Override
		public void write(byte[] b) throws IOException
		{
			// Discard written data.
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
			// Discard written data.
		}
	}
}
