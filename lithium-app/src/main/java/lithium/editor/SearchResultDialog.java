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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

import com.github.meinders.common.*;
import lithium.*;
import lithium.catalog.*;
import lithium.search.*;

/**
 * A dialog displaying the results of a search and allowing the user to perform
 * some action on the items found.
 *
 * @version 0.9 (2006.12.26)
 * @author Gerrit Meinders
 */
public class SearchResultDialog extends JInternalFrameEx
        implements ListSelectionListener {
    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    private List<SearchResult> searchResults;

    private Action addAction;
    private Action closeAction;

    private JTable resultsTable;
    private JTextPane detailsTextPane;

    public SearchResultDialog(JInternalFrameEx parent,
            Collection<SearchResult> searchResults,
            Action addAction) {
        super(parent, Resources.get().getString("searchResultDialog.dialogtitle"));
        this.addAction = addAction;
        this.searchResults = new ArrayList<SearchResult>(searchResults);
        init();
    }

    public void valueChanged(ListSelectionEvent e) {
        updateDetails();
    }

    public JTable getResultsTable() {
        return resultsTable;
    }

    private void init() {
        setResizable(false);
        setClosable(true);
        setMaximizable(false);
        setIconifiable(false);

        createActions();
        setContentPane(createContentPane());
        pack();
        show();
    }

    public Collection<SearchResult> getSelectedResults() {
        ArrayList<SearchResult> selectedResults = new ArrayList<SearchResult>();
        int[] selectedRows = resultsTable.getSelectedRows();
        TableSorter sorter = (TableSorter) resultsTable.getModel();
        for (int selectedRow : selectedRows) {
            int modelRow = sorter.modelIndex(selectedRow);
            selectedResults.add(searchResults.get(modelRow));
        }
        searchResults.removeAll(selectedResults);
        updateResultsTable();
        return selectedResults;
    }

    private void createActions() {
        closeAction = new AbstractAction(Resources.get().getString(
                "searchResultDialog.close")) {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }};
    }

    private JPanel createContentPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(createResultPanel());

        return panel;
    }

    private JPanel createResultPanel() {
        resultsTable = new JTable();
        updateResultsTable();
        resultsTable.getSelectionModel().addListSelectionListener(this);
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addAction.actionPerformed(null);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(575, 150));
        scrollPane.setAlignmentX(RIGHT_ALIGNMENT);

        // escape closes dialog
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();
        Object closeKey = closeAction.getValue(Action.NAME);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), closeKey);
        actionMap.put(closeKey, closeAction);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setAlignmentX(RIGHT_ALIGNMENT);
        buttonPanel.add(new JButton(addAction));
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(new JButton(closeAction));

        detailsTextPane = new JTextPane();
        detailsTextPane.setEditable(false);
        detailsTextPane.setEnabled(false);
        JScrollPane detailsScroller = new JScrollPane(detailsTextPane);
        detailsScroller.setPreferredSize(new Dimension(575, 150));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
                Resources.get().getString("searchResultDialog.details")));
        detailsPanel.setLayout(new BorderLayout());
        detailsPanel.setAlignmentX(RIGHT_ALIGNMENT);
        detailsPanel.add(detailsScroller, BorderLayout.CENTER);
        updateDetails();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(5));
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(detailsPanel);

        return panel;
    }

    private void updateResultsTable() {
        TableModel resultsTableModel = resultsTable.getModel();
        TableSorter sorter;
        if (resultsTableModel instanceof TableSorter) {
            sorter = (TableSorter) resultsTableModel;
        } else {
            sorter = new TableSorter();
            resultsTable.setModel(sorter);
            sorter.setTableHeader(resultsTable.getTableHeader());
            sorter.setSortingStatus(3, TableSorter.DESCENDING);
            sorter.setSortingStatus(2, TableSorter.ASCENDING);
        }

        SearchResultTableModel searchResultModel =
                new SearchResultTableModel(searchResults);
        sorter.setTableModel(searchResultModel);

        TableColumnModel colModel = resultsTable.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(100);
        colModel.getColumn(1).setMaxWidth(75);
        colModel.getColumn(2).setPreferredWidth(400);
    }

    private void updateDetails() {
        int selectedRow = resultsTable.getSelectedRow();
        Lyric lyric = null;

        if (selectedRow != -1) {
            String bundle = (String) resultsTable.getValueAt(selectedRow, 0);
            int number = ((Integer) resultsTable.getValueAt(selectedRow, 1)).intValue();
            LyricRef lyricRef = new LyricRef(bundle, number);
            Catalog catalog = CatalogManager.getCatalog();
            lyric = catalog.getLyric(lyricRef);
        }

        DefaultStyledDocument document = new DefaultStyledDocument();
        Style baseStyle = document.addStyle(null, null);
        Style labelStyle = document.addStyle(null, baseStyle);
        Style valueStyle = document.addStyle(null, baseStyle);
        StyleConstants.setFontFamily(baseStyle, "Sans Serif");
        StyleConstants.setBold(labelStyle, true);
        StyleConstants.setFontSize(labelStyle, 12);
        StyleConstants.setFontSize(valueStyle, 12);

        try {
            if (lyric == null) {
                document.insertString(document.getLength(), Resources.get().getString(
                        "searchResultDialog.emptySelection"), valueStyle);
            } else {
                document.insertString(document.getLength(), Resources.get().getString(
                        "searchResultDialog.title"), labelStyle);
                document.insertString(document.getLength(), "\n", labelStyle);
                document.insertString(document.getLength(), lyric.getTitle(), valueStyle);
                document.insertString(document.getLength(), "\n\n", null);

                document.insertString(document.getLength(), Resources.get().getString(
                        "searchResultDialog.text"), labelStyle);
                document.insertString(document.getLength(), "\n", labelStyle);
                document.insertString(document.getLength(), lyric.getText(), valueStyle);
                document.insertString(document.getLength(), "\n\n", null);

                String originalTitle = lyric.getOriginalTitle();
                if (originalTitle != null) {
                    document.insertString(document.getLength(), Resources.get().getString(
                            "searchResultDialog.originalTitle"), labelStyle);
                    document.insertString(document.getLength(), "\n", labelStyle);
                    document.insertString(document.getLength(), originalTitle, valueStyle);
                    document.insertString(document.getLength(), "\n\n", null);
                }

                String copyrights = lyric.getCopyrights();
                if (copyrights != null) {
                    document.insertString(document.getLength(), Resources.get().getString(
                            "searchResultDialog.copyrights"), labelStyle);
                    document.insertString(document.getLength(), "\n", labelStyle);
                    document.insertString(document.getLength(), copyrights, valueStyle);
                    document.insertString(document.getLength(), "\n\n", null);
                }

                Collection<BibleRef> bibleRefs = lyric.getBibleRefs();
                if (!bibleRefs.isEmpty()) {
                    document.insertString(document.getLength(), Resources.get().getString(
                            "searchResultDialog.bibleReferences"), labelStyle);
                    document.insertString(document.getLength(), "\n", labelStyle);
                    for (BibleRef bibleRef : bibleRefs) {
                        document.insertString(document.getLength(), bibleRef.toString() + "\n", valueStyle);
                    }
                }
            }

        } catch (BadLocationException e) {
            assert false;
        }

        detailsTextPane.setDocument(document);
        detailsTextPane.setEnabled(lyric != null);
    }
}

