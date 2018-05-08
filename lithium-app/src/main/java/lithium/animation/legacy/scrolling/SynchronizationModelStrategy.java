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
 * Conversion strategy based on a {@link SynchronizationModel}, allowing
 * perfect synchronization between views without relying on newlines. The only
 * disadvantage of this strategy is, that line wrapping differences between
 * multiple view cause additional delays when scrolling.
 *
 * @author Gerrit Meinders
 */
public class SynchronizationModelStrategy implements ConversionStrategy,
        ComponentListener, PropertyChangeListener {
    private final SynchronizationModel model;

    private final JTextPane textPane;

    private boolean topMarginFixed;

    private SortedSet<Integer> synchPoints;

    /**
     * Constructs a new strategy based on the specified synchronization model.
     *
     * @param model the synchronization model
     * @param textPane the view
     */
    public SynchronizationModelStrategy(SynchronizationModel model,
            JTextPane textPane) {
        if (model == null) {
            throw new NullPointerException("model");
        }
        if (textPane == null) {
            throw new NullPointerException("textPane");
        }

        this.model = model;
        this.textPane = textPane;

        synchPoints = new TreeSet<Integer>();

        textPane.addComponentListener(this);
        textPane.addPropertyChangeListener(this);
    }

    public void setTopMarginFixed(boolean topMarginFixed) {
        this.topMarginFixed = topMarginFixed;
    }

    private void updateSynchPoints() {
        Set<Integer> oldSynchPoints = new HashSet<Integer>(synchPoints);

        synchPoints.clear();
        Document document = textPane.getDocument();

        int lastY = -1;
        for (int i = 0; i < document.getLength(); i++) {
            int y = synchToView(i);
            if (y != lastY) {
                lastY = y;
                synchPoints.add(i);
            }
        }

        model.replacePoints(oldSynchPoints, synchPoints);
    }

    private int synchToView(int value) {
        try {
            Rectangle viewRect = textPane.modelToView(value);
            int result;
            if (viewRect == null) {
                result = 0;
            } else {
                result = viewRect.y;
                if (topMarginFixed) {
                    result -= textPane.modelToView(0).y;
                }
            }
            return result;
        } catch (BadLocationException e) {
            System.out.println("synchToView(" + value + ")");
            throw new AssertionError(e);
        }
    }

    private int viewToSynch(int value) {
        int correctedValue;
        if (topMarginFixed) {
            try {
                correctedValue = value + textPane.modelToView(0).y;
            } catch (BadLocationException e) {
                throw new AssertionError(e);
            }
        } else {
            correctedValue = value;
        }

        int result = 0;
        for (Integer synchPoint : synchPoints) {
            try {
                if (textPane.modelToView(synchPoint).y >= correctedValue) {
                    break;
                }
                result = synchPoint;
            } catch (BadLocationException e) {
                throw new AssertionError(e);
            }
        }
        return result;
    }

    public int modelToView(float value) {
        // discrete logical points surrounding actual value
        int logical1 = (int) value;
        int logical2 = (int) (value + 1f);

        // shared synch points for each logical point
        int synch1 = model.getPoint(logical1);
        int synch2 = model.getPoint(logical2);

        // actual synch point (fractional)
        float synch;
        {
            float weight = value - logical1;
            synch = synch1 * (1f - weight) + synch2 * weight;
        }

        // nearest local synch points
        Document document = textPane.getDocument();

        SortedSet<Integer> headSet = synchPoints.headSet(synch1 + 1);
        int localSynch1 = headSet.isEmpty() ? 0 : headSet.last();

        SortedSet<Integer> tailSet = synchPoints.tailSet(synch2);
        int localSynch2 = tailSet.isEmpty() ? document.getLength() : tailSet
                .first();

        // associated view coordinates
        int view1 = synchToView(localSynch1);
        int view2 = synchToView(localSynch2);

        // interpolate view coordinate for actual synch point
        int view;
        if (view1 == view2) {
            view = view1;
        } else {
            float weight = (synch - localSynch1) / (localSynch2 - localSynch1);
            view = (int) (view1 * (1f - weight) + view2 * weight);
        }

        return view;
    }

    public float viewToModel(int value) {
        int actualValue = value < 0 ? 0 : value;
        // find synch points surrounding value
        int synch1 = viewToSynch(actualValue);

        float result;

        SortedSet<Integer> tailSet = synchPoints.tailSet(synch1 + 1);
        if (tailSet.isEmpty()) {
            result = model.getIndex(synch1);
        } else {
            int synch2 = tailSet.first();

            // associated view coordinates
            int view1 = synchToView(synch1);
            int view2 = synchToView(synch2);

            assert (actualValue >= view1 && actualValue <= view2) : "value="
                    + actualValue + " outside of [" + view1 + "," + view2 + "]";

            int index1 = model.getIndex(synch1);
            int index2 = model.getIndex(synch2);

            // derive logical point
            float weight = (float) (actualValue - view1) / (view2 - view1);
            weight *= (index2 - index1);
            result = index1 + weight;
        }

        return result;
    }

    public void dispose() {
        // ignored
    }

    public void componentHidden(ComponentEvent e) {
        // ignored
    }

    public void componentMoved(ComponentEvent e) {
        // ignored
    }

    public void componentResized(ComponentEvent e) {
        updateSynchPoints();
    }

    public void componentShown(ComponentEvent e) {
        updateSynchPoints();
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("document")) {
            updateSynchPoints();
        }
    }
}
