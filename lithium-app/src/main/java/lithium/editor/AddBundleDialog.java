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

import lithium.*;
import lithium.catalog.*;

/**
 * A dialog allowing the user to add a bundle to a catalog.
 *
 * @version 0.9 (2006.02.09)
 * @author Gerrit Meinders
 */
public class AddBundleDialog extends EditBundleDialog {
    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    private MutableCatalog catalog;

    /**
     * Constructs a new add bundle dialog.
     *
     * @param parent the dialog's parent frame
     * @param catalog the catalog to add a bundle to
     */
    public AddBundleDialog(JInternalFrameEx parent, MutableCatalog catalog) {
        super(Resources.get().getString("addBundle.title"), parent, Group
                .createBundle());
        this.catalog = catalog;
    }

    /**
     * Adds a bundle with the specified name and version to the catalog
     * associated with the dialog.
     *
     * @param name bundle name entered in the dialog
     * @param version bundle version entered in the dialog
     */
    @Override
    protected void performOkAction(String name, String version) {
        super.performOkAction(name, version);
        catalog.addBundle(getBundle());
    }
}
