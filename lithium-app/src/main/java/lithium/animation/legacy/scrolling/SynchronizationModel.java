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

import java.util.*;

/**
 * UNITS
 *
 * view units actual scrolling of GUI components, values of scroll bars logical
 * units scale-independent scrolling at proper scroll rate model units position
 * in the content object, e.g. character index in a text
 *
 *
 * SYNCHRONIZATION
 *
 * The system uses one logical unit per synchronization point, which is
 * expressed in model units, but based on the view, e.g. per line. As such,
 * synch points may differ per view. To overcome this, views share a single
 * model that keeps track of all their respective synch points.
 *
 *
 * LOGICAL UNIT MAPPING
 *
 * All views must use the same logical unit to model unit mappings.
 *
 * [0 <=> 0] implicit 10 <=> 1 15 <=> 2 20 <=> 3 25 <=> 4 30 <=> 5 40 <=> 6 [n
 * <=> n] implicit
 *
 *
 * VIEW MAPPING
 *
 * local synch point X View(X) = component.modelToView(X).y
 *
 * local synch points A and B; remote synch point or in-between point X View(X) = (
 * (X - A) * View(A) + (B - X) * View(B) ) / (B - A)
 *
 * @author Gerrit Meinders
 */
public class SynchronizationModel {
    private final List<Integer> points;

    private final List<Integer> sortedPoints;

    public SynchronizationModel() {
        points = new ArrayList<Integer>();
        sortedPoints = new ArrayList<Integer>();
    }

    public synchronized void addPoints(Set<Integer> points) {
        this.points.addAll(points);
        updateSortedPoints();
    }

    public synchronized void removePoints(Set<Integer> points) {
        this.points.removeAll(points);
        updateSortedPoints();
    }

    public synchronized void replacePoints(Set<Integer> oldSynchPoints,
            Set<Integer> newSynchPoints) {
        this.points.removeAll(oldSynchPoints);
        this.points.addAll(newSynchPoints);
        updateSortedPoints();
    }

    public synchronized int getPoint(int index) {
        int result;
        if (sortedPoints.isEmpty()) {
            result = 0;
        } else if (index >= sortedPoints.size()) {
            result = sortedPoints.get(sortedPoints.size() - 1);
        } else {
            result = sortedPoints.get(index);
        }
        return result;
    }

    public synchronized int getIndex(int point) {
        return Collections.binarySearch(sortedPoints, point);
    }

    private void updateSortedPoints() {
        sortedPoints.clear();
        sortedPoints.addAll(new TreeSet<Integer>(points));
    }

    public synchronized int getPointCount() {
        return sortedPoints.size();
    }
}
