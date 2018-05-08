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
import javax.swing.*;

/**
 * Label component that supports multiple lines of automatically wrapped text.
 *
 * @author Gerrit Meinders
 */
public class MultiLineLabel extends JLabel {
    public MultiLineLabel(String text , int lines) {
        super.setText("<html>" + text + "</html>");

        setHorizontalAlignment(LEFT);
        setVerticalAlignment(TOP);

        Dimension preferredSize = getPreferredSize();
        preferredSize.width = 0;
        FontMetrics metrics = getFontMetrics(getFont());
        preferredSize.height = lines * metrics.getHeight();
        setPreferredSize(preferredSize);
    }
}
