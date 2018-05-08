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

import java.nio.*;
import java.util.*;
import javax.swing.tree.*;

/**
 * An abstract tree node used to facilitate implementation of a tree backed by
 * an existing model. Concrete sub-classes provide child node value iteration
 * and child node creation and, optionally, notify the node of changes made to
 * the model.
 *
 * @param <T> the value type of the node
 * @param <C> the value type of the child nodes
 *
 * @author Gerrit Meinders
 */
public abstract class AbstractTreeNode<T, C> implements TreeNode {
    /**
     * The tree model that the node is a part of.
     */
    protected final DefaultTreeModel model;

    /**
     * The value represented by the tree node.
     */
    protected final T value;

    private final TreeNode parent;

    private final List<AbstractTreeNode<C, ?>> childNodes;

    /**
     * Constructs a new tree node as part of the given model with the specified
     * parent node and value.
     *
     * @param model the model that the node is a part of
     * @param parent the parent node
     * @param value the node's value
     */
    public AbstractTreeNode(DefaultTreeModel model, TreeNode parent, T value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        this.model = model;
        this.value = value;
        this.parent = parent;
        childNodes = new ArrayList<AbstractTreeNode<C, ?>>();
        createChildNodes();
    }

    /**
     * Returns the value represented by the node.
     *
     * @return the node's value
     */
    public T getValue() {
        return value;
    }

    /**
     * Returns the values for which child nodes should be added to this node.
     *
     * @return the child node values
     */
    protected abstract Iterable<C> getChildren();

    /**
     * Creates a child node for the given child value.
     *
     * @param child the value to create a node for
     * @return the created node
     */
    protected abstract AbstractTreeNode<C, ?> createChildNode(C child);

    private void createChildNodes() {
        childNodes.clear();
        for (C child : getChildren()) {
            childNodes.add(createChildNode(child));
        }
    }

    /**
     * Updates the node's child nodes to match any changes that may have occured
     * in the underlying data model.
     */
    protected void updateChildNodes() {
        Iterator<C> valueIter = getChildren().iterator();
        ListIterator<AbstractTreeNode<C, ?>> nodeIter = childNodes.listIterator();

        IntBuffer changedBuffer = IntBuffer.allocate(childNodes.size());

        for (; valueIter.hasNext() && nodeIter.hasNext();) {
            final C value = valueIter.next();
            final AbstractTreeNode<C, ?> node = nodeIter.next();
            if (value != node.value) {
                nodeIter.set(createChildNode(value));
                changedBuffer.put(nodeIter.previousIndex());
                System.out.println("Changed: " + nodeIter.previousIndex() + " "
                        + value + " <- " + node.value);
            }
        }

        final int changedCount = changedBuffer.position();
        changedBuffer.rewind();
        int[] changed = new int[changedCount];
        changedBuffer.get(changed);
        model.nodesChanged(this, changed);

        if (valueIter.hasNext()) {
            int oldSize = childNodes.size();
            while (valueIter.hasNext()) {
                final C next = valueIter.next();
                System.out.println("Added = " + next + " (" + childNodes.size()
                        + ")");
                nodeIter.add(createChildNode(next));
            }
            model.nodesWereInserted(this, createSequence(oldSize, childNodes
                    .size()));

        } else if (nodeIter.hasNext()) {
            final int size = childNodes.size();
            final int firstRemoved = nodeIter.nextIndex();
            final List<AbstractTreeNode<C, ?>> removed = childNodes.subList(
                    firstRemoved, size);
            System.out.println("Removed: " + removed);
            removed.clear();
            model.nodesWereRemoved(this, createSequence(firstRemoved, size),
                    removed.toArray());
        }
    }

    private int[] createSequence(int start, int end) {
        final int[] result = new int[end - start];
        for (int i = 0; i < result.length; i++) {
            result[i] = start + i;
        }
        return result;
    }

    public Enumeration<AbstractTreeNode<C, ?>> children() {
        return Collections.enumeration(childNodes);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public TreeNode getChildAt(int childIndex) {
        return childNodes.get(childIndex);
    }

    public int getChildCount() {
        return childNodes.size();
    }

    public int getIndex(TreeNode node) {
        return childNodes.indexOf(node);
    }

    public TreeNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return false;
    }
}
