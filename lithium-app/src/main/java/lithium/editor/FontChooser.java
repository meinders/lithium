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
import javax.swing.GroupLayout.*;

import lithium.*;

/**
 * Allows the user to choose a font from a list of fonts, with each font name
 * styled using the font it represents.
 *
 * @author Gerrit Meinders
 */
public class FontChooser extends JPanel {
	private static final int DEFAULT_FONT_SIZE = 24;

	private static final Integer[] DEFAULT_FONT_SIZES = new Integer[] { 8, 9,
	        10, 12, 14, 16, 18, 20, 22, 24, 28, 32, 36, 40, 44, 48, 52, 56, 64,
	        72, 96, 128 };

	private static final Integer[] FONT_STYLES = new Integer[] { Font.PLAIN,
	        Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC };

	private String fontFamily = Font.SANS_SERIF;

	private int fontStyle = Font.PLAIN;

	private int fontSize = DEFAULT_FONT_SIZE;

	private final JComboBox familyCombo;

	private final JComboBox sizeCombo;

	private final JComboBox styleCombo;

	public FontChooser() {
		super();

		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = graphicsEnvironment.getAvailableFontFamilyNames();

		familyCombo = new JComboBox(fontNames);
		familyCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fontFamily = (String) familyCombo.getSelectedItem();
			}
		});
		familyCombo.setSelectedIndex(0);

		familyCombo.setPrototypeDisplayValue(Font.SANS_SERIF);
		familyCombo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
			        Object value, int index, boolean isSelected,
			        boolean cellHasFocus) {
				String fontFamily = (String) value;
				Font font = new Font(fontFamily, fontStyle, DEFAULT_FONT_SIZE);
				super.getListCellRendererComponent(list, fontFamily, index,
				        isSelected, cellHasFocus);
				setFont(font);
				return this;
			}
		});
		{
			Dimension preferredSize = familyCombo.getPreferredSize();
			preferredSize.width = 200;
			familyCombo.setPreferredSize(preferredSize);
		}

		sizeCombo = new JComboBox(DEFAULT_FONT_SIZES);

		sizeCombo.setPrototypeDisplayValue(Integer.valueOf(24));
		sizeCombo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
			        Object value, int index, boolean isSelected,
			        boolean cellHasFocus) {
				Integer fontSize = (Integer) value;
				super.getListCellRendererComponent(list, fontSize + "pt",
				        index, isSelected, cellHasFocus);
				setFont(new Font(fontFamily, Font.PLAIN, fontSize));
				return this;
			}
		});
		{
			Dimension preferredSize = sizeCombo.getPreferredSize();
			preferredSize.width = 75;
			sizeCombo.setPreferredSize(preferredSize);
		}
		sizeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fontSize = (Integer) sizeCombo.getSelectedItem();
			}
		});

		styleCombo = new JComboBox(FONT_STYLES);

		styleCombo.setPrototypeDisplayValue(Integer.valueOf(Font.BOLD
		        | Font.ITALIC));
		styleCombo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
			        Object value, int index, boolean isSelected,
			        boolean cellHasFocus) {
				Integer fontStyle = (Integer) value;
				String styleName;
				switch (fontStyle) {
				default:
				case Font.PLAIN:
					styleName = "font.plain";
					break;
				case Font.BOLD:
					styleName = "font.bold";
					break;
				case Font.ITALIC:
					styleName = "font.italic";
					break;
				case Font.BOLD | Font.ITALIC:
					styleName = "font.boldItalic";
					break;
				}
				super.getListCellRendererComponent(list,
				        Resources.get().getString(styleName), index,
				        isSelected, cellHasFocus);
				setFont(getFont().deriveFont(fontStyle));
				return this;
			}
		});
		{
			Dimension preferredSize = styleCombo.getPreferredSize();
			preferredSize.width = 75;
			styleCombo.setPreferredSize(preferredSize);
		}
		styleCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fontStyle = (Integer) styleCombo.getSelectedItem();
			}
		});

		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		setLayout(layout);

		SequentialGroup horizontal = layout.createSequentialGroup();
		ParallelGroup vertical = layout.createBaselineGroup(true, true);

		layout.setHorizontalGroup(horizontal);
		layout.setVerticalGroup(vertical);

		horizontal.addComponent(familyCombo);
		vertical.addComponent(familyCombo);

		horizontal.addComponent(sizeCombo);
		vertical.addComponent(sizeCombo);

		horizontal.addComponent(styleCombo);
		vertical.addComponent(styleCombo);
	}

	public Font getSelectedFont() {
		return new Font(fontFamily, fontStyle, fontSize);
	}

	public void setSelectedFont(Font font) {
		fontFamily = font.getFamily();
		fontSize = font.getSize();
		fontStyle = font.getStyle();

		familyCombo.setSelectedItem(fontFamily);
		sizeCombo.setSelectedItem(fontSize);
		styleCombo.setSelectedItem(fontStyle);
	}
}
