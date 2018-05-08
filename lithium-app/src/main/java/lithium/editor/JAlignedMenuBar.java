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

import java.util.*;
import javax.swing.*;

/**
 * <p>An implementation of a menu bar that allows for ordering of menus by means
 * of an alignment constant. Menus with a lower alignment will appear first,
 * followed by menus with a higher alignment. The order of menus with the same
 * alignments is identical to the menus' insertion-order.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class JAlignedMenuBar extends JMenuBar {
    private TreeSet<JMenu> menus;
    private HashMap<JMenu,Integer> alignments;
    private ArrayList<JMenu> insertionOrder;

    /**
     * Creates a new aligned menu bar.
     */
    public JAlignedMenuBar() {
        super();
        menus = new TreeSet<JMenu>(new Comparator<JMenu>() {
            public int compare(JMenu o1, JMenu o2) {
                assert alignments.containsKey(o1);
                assert alignments.containsKey(o2);
                Integer v1 = alignments.get(o1);
                Integer v2 = alignments.get(o2);
                if (v1 == v2) {
                    return insertionOrder.indexOf(o1) -
                            insertionOrder.indexOf(o2);
                } else {
                    return v1 - v2;
                }
            }});
        alignments = new HashMap<JMenu,Integer>();
        insertionOrder = new ArrayList<JMenu>();
    }

    /**
     * Adds the specified menu to the menu bar with an alignment of 0.
     *
     * @param menu the <code>JMenu</code> component to add
     * @return the menu component
     */
    public JMenu add(JMenu menu) {
        return add(menu, 0);
    }

    /**
     * Adds the specified menu to the menu bar with the given alignment.
     *
     * @param menu the <code>JMenu</code> component to add
     * @param alignment the alignment
     * @return the menu component
     */
    public JMenu add(JMenu menu, int alignment) {
        // remove menu from bar if present
        if (alignments.containsKey(menu)) {
            remove(menu);
        }

        // store positional data
        alignments.put(menu, alignment);
        insertionOrder.add(menu);

        // remove the menus with higher alignments
        JMenu[] afterMenus = menus.tailSet(menu).toArray(new JMenu[0]);
        for (JMenu afterMenu : afterMenus) {
            // only the component is removed, not the positional information
            super.remove(afterMenu);
        }

        // add the menu
        super.add(menu);
        menus.add(menu);

        // append the previously removed menus
        for (JMenu afterMenu : afterMenus) {
            super.add(afterMenu);
        }

        return menu;
    }

    /**
     * Removes the specified menu from the menu bar.
     *
     * @param menu the menu to remove
     */
    public void remove(JMenu menu) {
        super.remove(menu);
        menus.remove(menu);
        alignments.remove(menu);
        insertionOrder.remove(menu);
    }
}

