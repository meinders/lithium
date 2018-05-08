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

import java.awt.image.*;
import java.io.*;
import java.net.*;

/**
 * Represents an image that may be loaded from a file. For some APIs, such as
 * the Java Binding for the OpenGL API (JOGL), loading the image directly may be
 * faster than first loading the image into a {@link BufferedImage}.
 *
 * @author Gerrit Meinders
 */
public class ImageRef {
	private final URL source;

	public ImageRef(File source) {
		super();
		if (source == null) {
			throw new NullPointerException("source");
		}
		try {
			this.source = source.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public ImageRef(URL source) {
		super();
		if (source == null) {
			throw new NullPointerException("source");
		}
		this.source = source;
	}

	public URL getSource() {
		return source;
	}

	@Override
	public String toString() {
		return source.toString();
	}
}
