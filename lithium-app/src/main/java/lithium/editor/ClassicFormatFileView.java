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

import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import com.github.meinders.common.*;
import com.github.meinders.common.FileFilter;

/**
 * A file view to provide an intuitive means to select catalogs stored in the
 * classic format, which consists of many files and directories for a single
 * catalog.
 *
 * @author Gerrit Meinders
 */
public class ClassicFormatFileView extends FileView {
    private Icon importIcon;

    private final FileFilter filter = new CachedFileFilter(
            new ExtensionFileFilter("", "opw", false));

    /**
     * Constructs a new file view.
     */
    public ClassicFormatFileView() {
        importIcon = getIcon("/toolbarButtonGraphics/general/Import24.gif");
    }

    @Override
    public Icon getIcon(File f) {
        if (f.isDirectory() && !isRoot(f)) {
            File[] files = f.listFiles(filter);
            if (files == null) {
                return null;
            } else {
                if (files.length == 0) {
                    return null;
                } else {
                    return importIcon;
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public Boolean isTraversable(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles(filter);
            return files != null && files.length == 0;
        } else {
            return !file.isFile();
        }
    }

    private ImageIcon getIcon(String resource) {
        try {
            return new ImageIcon(ImageIO.read(getClass().getResource(resource)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isRoot(File f) {
        File[] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].equals(f))
                return true;
        }
        return false;
    }
}
