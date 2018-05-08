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

package lithium.animation.legacy.scrolling;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * <p>
 * This class provides a conversion strategy that ensures that a logical unit is
 * exactly one line of text in the component's model (as opposed to the
 * component's view, which may introduce additional line breaks).
 *
 * <p>
 * This strategy causes every line, regardless of its length, to be scrolled in
 * the same amount of time. This may be undesirable in some cases. Therefore, an
 * alternative character-based strategy is provided by the
 * {@link CharacterConversionStrategy}.
 *
 * @see AdvancedTextRangeModel
 *
 * @version 0.9x (2005.08.05)
 * @author Gerrit Meinders
 */
public class LineConversionStrategy implements ConversionStrategy,
        ComponentListener, PropertyChangeListener {
    private JTextPane textPane = null;

    private ArrayList<Integer> conversionTable = null;

    private boolean topMarginFixed = true;

    /**
     * Constructs a new line-based conversion strategy with no associated text
     * component. A text component must be set before the strategy can be used
     * to convert units.
     */
    public LineConversionStrategy() {
    }

    /**
     * Constructs a new line-based conversion strategy associated to the given
     * text component.
     *
     * @param textPane the text component
     */
    public LineConversionStrategy(JTextPane textPane) {
        setTextComponent(textPane);
    }

    /**
     * Sets whether the top margin should be ignored.
     */
    public void setTopMarginFixed(boolean topMarginFixed) {
        if (topMarginFixed != isTopMarginFixed()) {
            this.topMarginFixed = topMarginFixed;
            updateConversionTable();
        }
    }

    /**
     * Returns whether the top margin should be interpreted as a fixed
     * difference in scroll value.
     */
    public boolean isTopMarginFixed() {
        return topMarginFixed;
    }

    /**
     * Sets the text component used to take measurements for unit conversion.
     */
    public void setTextComponent(JTextPane textPane) {
        if (this.textPane != null) {
            this.textPane.removeComponentListener(this);
            this.textPane.removePropertyChangeListener(this);
        }
        this.textPane = textPane;
        if (textPane != null) {
            textPane.addComponentListener(this);
            textPane.addPropertyChangeListener(this);
        }
    }

    /**
     * Converts the given logical value to the text component's units.
     *
     * @param value scroll value in logical units
     */
    public int modelToView(float value) {
        if (isTopMarginFixed()) {
            value++;
        }
        int line = (int) value;
        if (line < 0) {
            line = 0;
        }
        float fraction = value - line;
        ArrayList<Integer> table = getConversionTable();
        int viewValue;
        if (line < table.size() - 1) {
            int start = table.get(line);
            int height = table.get(line + 1) - start;
            viewValue = (int) (start + fraction * height);
        } else {
            viewValue = table.get(table.size() - 1);
        }
        if (isTopMarginFixed()) {
            viewValue -= table.get(1);
        }
        return viewValue;
    }

    /**
     * Converts the given value in the text component's units to logical units.
     *
     * @param value scroll value in view's units
     */
    public float viewToModel(int value) {
        ArrayList<Integer> table = getConversionTable();
        if (isTopMarginFixed()) {
            value += table.get(1);
        }
        float modelValue = table.get(0);
        for (int i = 1; i < table.size(); i++) {
            if (value < table.get(i)) {
                float start = table.get(i - 1);
                float height = table.get(i) - start;
                modelValue = (i - 1) + (value - start) / height;
                break;
            }
        }
        if (isTopMarginFixed()) {
            modelValue--;
        }
        return modelValue;
    }

    private ArrayList<Integer> getConversionTable() {
        if (conversionTable == null) {
            updateConversionTable();
        }
        return conversionTable;
    }

    private void updateConversionTable() {
        conversionTable = createConversionTable();
    }

    private ArrayList<Integer> createConversionTable() {
        ArrayList<Integer> table = new ArrayList<Integer>();

        // add top
        table.add(0);

        // add y-coordinate of every line
        int lastY = -1;
        int finalY = -1;
        Document document = textPane.getDocument();
        boolean newline = true;

        for (int i = 0; i < document.getLength(); i++) {
            try {
                if (newline) {
                    newline = false;
                    Rectangle viewRect = textPane.modelToView(i);

                    if (viewRect == null) {
                        // not yet viewed; try again later
                        break;
                    }

                    if (lastY < viewRect.y) {
                        lastY = viewRect.y;
                        finalY = viewRect.y + viewRect.height;
                        table.add(lastY);
                    }
                }
                String text = document.getText(i, 1);
                if (text.equals("\n")) {
                    newline = true;
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        // add bottom of last line
        if (finalY >= 0) {
            table.add(finalY);
        }

        // add bottom of component
        table.add(textPane.getHeight());

        return table;
    }

    public void dispose() {
        setTextComponent(null);
    }

    public void componentShown(ComponentEvent e) {
        // make sure the conversion table is initialized
        updateConversionTable();
    }

    public void componentResized(ComponentEvent e) {
        // update conversion table on resize
        updateConversionTable();
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("document")) {
            updateConversionTable();
        }
    }
}
