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

import java.awt.event.*;
import javax.swing.*;

import lithium.*;

/**
 * A mouse and mouse motion listener that allows a reordering of a reorderable
 * object through a JList.
 *
 * @version 0.9 (2005.10.23)
 * @author Gerrit Meinders
 */
public class ReorderListener implements MouseListener, MouseMotionListener {

	public static void addToList(JList list, Reorderable reorderable) {
		ReorderListener listener = new ReorderListener(list, reorderable);
		list.addMouseListener(listener);
		list.addMouseMotionListener(listener);
	}

	private JList list;
	private Reorderable reorderable;
	private int from;

	/**
	 * Constucts a new ReorderAlbumTableListener for the given table and album.
	 * The constructed listener is added to the table automatically.
	 */
	protected ReorderListener(JList list, Reorderable reorderable) {
		assert list != null;
		assert reorderable != null;
		this.list = list;
		this.reorderable = reorderable;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			from = list.locationToIndex(e.getPoint());
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			int to = list.locationToIndex(e.getPoint());
			if (from != to) {
				reorderable.move(from, to);
				from = to;
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
	}
}
