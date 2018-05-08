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

import java.awt.*;
import java.io.*;

/**
 * A viewer for Microsoft PowerPoint presentation, using the freely available
 * PowerPoint Viewer from Microsoft; this class adds rudimentary multi-monitor
 * support, which is lacking in the viewer itself.
 *
 * @version 0.9x (2006.07.14)
 * @author Gerrit Meinders
 */
public class PowerPointViewer implements PPTViewer {
	/**
	 * Returns whether the PowerPointViewer is supported on the current
	 * platform.
	 *
	 * @return whether the viewer is supported
	 */
	public static boolean isSupported() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	/** The PPT Viewer executable. */
	private File executable;

	/** The device to show the presentation on. */
	private GraphicsDevice device;

	private File presentation;

	private Process process;

	/**
	 * Constructs a new viewer using the given executable and device.
	 *
	 * @param executable the PowerPoint Viewer executable
	 * @param device the device to display the presentation on
	 */
	public PowerPointViewer(File executable, GraphicsDevice device) {
		this.executable = executable;
		this.device = device;

		process = null;
	}

	/**
	 * Starts a viewer for the given presentation.
	 *
	 * @param presentation the presentation
	 * @throws IOException if an exception occurs while starting the viewer
	 */
	public void startViewer(File presentation) throws IOException {
		this.presentation = presentation;

		String[] command = { executable.toString(), presentation.toString() };
		process = Runtime.getRuntime().exec(command);

		// select method: ScreenMirror vs NativeWindow relocation
		if (device != null) {
			GraphicsEnvironment graphEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] devices = graphEnv.getScreenDevices();
			GraphicsConfiguration defaultConfiguration = devices[0].getDefaultConfiguration();
			GraphicsConfiguration targetConfiguration = device.getDefaultConfiguration();
			Rectangle defaultConfigurationBounds = defaultConfiguration.getBounds();
			Rectangle targetConfigurationBounds = targetConfiguration.getBounds();

			// find or wait for PPT Viewer window
			final NativeWindow pptWindow = waitForWindow(presentation.getName());

			// if (targetConfigurationBounds.width <
			// defaultConfigurationBounds.width)
			if (false) {
				// display on smaller screens using a Screen Mirror
				final AWTScreenMirror mirror = new AWTScreenMirror(device,
				        devices[0]);
				mirror.setVisible(true);

				class ProcessMonitor extends Thread {
					private ProcessMonitor() {
						setDaemon(true);
					}

					@Override
					public void run() {
						try {
							process.waitFor();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mirror.dispose();
					}
				}
				ProcessMonitor processMonitor = new ProcessMonitor();
				processMonitor.start();

				pptWindow.requestFocus();

			} else {
				// display on larger screens by moving the PPT Viewer window
				if (pptWindow != null) {
					Rectangle bounds = getCenteredBounds(
					        defaultConfiguration.getBounds(),
					        targetConfiguration.getBounds());
					pptWindow.setBounds(bounds);
				}
			}
		}
	}

	/**
	 * Finds and returns the first window that matches the given title. This
	 * method blocks until a window is found.
	 */
	private NativeWindow waitForWindow(String title) {
		NativeWindow pptWindow = null;
		for (int i = 0; i < 50; i++) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			pptWindow = NativeWindow.getWindowByTitle(title);
			if (pptWindow != null) {
				break;
			}
		}
		return pptWindow;
	}

	/**
	 * Returns the given bounds when centered within the context bounds.
	 *
	 * @param bounds the bounds to be centered
	 * @param context the context in which the bounds are centered
	 * @return the centered bounds
	 */
	private Rectangle getCenteredBounds(Rectangle bounds, Rectangle context) {
		return new Rectangle(context.x + (context.width - bounds.width) / 2,
		        context.y + (context.height - bounds.height) / 2, bounds.width,
		        bounds.height);
	}

	public void requestFocus() {
		final NativeWindow pptWindow = NativeWindow.getWindowByTitle(presentation.getName());
		if (pptWindow != null) {
			pptWindow.requestFocus();
		}
	}

	public boolean isActive() {
		final NativeWindow pptWindow = NativeWindow.getWindowByTitle(presentation.getName());
		return pptWindow != null;
	}

	@Override
	public void stop() {
		if (process != null) {
			process.destroy();
		}
	}
}
