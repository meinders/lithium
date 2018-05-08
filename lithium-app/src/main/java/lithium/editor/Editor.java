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

import java.util.*;

/**
 * Provides an interface for editors, allowing a context to present the editor's
 * common operations, such as save/export, copy/paste, and so on.
 *
 * @author Gerrit Meinders
 */
public interface Editor {
    /**
     * Returns the editor's context.
     *
     * @return the editor context
     */
    public EditorContext getEditorContext();

    /**
     * Sets the editor context for this editor.
     *
     * @param editorContext the context to be set
     */
    public void setEditorContext(EditorContext editorContext);

    /**
     * Returns a collection of the items currently selected in the editor. If no
     * items are selected, an empty collection is returned.
     *
     * @return the selected items
     */
    public Collection<Object> getSelectedItems();

    /**
     * Returns whether the given object is the object being edited by the
     * editor.
     *
     * @param edited The object to check.
     * @return <code>true</code> if the editor edits the given object;
     *         <code>false</code> otherwise.
     */
    public boolean isEditorFor(Object edited);
}
