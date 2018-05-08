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

import java.beans.*;
import javax.swing.tree.*;

import lithium.*;
import lithium.catalog.*;

/**
 * A tree model of the bundles and groups in a catalog.
 *
 * @author Gerrit Meinders
 */
public class CatalogTreeModel extends DefaultTreeModel {
    public static Group getGroup(TreeNode node) {
        if (node instanceof GroupNode) {
            return ((GroupNode) node).getValue();
        } else {
            return null;
        }
    }

    public CatalogTreeModel(Catalog catalog) {
        super(null);
        setRoot(new CatalogNode(this, catalog));
        setAsksAllowsChildren(true);
    }

    private static class CatalogNode extends AbstractTreeNode<Catalog, Group>
            implements PropertyChangeListener {
        public CatalogNode(DefaultTreeModel model, Catalog value) {
            super(model, null, value);
            value.addPropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (MutableCatalog.GROUPS_PROPERTY == e.getPropertyName()) {
                updateChildNodes();
            } else {
                model.nodeChanged(this);
            }
        }

        @Override
        public String toString() {
            return Resources.get().getString("catalogTreeModel.root");
        }

        @Override
        protected AbstractTreeNode<Group, ?> createChildNode(Group child) {
            return new GroupNode(model, this, child);
        }

        @Override
        protected Iterable<Group> getChildren() {
            return value.getGroups();
        }
    }

    private static class GroupNode extends AbstractTreeNode<Group, Group>
            implements PropertyChangeListener {
        public GroupNode(DefaultTreeModel model, TreeNode parent, Group group) {
            super(model, parent, group);
            group.addPropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (Group.GROUPS_PROPERTY == e.getPropertyName()) {
                updateChildNodes();
            } else {
                model.nodeChanged(this);
            }
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        protected AbstractTreeNode<Group, ?> createChildNode(Group child) {
            return new GroupNode(model, this, child);
        }

        @Override
        protected Iterable<Group> getChildren() {
            return value.getGroups();
        }

        @Override
        public boolean getAllowsChildren() {
            return !value.getGroups().isEmpty();
        }
    }
}
