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

package lithium.imagebrowser;

import java.awt.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

import com.github.meinders.common.*;

/**
 * An experimental class for browsing images.
 *
 * @version 0.9 (2006.02.22)
 * @author Gerrit Meinders
 */
public class ImageBrowser {
    public static void main(String[] args) {
        JFrame owner = new JFrame();

        // show browser
        ImageBrowser browser = new ImageBrowser();
        ImageBrowser.Option option = browser.showDialog(owner,
                new java.io.File("J:\\Artwork\\Wallpapers"));
        if (option == ImageBrowser.Option.OK_OPTION) {
            System.out.println(browser.getSelectedFile());
        }

        owner.dispose();
    }

    /**
     * The option selected by the user when closing the browser dialog.
     */
    public enum Option {
        /** Indicates that the user pressed the OK button */
        OK_OPTION,

        /**
         * Indicates that the user pressed the Cancel button or closed the
         * browser dialog through some other means.
         */
        CANCEL_OPTION
    };

    private ExtensionFileFilter imageFilter;

    private File initialFolder = new File(".").getAbsoluteFile();

    private boolean nullImageVisible = true;

    private File selectedFile;

    public ImageBrowser() {
        imageFilter = new ExtensionFileFilter("Afbeeldingen", ImageIO
                .getReaderFormatNames(), false, true);
    }

    public void setFolder(File folder) {
        this.initialFolder = folder;
    }

    /**
     * @return the initialFolder
     */
    public File getInitialFolder() {
        return initialFolder;
    }

    /**
     * @param initialFolder the initialFolder to be set
     */
    public void setInitialFolder(File initialFolder) {
        this.initialFolder = initialFolder;
    }

    /**
     * @return the nullImageVisible
     */
    public boolean isNullImageVisible() {
        return nullImageVisible;
    }

    /**
     * @param selectedFile the selectedFile to be set
     */
    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    public void setNullImageVisible(boolean nullImageVisible) {
        this.nullImageVisible = nullImageVisible;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public Option showDialog(Component owner, File folder) {
        ImageBrowserDialog dialog = createDialog(owner);
        dialog.setFolder(folder);
        dialog.setVisible(true);
        return dialog.getSelectedOption();
    }

    protected ImageBrowserDialog createDialog(Component owner) {
        ImageBrowserDialog dialog;
        if (owner instanceof Frame) {
            dialog = new ImageBrowserDialog(this, (Frame) owner, "Image Browser",
                    true);
        } else {
            dialog = new ImageBrowserDialog(this, (Dialog) owner, "Image Browser",
                    true);
        }
        return dialog;
    }

    /**
     * @return the imageFilter
     */
    public ExtensionFileFilter getImageFilter() {
        return imageFilter;
    }

    /**
     * @param imageFilter the imageFilter to be set
     */
    public void setImageFilter(ExtensionFileFilter imageFilter) {
        this.imageFilter = imageFilter;
    }
}
