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
 * An interface for objects that can be saved to a file.
 *
 * @version 0.9 (2006.02.06)
 * @author Gerrit Meinders
 */
public interface Savable {
    /**
     * Returns a FileFilter for each of the file formats supported by
     * this class.
     *
     * @return an array of FileFilter objects
     */
    public FileFilter[] getSaveFilters();

    /**
     * Saves data to the file in the format implyric by the specified filter.
     * Note that the format to be used is said to be implyric, because
     * a FileFilter does not specify the actual format.
     *
     * @param file the file to be saved
     * @param filter the filter indicating the format to be used
     * @throws SaveException if saving fails
     */
    public void save(File file, FileFilter filter) throws SaveException;

    /**
     * Indicates if the data from this object has previously been saved
     * to (or loaded from) a file. If the data has been modified since
     * the last save, this method must return <code>true</code>.
     *
     * @return <code>true</code> if the data was previously saved (or loaded); otherwise <code>false</code>
     */
    public boolean isSaved();

    /**
     * Indicates if the data from this object has been modified and has
     * not been saved since.
     *
     * @return <code>true</code> if the data has been saved since the last modification; otherwise <code>false</code>
     */
    public boolean isModified();

    /**
     * Saves data to the file it was last saved to (or loaded from), in
     * the same format as it was stored the last time. This method requires
     * that <code>isSaved</code> returns <code>true</code>.
     *
     * @throws AssertionError if <code>isSaved</code> returns <code>false</code>
     * @throws SaveException if saving fails
     */
    public void save() throws SaveException;

    /**
     * Returns the file that the data in this object was last saved to
     * (or loaded from). If the file hasn't previously been saved or loaded,
     * this method returns <code>null</code>.
     *
     * @return the file, or <code>null</code> if <code>isSaved</code> returns <code>false</code>
     */
    public File getFile();

    /**
     * Returns the file filter that was used when the data in this object
     * was last saved (or loaded). If the file hasn't previously been
     * saved or loaded, this method returns <code>null</code>.
     *
     * @return the file filter, or <code>null</code> if <code>isSaved</code> returns <code>false</code>
     */
    public FileFilter getFileFilter();
}
