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

package lithium.imagebrowser;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import lithium.imagebrowser.ImageBrowser.*;

class ImageBrowserDialog extends JDialog {
    private Option selectedOption = Option.CANCEL_OPTION;

    private ImageBrowser browser;

    private String nullLabel = "geen afbeelding";

    private ThumbnailListModel listModel;

    private File folder;

    private ThumbnailLoader thumbnailLoader;

    private ArrayList<File> imageFiles = null;

    public ImageBrowserDialog(ImageBrowser browser, Frame owner, String title,
            boolean modal) {
        super(owner, title, modal);
        this.browser = browser;
        init();
    }

    public ImageBrowserDialog(ImageBrowser browser, Dialog owner, String title,
            boolean modal) {
        super(owner, title, modal);
        this.browser = browser;
        init();
    }

    private void init() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createThumbnailList(), BorderLayout.CENTER);
        setContentPane(panel);
        setFolder(browser.getInitialFolder());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();

        // center dialog on screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2,
                (screen.height - getHeight()) / 2);
    }

    private JComponent createThumbnailList() {
        int thumbnailWidth = 160;
        int thumbnailHeight = 120;
        thumbnailLoader = new ThumbnailLoader(thumbnailWidth, thumbnailHeight);

        listModel = new ThumbnailListModel(thumbnailLoader);

        final JIconList<URL> list = new JIconList<URL>(listModel);
        // IconCellRenderer renderer = new IconCellRenderer();
        // list.setCellRenderer(renderer);
        // list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        // list.setVisibleRowCount(0);

        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    browser.setSelectedFile(imageFiles.get(list
                            .getSelectedIndex()));
                }
            }
        });

        JScrollPane listScroller = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        Dimension preferredSize = new Dimension((thumbnailWidth + 10) * 4, 0);
        preferredSize.height = (preferredSize.width * 3) / 4;
        listScroller.getViewport().setPreferredSize(preferredSize);

        return listScroller;
    }

    public void setFolder(File folder) {
        if (folder != this.folder) {
            this.folder = folder;
            updateListModel();
        }
    }

    private void updateListModel() {
        listModel.clear();

        // create local list of files
        imageFiles = new ArrayList<File>();
        if (browser.isNullImageVisible()) {
            imageFiles.add((File) null);
        }
        for (File file : folder.listFiles(browser.getImageFilter())) {
            imageFiles.add(file);
        }

        // insert files into list model
        for (File file : imageFiles) {
            try {
                listModel.add(file == null ? null : file.toURI().toURL(),
                        file == null ? nullLabel : file.getName());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public Option getSelectedOption() {
        return selectedOption;
    }
}
