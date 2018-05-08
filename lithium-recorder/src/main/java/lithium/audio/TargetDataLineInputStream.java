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
import javax.sound.sampled.*;

public class TargetDataLineInputStream extends InputStream {
	private final TargetDataLine targetDataLine;

	private final byte[] buffer;

	private int bufferOffset;

	private int bufferDataLength;

	private final Object startLock = new Object();

	private volatile boolean started = false;

	TargetDataLineInputStream(TargetDataLine targetDataLine,
	        AudioFormat audioFormat) throws LineUnavailableException {
		this.targetDataLine = targetDataLine;

		targetDataLine.open(audioFormat);
		int frameSize = audioFormat.getFrameSize();
		buffer = new byte[((targetDataLine.getBufferSize() / 8 + frameSize - 1) / frameSize)
		        * frameSize];
	}

	public void start() {
		synchronized (startLock) {
			if (!started) {
				targetDataLine.start();
				started = true;
				startLock.notifyAll();
			}
		}
	}

	public boolean isStarted() {
		return started;
	}

	public void stop() {
		synchronized (startLock) {
			if (started) {
				targetDataLine.stop();
				started = false;
			}
		}
	}

	public void drain() {
		targetDataLine.drain();
	}

	@Override
	public int read() throws IOException {
		while (targetDataLine.isOpen() && (bufferOffset == bufferDataLength)) {
			bufferDataLength = targetDataLine.read(buffer, 0, buffer.length);
			bufferOffset = 0;
			if (targetDataLine.isOpen() && (bufferOffset == bufferDataLength)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Handled by enclosing while loop.
				}
			}
		}

		boolean eof = (bufferOffset == bufferDataLength);

		int result;
		if (eof) {
			result = -1;
			System.out.println("Recording stream closed");
		} else {
			result = buffer[bufferOffset++] & 0xff;
		}

		return result;
	}

	@Override
	public void close() throws IOException {
		targetDataLine.close();
	}
}
