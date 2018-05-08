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

package lithium;

import java.awt.*;

/**
 * A class containing display configuration settings.
 *
 * @version 0.9 (2006.03.11)
 * @author Gerrit Meinders
 */
public class DisplayConfig implements Cloneable {
    public static final String EDITOR_MODE = "editor";
    public static final String PRESENTATION_MODE = "presentation";

    private GraphicsDevice device = null;
    private DisplayMode displayMode = null;

    public static DisplayConfig createDefaultConfig() {
        DisplayConfig defaultConfig = new DisplayConfig();
        return defaultConfig;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
        /*
        Implementation note: GraphicsDevice and DisplayMode objects are
        immutable and therefore don't need to be cloned.
        */
    }

    public void setDevice(String deviceId) {
        GraphicsEnvironment graphEnv =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = graphEnv.getScreenDevices();
        GraphicsDevice device = null;
        for (int i=0; i<devices.length; i++) {
            if (devices[i].getIDstring().equals(deviceId)) {
                device = devices[i];
                break;
            }
        }
        setDevice(device);
    }

    public void setDevice(GraphicsDevice device) {
        this.device = device;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public GraphicsDevice getDevice() {
        if (isDeviceSet()) {
            return device;
        } else {
            GraphicsEnvironment graphEnv =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            return graphEnv.getDefaultScreenDevice();
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        return getDevice().getDefaultConfiguration();
    }

    public boolean isDeviceSet() {
        return device != null;
    }

    public boolean isDisplayModeSet() {
        return displayMode != null;
    }
}

