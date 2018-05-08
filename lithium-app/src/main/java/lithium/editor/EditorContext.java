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

package lithium.editor;

import java.awt.datatransfer.*;
import javax.swing.*;

import lithium.*;

/**
 * Provides context information to an editor. Typically, the editor's container
 * would implement this interface.
 *
 * @author Gerrit Meinders
 */
public interface EditorContext {
    /**
     * Returns a local clipboard instance, allowing any object to be shared
     * within this JVM.
     *
     * @return the local clipboard instance
     */
    public Clipboard getLocalClipboard();

    /**
     * Shows a dialog notifying the user that an exception occurred.
     *
     * @param message a message explaining the exception
     * @param exception the exception
     */
    public void showExceptionDialog(String message, Exception exception);

    /**
     * Returns the icon with the specified name.
     *
     * @param name the name of the icon
     * @return the icon
     */
    public Icon getIcon(String name);

    /**
     * Shows an editor dialog for the given playlist. If there already is a
     * dialog showing the playlist, the existing dialog is shown.
     *
     * @param playlist The playlist to be shown.
     */
    public void showPlaylist(Playlist playlist);
}
