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

package lithium.display;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import lithium.*;
import lithium.display.java2d.*;
import lithium.editor.*;

/**
 * An internal frame containing a lyric view.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class InternalFrameDisplay extends JInternalFrameEx {
    private LyricView lyricView;

    public InternalFrameDisplay() {
        super((JInternalFrameEx) null, Resources.get().getString(
                "internalFullScreenDisplay.title"));
        init();
    }

    public void setLyricView(LyricView lyricView) {
        this.lyricView = lyricView;
        getContentPane().removeAll();
        getContentPane().add(lyricView, BorderLayout.CENTER);
        revalidate();
        setSize(getPreferredSize());
    }

    public LyricView getLyricView() {
        return lyricView;
    }

    private void init() {
        setResizable(true);
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);

        setContentPane(createContentPane());

        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent e) {
                if (lyricView != null) {
                    lyricView.dispose();
                }
            }});

        pack();
        setVisible(true);
    }

    private JPanel createContentPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        return panel;
    }
}

