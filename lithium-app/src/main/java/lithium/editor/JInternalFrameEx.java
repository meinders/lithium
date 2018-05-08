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

import javax.swing.*;
import javax.swing.event.*;

/**
 * An extension of the JInternalFrame that allows for hierarchical grouping
 * of frames. When frames are grouped, closing a parent frame will
 * automatically close all of the frame's children.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class JInternalFrameEx extends JInternalFrame {
    private JButton defaultButton = null;
    private InternalFrameGroup group = new InternalFrameGroup(this);
    private InternalFrameGroup parentGroup = null;

    public JInternalFrameEx(JInternalFrameEx parent, String title) {
        this(parent == null ? null : parent.getFrameGroup(), title);
    }

    public JInternalFrameEx(InternalFrameGroup parentGroup, String title) {
        super(title);
        if (parentGroup != null) {
            parentGroup.addChild(this);
        }
        addInternalFrameListener(new DefaultButtonHelper());
    }

    public void setDefaultButton(JButton defaultButton) {
        this.defaultButton = defaultButton;
    }

    public JButton getDefaultButton() {
        return defaultButton;
    }

    public InternalFrameGroup getFrameGroup() {
        return group;
    }

    public void setParentFrameGroup(InternalFrameGroup parentGroup) {
        assert this.parentGroup == null : this + " is already a child of " +
                this.parentGroup.getParent() + " (" + this.parentGroup + ")";
        this.parentGroup = parentGroup;
    }

    public InternalFrameGroup getParentFrameGroup() {
        return parentGroup;
    }

    private class DefaultButtonHelper extends InternalFrameAdapter {
        public void internalFrameActivated(InternalFrameEvent e) {
            if (defaultButton != null) {
                rootPane.setDefaultButton(defaultButton);
            }
        }
    }
}
