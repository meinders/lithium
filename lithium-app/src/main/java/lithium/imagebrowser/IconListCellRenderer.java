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

class IconListCellRenderer extends JLabel implements ListCellRenderer {
    private boolean cellHasFocus;

    public IconListCellRenderer() {
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setHorizontalTextPosition(CENTER);
        setVerticalTextPosition(BOTTOM);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    @SuppressWarnings("unchecked")
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        IconListModel model = (IconListModel) list.getModel();
        Icon icon = model.getIcon(value);
        setText(model.getDescription(value));
        setIcon(icon);
        setOpaque(list.isOpaque());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setCellHasFocus(cellHasFocus);

        return this;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (cellHasFocus) {
            g.setColor(getBackground().darker());
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    private void setCellHasFocus(boolean cellHasFocus) {
        this.cellHasFocus = cellHasFocus;
    }
}
