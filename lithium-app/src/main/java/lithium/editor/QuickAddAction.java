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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.github.meinders.common.*;
import lithium.*;
import lithium.catalog.*;
import lithium.search.*;

public abstract class QuickAddAction extends AbstractAction implements Runnable {
	private JTextField textField;

	protected QuickAddAction(JTextField textField) {
		super(Resources.get("playlistEditor").getString("quickAdd"));

		if (textField == null) {
			throw new NullPointerException("textField");
		}

		this.textField = textField;

		ResourceUtilities resources = Resources.get("playlistEditor");
		putValue(MNEMONIC_KEY, resources.getMnemonic("quickAdd"));
		putValue(SHORT_DESCRIPTION, resources.getString("quickAdd.tip"));
		putValue(SMALL_ICON, getIcon("general/Add16.gif"));
		putValue(LARGE_ICON_KEY, getIcon("general/Add24.gif"));
	}

	private Icon getIcon(String name) {
		return new ImageIcon(getClass().getResource(
		        "/toolbarButtonGraphics/" + name));
	}

	private Window getWindow() {
		return SwingUtilities.getWindowAncestor(textField);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		SwingUtilities.invokeLater(this);
	}

	@Override
	public void run() {
		String text = textField.getText();

		Selector selector = Selectors.firstNonEmpty(Selectors.all(
		        new BibleRefSelector(), new LyricSelector()),
		        new LyricSearchSelector());

		Collection<?> selection = selector.select(text);

		textField.setBackground(Color.WHITE);
		if (selection.isEmpty()) {
			textField.setBackground(new Color(0xffe0c0));

			textField.setSelectionStart(0);
			textField.setSelectionEnd(text.length());
			textField.requestFocus();

		} else if (selection.size() == 1) {
			Object content = selection.iterator().next();
			final PlaylistItem item = getPlaylistItem(content);
			playlistItemSelected(item);

			textField.setText("");
			textField.requestFocus();

		} else {
			JPopupMenu popup = new JPopupMenu();

			boolean first = true;
			JMenuItem firstMenuItem = null;
			for (final Object content : selection) {
				final PlaylistItem item = getPlaylistItem(content);

				AbstractAction action = new AbstractAction(item.getTitle()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						playlistItemSelected(item);

						textField.setText("");
						textField.requestFocus();
					}
				};

				JMenuItem menuItem = popup.add(action);
				if (first) {
					firstMenuItem = menuItem;
					first = false;
				}
			}

			popup.show(textField, 0, 0);
			final MenuSelectionManager menuSelectionManager = MenuSelectionManager.defaultManager();
			menuSelectionManager.setSelectedPath(new MenuElement[] { popup,
			        firstMenuItem });
		}

		getWindow().setCursor(Cursor.getDefaultCursor());
	}

	private PlaylistItem getPlaylistItem(Object content) {
		final PlaylistItem item;

		if (content instanceof SearchResult) {
			SearchResult searchResult = (SearchResult) content;
			LyricRef lyricRef = searchResult.getLyricRef();
			item = new PlaylistItem(lyricRef);

		} else if (content instanceof Lyric) {
			/*
			 * Add lyrics by reference if found in catalog; otherwise by value.
			 */
			Lyric lyric = (Lyric) content;

			Catalog catalog = CatalogManager.getCatalog();
			Group bundle = catalog.getBundle(lyric);

			if (bundle == null) {
				item = new PlaylistItem(lyric);
			} else {
				LyricRef lyricRef = new LyricRef(bundle.getName(),
				        lyric.getNumber());
				item = new PlaylistItem(lyricRef);
			}

		} else {
			item = new PlaylistItem(content);
		}

		return item;
	}

	protected abstract void playlistItemSelected(PlaylistItem item);
}
