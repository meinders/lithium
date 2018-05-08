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

package lithium.powerpoint;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * This class provides access to native windows, running outside of the Java
 * application. Currenly only the Windows operating system is supported.
 *
 * @version 0.9x (2005.09.06)
 * @author Gerrit Meinders
 */
public class NativeWindow {
    static {
        File file = new File("lib", System.mapLibraryName("ppt"));
        System.load(file.getAbsolutePath());
    }

    /**
     * Returns the currently active window.
     *
     * @return the window
     */
    public static NativeWindow getForegroundWindow() {
        int hwnd = getForegroundWindowHwnd();
        return hwnd == 0 ? null : new NativeWindow(hwnd);
    }

    /**
     * Returns the names of all windows.
     *
     * @return the window names
     */
    public static List<String> getWindowTitles() {
        String[] titles = getWindowTitlesImpl();
        List<String> titleList = new ArrayList<String>(titles.length);
        for (String title : titles) {
            titleList.add(title);
        }
        return titleList;
    }

    /**
     * Returns the names of all windows.
     *
     * @return the window names
     */
    private static native String[] getWindowTitlesImpl();

    /**
     * Returns the first window found that has a title containing the given
     * title fragment.
     *
     * @param title the title fragment to find
     * @return the window
     */
    public static NativeWindow getWindowByTitle(String title) {
        int hwnd = getWindowHwndByTitle(title);
        return hwnd == 0 ? null : new NativeWindow(hwnd);
    }

    /**
     * Returns the HWND of the currently active window.
     *
     * @return the window's HWND
     */
    private static native int getForegroundWindowHwnd();

    /**
     * Returns the HWND of the first window found that has a title containing
     * the given title fragment.
     *
     * @param title the title fragment to find
     * @return the window's HWND
     */
    private static native int getWindowHwndByTitle(String title);

    /** The window's HWND. */
    private int hwnd;

    /**
     * Creates a new native window instance for the window with the given HWND.
     *
     * @param hwnd the HWND
     */
    private NativeWindow(int hwnd) {
        this.hwnd = hwnd;
    }

    /**
     * Sets the bounds of the window to the given bounds.
     *
     * @param bounds the bounds
     */
    public void setBounds(Rectangle bounds) {
        setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Returns the text in the native window's title bar.
     *
     * @return the title
     */
    public native String getTitle();

    /**
     * Sets the bounds of the window to the given bounds.
     *
     * @param x the x coordinate of the top-left corner
     * @param y the y coordinate of the top-left corner
     * @param width the width to be set
     * @param height the height to be set
     */
    private native void setBounds(int x, int y, int width, int height);

    public String toString() {
        return super.toString() + "[hwnd=" + hwnd + "]";
    }

    /**
     * Requests that the window receive be made the focused window.
     */
    public native void requestFocus();

    /**
     * If the window is minimized or maximized, the system restores it to its
     * original size and position.
     */
    public native void restore();

    /**
     * Maximizes the window.
     */
    public native void maximize();
}

