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

import com.github.meinders.common.*;
import com.github.meinders.common.FileFilter;
import lithium.*;

/**
 * A file filter that accepts only filestructures containing catalog data stored
 * in the so-called 'classic format', used by OPS.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class ClassicFormatFileFilter extends FileFilter {

    private final FileFilter filter = new CachedFileFilter(
            new ExtensionFileFilter("", "opw", true));

    public ClassicFormatFileFilter() {
        super(Resources.get().getString("classicFormat"));
    }

    public boolean accept(File f) {
        return filter.accept(f);
    }
}
