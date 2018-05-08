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

package lithium.powerpoint;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A frame that copies the contents of one device to display it on another.
 * This implementation is Swing-based.
 *
 * @version 0.9x (2005.10.17)
 * @author Gerrit Meinders
 */
public class ScreenMirror extends JFrame {
    private GraphicsDevice source;
    private Robot robot;
    private boolean performanceData = false;

    public ScreenMirror(GraphicsDevice target, GraphicsDevice source) {
        super(target.getDefaultConfiguration());
        this.source = source;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        final Timer timer = new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getContentPane().repaint();
            }});
        timer.setRepeats(true);

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                timer.start();
            }

            public void windowClosed(WindowEvent e) {
                timer.stop();
            }});

        setUndecorated(true);
        setContentPane(new MirrorPanel());
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private class MirrorPanel extends JPanel {
        private static final int SECTION_COUNT = 16;
        private final int[] START = {
            0, 8, 4, 12, 2, 6, 10, 14,
            1, 9, 5, 13, 3, 7, 11, 15
        };
        private int section = 0;

        // performance data
        private int frames = 0;
        private long startTime = 0;
        private long paintTime = 0;

        public MirrorPanel() {
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    dispose();
                }});
        }

        public void paintComponent(Graphics g) {
            if (performanceData) {
                // subtract non-paint time
                paintTime -= System.nanoTime();
            }

            GraphicsConfiguration graphConfig = source.getDefaultConfiguration();
            Rectangle screenRect = graphConfig.getBounds();
            int height = getHeight();
            Rectangle targetRect = new Rectangle(0, 0, getWidth(), 1);
            Rectangle sourceRect = new Rectangle(screenRect);

            for (int i=START[section]; i<height; i+=SECTION_COUNT) {
                targetRect.y = i;
                sourceRect.y = i * screenRect.height / height;
                int y2 = (i + 1) * screenRect.height / height;
                sourceRect.height = y2 - sourceRect.y + 1;
                Image image = robot.createScreenCapture(sourceRect);
                g.drawImage(image, targetRect.x, targetRect.y,
                        targetRect.width, targetRect.height, null);
            }
            section++;
            section %= SECTION_COUNT;

            if (performanceData) {
                // add paint time
                paintTime += System.nanoTime();

                long currentTime = System.nanoTime();
                long totalTime = currentTime - startTime;
                float fps = frames * (1000000000 / (float) totalTime);
                float load = (float) paintTime / (float) totalTime;
                if (startTime > 0) {
                    /*
                    g.setColor(Color.WHITE);
                    g.fillRect(50, 35, 60, 20);
                    g.setColor(Color.BLACK);
                    g.drawString(""+fps, 50, 50);
                    */
                } else {
                    startTime = currentTime;
                }
                frames++;
                if (frames > 250) {
                    System.out.printf("Average FPS (visual): %s (actual: %s)\n"+
                            "\tAverage load: %s%%\n",
                            fps, fps / SECTION_COUNT, load * 100);
                    startTime = currentTime;
                    paintTime = 0;
                    frames = 1;
                }
            }
        }
    }
}

