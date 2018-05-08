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

import java.beans.*;
import java.util.*;
import javax.swing.event.*;

/**
 * A logical group of internal frames that gets closed in its entirity when the
 * parent frame is closed.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class InternalFrameGroup implements InternalFrameListener {
    private JInternalFrameEx parent;
    private LinkedHashSet<JInternalFrameEx> children;
    private LinkedHashSet<InternalFrameListener> internalFrameListeners;

    public InternalFrameGroup(JInternalFrameEx parent) {
        children = new LinkedHashSet<JInternalFrameEx>();
        internalFrameListeners = new LinkedHashSet<InternalFrameListener>();
        this.parent = parent;
        parent.addInternalFrameListener(this);
    }

    public JInternalFrameEx getParent() {
        return parent;
    }

    public void addChild(JInternalFrameEx child) {
        children.add(child);
        child.setParentFrameGroup(this);
        child.addInternalFrameListener(this);
    }

    public void addInternalFrameListener(InternalFrameListener l) {
        internalFrameListeners.add(l);
    }

    public void internalFrameActivated(InternalFrameEvent e) {
        for (InternalFrameListener l : internalFrameListeners) {
            l.internalFrameActivated(e);
        }
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
        for (InternalFrameListener l : internalFrameListeners) {
            l.internalFrameDeactivated(e);
        }
    }

    public void internalFrameClosed(InternalFrameEvent e) {
        if (e.getSource() == parent) {
            for (JInternalFrameEx child : children) {
                child.removeInternalFrameListener(this);
                child.dispose();
            }
        } else {
            try {
                parent.setSelected(true);
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
                ex.printStackTrace();
            }
        }
        for (InternalFrameListener l : internalFrameListeners) {
            l.internalFrameClosed(e);
        }
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        for (InternalFrameListener l : internalFrameListeners) {
            l.internalFrameClosing(e);
        }
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
        for (InternalFrameListener l : internalFrameListeners) {
            l.internalFrameDeiconified(e);
        }
    }

    public void internalFrameIconified(InternalFrameEvent e) {
        for (InternalFrameListener l : internalFrameListeners) {
            l.internalFrameIconified(e);
        }
    }

    public void internalFrameOpened(InternalFrameEvent e) {
        for (InternalFrameListener l : internalFrameListeners) {
            l.internalFrameOpened(e);
        }
    }
}
