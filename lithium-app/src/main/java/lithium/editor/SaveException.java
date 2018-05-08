/*
 * Copyright 2008 Gerrit Meinders
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

/**
 * An interface for objects that can be saved to a file.
 *
 * @version 0.9 (2006.02.22)
 * @author Gerrit Meinders
 */
public class SaveException extends Exception {
    private Savable source;

    public SaveException(Savable source) {
        this(source, null);
    }

    public SaveException(Savable source, Throwable cause) {
        this(source, cause, null);
    }

    public SaveException(Savable source, Throwable cause, String message) {
        super(message, cause);
        this.source = source;
    }

    public Savable getSource() {
        return source;
    }
}

