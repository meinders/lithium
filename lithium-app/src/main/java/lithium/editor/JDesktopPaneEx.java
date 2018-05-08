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
 * An extension of the JDesktopPane that has better support for use inside a
 * scroll pane and automatically sets the position of newly added frames.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class JDesktopPaneEx extends JDesktopPane {
    public static final int DEFAULT = 0;
    public static final int CENTER = 1;

    private static int offset = 0;

    private MDIDesktopManager manager;

    public JDesktopPaneEx() {
        super();
        manager = new MDIDesktopManager(this);
        setDesktopManager(manager);
    }

    public Component add(Component component) {
        try {
            if (component instanceof JInternalFrame) {
                return add((JInternalFrame) component, DEFAULT);
            } else {
                return super.add(component);
            }
        } finally {
            manager.resizeDesktop();
        }
    }

    public Component add(JInternalFrame internalFrame, int position) {
        switch (position) {
        case DEFAULT:
            internalFrame.setLocation(offset * 30, offset * 30);
            offset++;
            offset %= 10;
            break;

        case CENTER:
            internalFrame.setLocation(
                    (getWidth() - internalFrame.getWidth()) / 2,
                    (getHeight() - internalFrame.getHeight()) / 2);
            break;
        }
        try {
            return super.add(internalFrame);
        } finally {
            manager.resizeDesktop();
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        manager.resizeDesktop();
    }

    public void remove(Component comp) {
        super.remove(comp);
        manager.resizeDesktop();
    }

    /**
     * Sets all component size properties ( maximum, minimum, preferred)
     * to the given dimension.
     * (Source: http://www.javaworld.com/javaworld/jw-05-2001/jw-0525-mdi.html)
     */
    public void setAllSize(Dimension d){
        setMinimumSize(d);
        setMaximumSize(d);
        setPreferredSize(d);
    }

    /**
     * Sets all component size properties ( maximum, minimum, preferred)
     * to the given width and height.
     * (Source: http://www.javaworld.com/javaworld/jw-05-2001/jw-0525-mdi.html)
     */
    public void setAllSize(int width, int height){
        setAllSize(new Dimension(width,height));
    }

    /**
     * Private class used to replace the standard DesktopManager for JDesktopPane.
     * Used to provide scrollBar functionality.
     * (Source: http://www.javaworld.com/javaworld/jw-05-2001/jw-0525-mdi.html)
     */
    private class MDIDesktopManager extends DefaultDesktopManager {
        private JDesktopPaneEx desktop;

        public MDIDesktopManager(JDesktopPaneEx desktop) {
            this.desktop = desktop;
        }

        public void endResizingFrame(JComponent f) {
            super.endResizingFrame(f);
            resizeDesktop();
        }

        public void endDraggingFrame(JComponent f) {
            super.endDraggingFrame(f);
            resizeDesktop();
        }

        public void setNormalSize() {
            JScrollPane scrollPane=getScrollPane();
            int x = 0;
            int y = 0;
            Insets scrollInsets = getScrollPaneInsets();

            if (scrollPane != null) {
                Dimension d = scrollPane.getVisibleRect().getSize();
                if (scrollPane.getBorder() != null) {
                   d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                             d.getHeight() - scrollInsets.top - scrollInsets.bottom);
                }

                d.setSize(d.getWidth() - 20, d.getHeight() - 20);
                desktop.setAllSize(x,y);
                scrollPane.invalidate();
                scrollPane.validate();
            }
        }

        private Insets getScrollPaneInsets() {
            JScrollPane scrollPane=getScrollPane();
            if (scrollPane==null) return new Insets(0,0,0,0);
            else return getScrollPane().getBorder().getBorderInsets(scrollPane);
        }

        private JScrollPane getScrollPane() {
            if (desktop.getParent() instanceof JViewport) {
                JViewport viewPort = (JViewport)desktop.getParent();
                if (viewPort.getParent() instanceof JScrollPane)
                    return (JScrollPane)viewPort.getParent();
            }
            return null;
        }

        protected void resizeDesktop() {
            int x = 0;
            int y = 0;
            JScrollPane scrollPane = getScrollPane();
            Insets scrollInsets = getScrollPaneInsets();

            if (scrollPane != null) {
                JInternalFrame allFrames[] = desktop.getAllFrames();
                for (int i = 0; i < allFrames.length; i++) {
                    if (allFrames[i].getX()+allFrames[i].getWidth()>x) {
                        x = allFrames[i].getX() + allFrames[i].getWidth();
                    }
                    if (allFrames[i].getY()+allFrames[i].getHeight()>y) {
                        y = allFrames[i].getY() + allFrames[i].getHeight();
                    }
                }
                Dimension d=scrollPane.getVisibleRect().getSize();
                if (scrollPane.getBorder() != null) {
                   d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                             d.getHeight() - scrollInsets.top - scrollInsets.bottom);
                }

                if (x <= d.getWidth()) x = ((int)d.getWidth()) - 20;
                if (y <= d.getHeight()) y = ((int)d.getHeight()) - 20;
                desktop.setAllSize(x,y);
                scrollPane.invalidate();
                scrollPane.validate();
            }
        }
    }
}
