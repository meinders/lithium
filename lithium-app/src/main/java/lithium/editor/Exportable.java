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

import com.github.meinders.common.FileFilter;

/**
 * An interface for objects that can be exported to a file.
 *
 * @version 0.9 (2006.02.06)
 * @author Gerrit Meinders
 */
public interface Exportable {
    /**
     * Returns a FileFilter for each of the file formats that can be exported
     * by this class.
     *
     * @return an array of FileFilter objects
     */
    public FileFilter[] getExportFilters();

    /**
     * Exports data to the file in the format implyric by the specified
     * filter. Note that the format to be used is said to be implyric,
     * because a FileFilter does not specify the actual format.
     *
     * @param file the file to be exported
     * @param filter the filter indicating the format to be used
     * @throws ExportException if exporting fails
     */
    public void export(File file, FileFilter filter) throws ExportException;
}

