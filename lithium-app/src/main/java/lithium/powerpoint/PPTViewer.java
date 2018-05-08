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
 * Interface for classes that provide viewing capabilities for PPT files.
 *
 * @author Gerrit Meinders
 */
public interface PPTViewer {
    /**
     * Starts a viewer for the given presentation.
     *
     * @param presentation the presentation
     * @throws IOException if an exception occurs while starting the viewer
     */
    void startViewer(File presentation) throws IOException;

    /**
     * Returns whether the viewer is currently active.
     *
     * @return <code>true</code> if the viewer is active; <code>false</code>
     *         otherwise.
     */
    boolean isActive();

    /**
     * Stops the viewer, if it is currently active.
     */
    void stop();

    /**
     * Focuses the window of the viewer.
     */
    void requestFocus();
}
