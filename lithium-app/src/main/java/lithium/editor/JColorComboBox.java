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
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.github.meinders.common.swing.*;

/**
 * This special combo box allows the user to select a color from a predefined
 * set of colors or a custom color, selected using a JColorChooser.
 *
 * @version 0.9 (2006.02.07)
 * @author Gerrit Meinders
 */
public class JColorComboBox extends JComboBox {
    private String colorChooserTitle = "";

    public JColorComboBox(Color[] colors, boolean selectCustomColors) {
        this(new ColorComboBoxModel(colors, selectCustomColors));
    }

    public JColorComboBox(ColorComboBoxModel model) {
        super(model);
        setEditable(false);
        setRenderer(new ColorListCellRenderer());
    }

    public void setColorChooserTitle(String colorChooserTitle) {
        this.colorChooserTitle = colorChooserTitle;
    }

    public String getColorChooserTitle() {
        return colorChooserTitle;
    }

    public void setSelectedItem(Object item) {
        if (item == null) {
            selectCustomColor();
            if (getModel().getSize() > 0) {
                assert getModel().getSelectedItem() != null :
                        "getModel().getSelectedItem() != null";
            }

        } else {
            super.setSelectedItem(item);
        }
    }

    private void selectCustomColor() {
        // create color chooser
        Color initialColor = getModel().getCustomColor();
        if (initialColor == null) {
            initialColor = Color.BLACK;
        }
        final JColorChooser chooser = new JColorChooser(initialColor);

        // create custom preview panel
        chooser.setPreviewPanel(new JPanel());
        SimplePreviewPanel previewPanel =
                new SimplePreviewPanel(initialColor);
        chooser.getSelectionModel().addChangeListener(previewPanel);
        chooser.add(previewPanel, BorderLayout.SOUTH);

        // show dialog
        Action okAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // set custom color
                Color customColor = chooser.getColor();
                getModel().setSelectedItem(customColor);
            }};
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getModel().setSelectedItem(Color.BLACK);
            }};
        JDialog dialog = JColorChooser.createDialog(
                getTopLevelAncestor(),
                colorChooserTitle,
                true, chooser, okAction, cancelAction);
        dialog.setVisible(true);
    }

    public ColorComboBoxModel getModel() {
        return (ColorComboBoxModel) super.getModel();
    }

    public void setModel(ColorComboBoxModel model) {
        super.setModel(model);
    }

    private class SimplePreviewPanel extends JPanel implements ChangeListener {
        public SimplePreviewPanel(Color initialColor) {
            setForeground(initialColor);
            setPreferredSize(new Dimension(0, 50));
            setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }

        public void paintComponent(Graphics g) {
            Insets insets = getInsets();
            g.fillRect(insets.left, insets.top,
                    getWidth() - insets.left - insets.right,
                    getHeight() - insets.top - insets.bottom);
        }

        public void stateChanged(ChangeEvent e) {
            JColorChooser host = (JColorChooser)
                    SwingUtilities.getAncestorOfClass(
                    JColorChooser.class, this);
            Color newColor = host.getColor();
            setForeground(newColor);
        }
    }
}

