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
import javax.swing.*;

import com.github.meinders.common.*;
import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.catalog.*;

/**
 * Editor component for bible references.
 *
 * @author Gerrit Meinders
 */
public class BibleRefEditor extends JPanel {

    private JComboBox bookCombo;

    private JTextField startChapterField;

    private JTextField endChapterField;

    private JTextField startVerseField;

    private JTextField endVerseField;

    /**
     * Constructs a new bible reference editor.
     */
    public BibleRefEditor() {
        super();

        bookCombo = new JComboBox(BibleRef.getBooks());
        startChapterField = new JTextField(5);
        endChapterField = new JTextField(5);
        startVerseField = new JTextField(5);
        endVerseField = new JTextField(5);

        ResourceUtilities resources = Resources.get("quote");

        setLayout(new SpringLayout());

        JPanel startComponent = createFlow(startChapterField, new JLabel(
                resources.getString("verseSeparator")), startVerseField);
        JPanel endComponent = createFlow(endChapterField, new JLabel(resources
                .getString("verseSeparator")), endVerseField);

        addField(resources.getLabel("book"), bookCombo);
        addField(resources.getLabel("start"), startComponent);
        addField(resources.getLabel("end"), endComponent);

        SpringUtilities.makeCompactGrid(this, 3, 2, 0, 0, 5, 5);
    }

    private void addField(String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(component);
        add(label);
        add(component);
    }

    private JPanel createFlow(JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        for (JComponent component : components) {
            panel.add(component);
            panel.add(Box.createRigidArea(new Dimension(5, 5)));
        }
        panel.remove(panel.getComponentCount() - 1);
        return panel;
    }

    /**
     * Constructs a new bible reference editor with the specified initial value.
     *
     * @param initialValue the initial value of the editor
     */
    public BibleRefEditor(BibleRef initialValue) {
        this();
        setValue(initialValue);
    }

    /**
     * Returns the value of the editor, or {@code null} if the editor's value is
     * currently invalid.
     *
     * @return the value of the editor, or {@code null}
     */
    public BibleRef getValue() {
        String book = (String) bookCombo.getSelectedItem();

        BibleRef result;
        try {
            Integer startChapter = parseIntegerField(startChapterField,
                    Resources.get().getString("bibleRef.startChapter"));
            Integer endChapter = parseIntegerField(endChapterField, Resources
                    .get().getString("bibleRef.endChapter"));
            Integer startVerse = parseIntegerField(startVerseField, Resources
                    .get().getString("bibleRef.startVerse"));
            Integer endVerse = parseIntegerField(endVerseField, Resources.get()
                    .getString("bibleRef.endVerse"));

            if (startChapter == null) {
                startChapter = endChapter;
                endChapter = null;
                startVerse = endVerse;
                endVerse = null;
            }

            result = new BibleRef(book, startChapter, endChapter, startVerse,
                    endVerse);

        } catch (NumberFormatException ex) {
            result = null;
        }

        return result;
    }

    /**
     * Sets the value of the editor.
     *
     * @param value the value to be set
     */
    public void setValue(BibleRef value) {
        bookCombo.setSelectedItem(value.getBookName());
        setIntegerField(startChapterField, value.getStartChapter());
    }

    private Integer parseIntegerField(JTextField field, String fieldName) {
        try {
            String fieldText = field.getText();
            boolean empty = fieldText.length() == 0;
            return empty ? null : Integer.valueOf(fieldText);
        } catch (NumberFormatException e) {
            // show message
            String message = Resources.get().getString(
                    "findLyricDialog.invalidField", fieldName);
            JOptionPane.showInternalMessageDialog(this, message, getTitle(),
                    JOptionPane.WARNING_MESSAGE);
            startChapterField.requestFocus();
            throw e;
        }
    }

    private String getTitle() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof Window)) {
            parent = parent.getParent();
        }

        if (parent instanceof Frame) {
            return ((Frame) parent).getTitle();
        } else if (parent instanceof Dialog) {
            return ((Dialog) parent).getTitle();
        } else {
            return null;
        }
    }

    private void setIntegerField(JTextField field, Integer value) {
        field.setText(value == null ? "" : value.toString());
    }
}
