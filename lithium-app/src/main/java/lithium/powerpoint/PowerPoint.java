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
 * A viewer for PPT presentations which uses PowerPoint, from the Microsoft
 * Office suite. All versions since PowerPoint 95 are supported. This class does
 * not support specification of a graphics device to show the presentation on.
 * PowerPoint should be configured seperately to display presentations on the
 * appropriate display.
 *
 * @author Gerrit Meinders
 */
public class PowerPoint implements PPTViewer {
    private final File executable;

    private File presentation;

    private Process process;

    public PowerPoint(File executable) {
        this.executable = executable;

        process = null;
    }

    public void startViewer(File presentation) throws IOException {
        this.presentation = presentation;

        String[] command = { executable.toString(), "/s", presentation.toString() };
        process = Runtime.getRuntime().exec(command);
    }

    /**
     * Finds and returns the first window that matches the given title. This
     * method blocks until a window is found.
     */
    private NativeWindow findWindow(String title) {
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

    public void requestFocus() {
        NativeWindow pptWindow = NativeWindow.getWindowByTitle(presentation.getName());
        if (pptWindow != null) {
            pptWindow.requestFocus();
        }
    }

    public boolean isActive() {
        NativeWindow pptWindow = NativeWindow.getWindowByTitle(presentation.getName());
        return pptWindow != null;
    }

    @Override
    public void stop() {
    	if (process != null) {
    		process.destroy();
    	}
    }
}
