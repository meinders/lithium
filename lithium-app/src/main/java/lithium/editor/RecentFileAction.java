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

import java.awt.event.*;
import java.beans.*;
import java.io.*;
import javax.swing.*;

import com.github.meinders.common.FileFilter;
import lithium.*;

/**
 * An action that re-opens a recently opened file.
 *
 * @version 0.9 (2006.03.12)
 * @author Gerrit Meinders
 */
public class RecentFileAction extends AbstractAction {
    private static final int MAXIMUM_SHORT_NAME_LENGTH = 40;

    private int index;

    private JMenuItem item;

    private EditorFrame editor;

    /**
     * Constructs a new recent file action.
     *
     * @param editor the editor
     * @param index
     * @param item
     */
    public RecentFileAction(EditorFrame editor, int index, JMenuItem item) {
        super();
        this.editor = editor;
        this.index = index;
        this.item = item;

        PropertyChangeListener configListener = new PropertyChangeListener() {
            private Config config = null;

            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName() == Config.RECENT_FILES_PROPERTY) {
                    update();
                } else if (e.getPropertyName() == ConfigManager.CONFIG_PROPERTY) {
                    if (config != null) {
                        config.removePropertyChangeListener(this);
                    }
                    config = ConfigManager.getConfig();
                    config.addPropertyChangeListener(this);
                    update();
                }
            }
        };
        ConfigManager.getConfig().addPropertyChangeListener(configListener);
	    ConfigManager configManager = ConfigManager.getInstance();
        configManager.addPropertyChangeListener(configListener);

        update();
    }

    public void actionPerformed(ActionEvent e) {
        File[] recentFiles = ConfigManager.getConfig().getRecentFiles();
        File file = recentFiles[index];

        FileFilter catalogFilter = FilterManager.getCombinedFilter( FilterType.CATALOG);
        FileFilter playlistFilter = FilterManager.getCombinedFilter( FilterType.PLAYLIST);

        if (catalogFilter.accept(file)) {
            editor.openCatalog(file, catalogFilter);
        } else if (playlistFilter.accept(file)) {
            editor.openPlaylist(file, playlistFilter);
        } else {
            editor.showExceptionDialog(Resources.get().getString(
                    "editorFrame.unknownType", file), null);
        }
    }

    private void update() {
        File[] recentFiles = ConfigManager.getConfig().getRecentFiles();
        if (index < recentFiles.length) {
            putValue(NAME, (index + 1) + ". "
                    + getShortName(recentFiles[index].toString()));
            putValue(MNEMONIC_KEY, '1' + index);
            setEnabled(true);
            item.setVisible(true);
        } else {
            setEnabled(false);
            if (index == 0) {
                putValue(NAME, Resources.get().getString(
                        "editorFrame.recentFiles"));
                item.setVisible(true);
            } else {
                item.setVisible(false);
            }
        }
    }

    private String getShortName(String name) {
        final int length = name.length();
        if (length > MAXIMUM_SHORT_NAME_LENGTH) {
            int skipStart = name.indexOf(File.separatorChar) + 1;
            int skipEnd = name.indexOf(File.separatorChar, skipStart + length
                    - MAXIMUM_SHORT_NAME_LENGTH);
            if (skipStart < skipEnd) {
                // remove the pathname
                return name.substring(0, skipStart) + "..."
                        + name.substring(skipEnd);
            } else {
                // it's an impossibly long filename; truncate from start
                return "..."
                        + name.substring(length - MAXIMUM_SHORT_NAME_LENGTH);
            }
        } else {
            // the name is already short enough
            return name;
        }
    }
}
