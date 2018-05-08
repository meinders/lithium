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

package lithium.powerpoint;

import java.io.*;

/**
 * Views slide show presentations using OpenOffice.org Impress. This viewer does
 * not support the {@link #requestFocus()} method.
 *
 * @author Gerrit Meinders
 */
public class OpenOfficeImpress implements PPTViewer {
	private final File executable;

	private Process process;

	/**
	 * Constructs a new viewer using the given executable.
	 *
	 * @param executable the location of the Open Office executable (soffice)
	 */
	public OpenOfficeImpress(File executable) {
		super();
		this.executable = executable;
		process = null;
	}

	@Override
	public boolean isActive() {
		if (process == null) {
			return false;
		} else {
			try {
				process.exitValue();
				return false;
			} catch (IllegalThreadStateException e) {
				return true;
			}
		}
	}

	public void stop() {
		if (process != null) {
			process.destroy();
		}
	}

	@Override
	public void requestFocus() {
		/* Not supported. */
	}

	@Override
	public void startViewer(File presentation) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec(new String[] { executable.toString(),
		        "-invisible", "-norestore", "-show", presentation.toString() });
	}
}
