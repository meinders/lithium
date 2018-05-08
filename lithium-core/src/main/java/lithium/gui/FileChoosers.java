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

package lithium.gui;

import java.beans.*;
import java.io.*;
import javax.swing.*;

import lithium.*;

public class FileChoosers
{
	private static File currentDirectory = null;

	/**
	 * Keeps the {@link #currentDirectory} field synchronized with the file
	 * choosers created by this class.
	 */
	private static PropertyChangeListener directoryChangeListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			JFileChooser fileChooser = (JFileChooser) evt.getSource();
			currentDirectory = fileChooser.getCurrentDirectory();
		}
	};

	public static JFileChooser createFileChooser()
	{
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);

		JFileChooser fileChooser = new JFileChooser();

		if (currentDirectory == null)
		{
			currentDirectory = ConfigManager.getHomeFolder();
		}
		fileChooser.setCurrentDirectory(currentDirectory);

		fileChooser.addPropertyChangeListener(
		        JFileChooser.DIRECTORY_CHANGED_PROPERTY,
		        directoryChangeListener);

		return fileChooser;
	}
}
