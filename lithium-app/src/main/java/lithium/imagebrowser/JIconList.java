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

package lithium.imagebrowser;

import java.awt.*;
import javax.swing.*;

import static javax.swing.SwingConstants.*;

/**
 * A list that displays an icon for each item.
 *
 * @author Gerrit Meinders
 * @param <V> value type of the list
 */
public class JIconList<V> extends JList {
    private IconListCellRenderer cellRenderer;

    public JIconList() {
        this(new IconListModel<V>());
    }

    public JIconList(IconListModel<V> listModel) {
        super(listModel);
        setOpaque(true);
        cellRenderer = new IconListCellRenderer();
        setCellRenderer(cellRenderer);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(0);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        final Rectangle cellBounds = getCellBounds(0, getModel().getSize() - 1);
        return cellBounds == null ? super.getPreferredSize() : new Dimension(
                cellBounds.x * 2 + cellBounds.width, cellBounds.y * 2
                        + cellBounds.height);
    }

    public void setModel(IconListModel<V> model) {
        super.setModel(model);
    }

    public void setTextPosition(int position) {
        switch (position) {
        case TOP:
            cellRenderer.setHorizontalTextPosition(CENTER);
            cellRenderer.setVerticalTextPosition(TOP);
            cellRenderer.setHorizontalAlignment(CENTER);
            cellRenderer.setVerticalAlignment(CENTER);
            break;
        case BOTTOM:
            cellRenderer.setHorizontalTextPosition(CENTER);
            cellRenderer.setVerticalTextPosition(BOTTOM);
            cellRenderer.setHorizontalAlignment(CENTER);
            cellRenderer.setVerticalAlignment(CENTER);
            break;
        case LEFT:
            cellRenderer.setHorizontalTextPosition(LEFT);
            cellRenderer.setVerticalTextPosition(CENTER);
            cellRenderer.setHorizontalAlignment(RIGHT);
            cellRenderer.setVerticalAlignment(CENTER);
            break;
        case RIGHT:
            cellRenderer.setHorizontalTextPosition(RIGHT);
            cellRenderer.setVerticalTextPosition(CENTER);
            cellRenderer.setHorizontalAlignment(LEFT);
            cellRenderer.setVerticalAlignment(CENTER);
            break;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public IconListModel<V> getModel() {
        return (IconListModel<V>) super.getModel();
    }

    public void add(V value, ImageIcon icon) {
        getModel().add(value, icon, icon.getDescription());
    }

    public void add(V value, Icon icon, String description) {
        getModel().add(value, icon, description);
    }

    public void remove(V value) {
        getModel().remove(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getSelectedValue() {
        return (V) super.getSelectedValue();
    }
}
