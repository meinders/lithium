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

import java.awt.*;
import java.awt.datatransfer.*;
import java.beans.*;
import javax.swing.*;

import com.github.meinders.common.*;
import lithium.*;

public class PlaylistEditorApp
{
	static
	{
		Resources.set(new ResourceUtilities("lithium.Resources"));
	}

	public static void main(String[] args)
	{
		ConfigManager.readConfig();
		CatalogManager.loadDefaultCatalogs(null);

		PlaylistEditor editor = new PlaylistEditor();
		editor.setEditorContext(new EditorContext()
		{
			@Override
			public Icon getIcon(String name)
			{
				return new ImageIcon(getClass().getResource(
				        "/toolbarButtonGraphics/" + name));
			}

			@Override
			public Clipboard getLocalClipboard()
			{
				return null;
			}

			@Override
			public void showExceptionDialog(String message, Exception exception)
			{
				System.err.println(message);
				if (exception != null)
				{
					exception.printStackTrace();
				}
			}

			@Override
			public void showPlaylist(Playlist playlist)
			{
			}
		});

		JDesktopPaneEx desktop = new JDesktopPaneEx();
		desktop.add(editor, JDesktopPaneEx.DEFAULT);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(desktop);
		frame.setBounds(100, 100, 800, 600);
		frame.setVisible(true);

		try
		{
			editor.setSelected(true);
		}
		catch (PropertyVetoException e)
		{
			e.printStackTrace();
		}
	}
}
