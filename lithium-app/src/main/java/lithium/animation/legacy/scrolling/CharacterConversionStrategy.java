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
 * <p>This class provides a conversion strategy that uses logical units based on
 * the number of characters on a line.
 *
 * <p>This strategy causes every line to scroll at a rate relative to its
 * length. This behavior may be undesirable in some cases. Therefore, an
 * alternative line-based strategy is provided by the
 * {@link LineConversionStrategy}.
 *
 * <p>To prevent instant skipping of empty lines, any empty line is given a size
 * of 1 logical unit. Top and bottom margins are given the same logical size.
 *
 * @see AdvancedTextRangeModel
 *
 * @version 0.9x (2005.08.04)
 * @author Gerrit Meinders
 */
public class CharacterConversionStrategy implements ConversionStrategy,
        ComponentListener, PropertyChangeListener {
    /** The default value for the unitSize property. */
    private static final float DEFAULT_UNIT_SIZE = 100f;

    private JTextPane textPane;
    private ArrayList<Integer> conversionTable;
    private HashMap<Integer,Float> lineScales;

    /** The number of characters in one logical unit. */
    private float unitSize = DEFAULT_UNIT_SIZE;

    private boolean topMarginFixed = false;

    /**
     * Constructs a new character-based conversion strategy with no associated
     * text component. A text component must be set before the strategy can be
     * used to convert units.
     */
    public CharacterConversionStrategy() {
    }

    /**
     * Constructs a new character-based conversion strategy associated to the
     * given text component.
     *
     * @param textPane the text component
     */
    public CharacterConversionStrategy(JTextPane textPane) {
        setTextComponent(textPane);
    }

    /**
     * Sets whether the top margin should be ignored.
     */
    public void setTopMarginFixed(boolean topMarginFixed) {
        boolean changed = topMarginFixed != isTopMarginFixed();
        this.topMarginFixed = topMarginFixed;
        if (changed) {
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
     * Sets the size of a logical unit.
     *
     * @param unitSize the size of a unit in characters
     */
    public void setUnitSize(int unitSize) {
        this.unitSize = unitSize;
    }

    /**
     * Returns the size of a logical unit.
     *
     * @return the size of a unit in characters
     */
    public int getUnitSize() {
        return (int) unitSize;
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

        // get and unscale logical units
        int line = 0;
        float fraction = 0f;
        for (int i=0; i<lineScales.size(); i++) {
            float lineScale = lineScales.get(i);
            if (value > lineScale) {
                value -= lineScale;
                line++;
            } else {
                // unscale fraction
                fraction = value / lineScale;
                break;
            }
        }

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
        for (int i=1; i<table.size(); i++) {
            if (value < table.get(i)) {
                float start = table.get(i - 1);
                float height = table.get(i) - start;
                int line = i - 1;
                float fraction = (value - start) / height;

                // scale logical units
                float scaledLine = 0f;
                for (int j=0; j<line; j++) {
                    scaledLine += lineScales.get(j);
                }
                float scaledFraction = fraction * lineScales.get(line);
                modelValue = scaledLine + scaledFraction;
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
        createConversionTable();
    }

    private void createConversionTable() {
        ArrayList<Integer> table = new ArrayList<Integer>();
        HashMap<Integer,Float> lineScales = new HashMap<Integer,Float>();

        // add top
        table.add(0);

        // add y-coordinate of every line
        int lastY = -1;
        int finalY = -1;
        Document document = textPane.getDocument();
        boolean newline = true;
        int lineIndex = 0;
        int lineSize = 0;
        for (int i=0; i<document.getLength(); i++) {
            try {
                if (newline) {
                    newline = false;
                    Rectangle viewRect = textPane.modelToView(i);
                    lastY = viewRect.y;
                    finalY = viewRect.y + viewRect.height;

                    table.add(lastY);

                    if (lineSize == 0) {
                        lineScales.put(lineIndex, 1f);
                    } else {
                        lineScales.put(lineIndex, lineSize / unitSize);
                    }
                    lineIndex++;
                    lineSize = 0;

                } else {
                    lineSize++;
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

            if (lineSize == 0) {
                lineScales.put(lineIndex, 1f);
            } else {
                lineScales.put(lineIndex, lineSize / unitSize);
            }
            lineIndex++;
        }

        // add bottom of component
        table.add(textPane.getHeight());
        lineScales.put(lineIndex, 1f);

        // set instance variables
        this.conversionTable = table;
        this.lineScales = lineScales;
    }

    public void dispose() {
        setTextComponent(null);
    }

    public void componentShown(ComponentEvent e) {
        // make sure the conversion table is initialized
        getConversionTable();
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

