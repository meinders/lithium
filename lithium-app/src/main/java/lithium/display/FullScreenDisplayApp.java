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

package lithium.display;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.github.meinders.common.*;
import lithium.*;
import lithium.catalog.*;

public class FullScreenDisplayApp {
	public static void main(String[] args) {
		Runnable runFullScreenDisplay = new Runnable() {
			public void run() {
				Resources.set(new ResourceUtilities(
				        "lithium.Resources"));

				ConfigManager.readConfig();
				Config config = ConfigManager.getConfig();

				CatalogManager.loadDefaultCatalogs(null);
				Catalog catalog = CatalogManager.getCatalog();
				Lyric lyric = catalog.getLyric(new LyricRef(
				        config.getDefaultBundle(), 123));

				System.out.println("lyric = '" + lyric + "'");

				PlaylistItem playlistItem = new PlaylistItem(lyric);

				Playlist playlist = new Playlist();
				playlist.add(playlistItem);

				PlaylistSelectionModel selectionModel = playlist.getSelectionModel();
				selectionModel.setSelectedIndex(0);

				PlaylistManager.setPlaylist(playlist);

				FullScreenDisplay.start();
			}
		};

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("Window Positioning Test");
				frame.setLayout(new GridLayout(0, 1));

				GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] screens = graphicsEnvironment.getScreenDevices();
				GraphicsDevice screen = screens[screens.length - 1];
				final GraphicsConfiguration graphicsConfiguration = screen.getDefaultConfiguration();

				frame.add(new JButton(new AbstractAction("Default") {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFrame frame = new JFrame((String) getValue(NAME),
						        graphicsConfiguration) {
							{
								setDefaultCloseOperation(DISPOSE_ON_CLOSE);
								add(new JLabel("Hello world!"));
								setBounds(graphicsConfiguration.getBounds());
							}
						};
						frame.setVisible(true);
					}
				}));

				frame.add(new JButton(new AbstractAction("Undecorated") {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFrame frame = new JFrame((String) getValue(NAME),
						        graphicsConfiguration) {
							{
								setUndecorated(true);
								setDefaultCloseOperation(DISPOSE_ON_CLOSE);
								add(new JLabel("Hello world!"));
								setBounds(graphicsConfiguration.getBounds());
							}
						};
						frame.setVisible(true);
					}
				}));

				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
}
