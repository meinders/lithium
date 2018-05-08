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
import java.awt.geom.*;
import javax.swing.*;

public class GraphicsEnvironmentView extends JComponent {
    private final Color selectionForeground;

    private final Color selectionBackground;

    private GraphicsDevice selectedDevice = null;

    private boolean emptySelectionAllowed = true;

    public GraphicsEnvironmentView() {
        setPreferredSize(new Dimension(350, 120));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    final GraphicsDevice device = getDeviceAt(e.getX(), e.getY());
                    if (device != null || isEmptySelectionAllowed()) {
                        setSelectedDevice(device);
                    }
                }
            }
        });

        final JList list = new JList();
        selectionForeground = list.getSelectionForeground();
        selectionBackground = list.getSelectionBackground();
    }

    public boolean isEmptySelectionAllowed() {
        return emptySelectionAllowed;
    }

    public void setEmptySelectionAllowed(boolean emptySelectionAllowed) {
        this.emptySelectionAllowed = emptySelectionAllowed;
        if (!emptySelectionAllowed && selectedDevice == null) {
            selectedDevice = getDefaultDevice();
        }
    }

    private GraphicsDevice getDefaultDevice() {
        final GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        return localGraphicsEnvironment.getDefaultScreenDevice();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground().darker().darker().darker());
        g.fillRect(0, 0, getWidth(), getHeight());

        final GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        final GraphicsDevice[] screenDevices = localGraphicsEnvironment.getScreenDevices();

        Graphics2D g2 = (Graphics2D) g.create();

        final AffineTransform transform = g2.getTransform();
        transform.concatenate(createModelToViewTransform());
        g2.setTransform(transform);

        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (int i = 0; i < screenDevices.length; i++) {
            final GraphicsDevice device = screenDevices[i];
            final GraphicsConfiguration configuration = device.getDefaultConfiguration();
            final Rectangle bounds = configuration.getBounds();

            if (selectedDevice == device) {
                g2.setColor(selectionBackground);
                g2.fill(bounds);
            }

            Rectangle smaller = (Rectangle) bounds.clone();
            double scale = getScale();
            smaller.grow((int) (-4 / scale), (int) (-4 / scale));

            g2.setColor(selectedDevice == device ? selectionBackground.darker() : Color.GRAY);
            g2.fill(smaller);
            g2.setColor(Color.WHITE);
            g2.draw(smaller);

            g2.setFont(getFont().deriveFont(Font.BOLD, (float) bounds.getHeight() * 0.75f));
            final FontMetrics fontMetrics = g2.getFontMetrics();
            g2.setColor(selectedDevice == device ? selectionForeground.darker().darker()
                    : getForeground().darker().darker());
            final String id = String.valueOf(i + 1);
            g2.drawString(id, (int) (bounds.getCenterX() - fontMetrics.getStringBounds(id, g2)
                    .getCenterX()), (int) (bounds.getCenterY() - fontMetrics.getStringBounds(
                    id, g2).getCenterY()));
        }
    }

    private AffineTransform createModelToViewTransform() {
        final Rectangle environmentBounds = getEnvironmentBounds();
        final Insets insets = getInsets();
        final double scale = getScale();

        int width = getWidth() - (insets.left + insets.right + 1);
        int height = getHeight() - (insets.top + insets.bottom + 1);

        AffineTransform transform = new AffineTransform();
        transform.translate(insets.left + width / 2, insets.top + height / 2);
        transform.scale(scale, scale);
        transform.translate(-environmentBounds.getCenterX(), -environmentBounds.getCenterY());
        return transform;
    }

    private AffineTransform createViewToModelTransform() {
        final AffineTransform transform = createModelToViewTransform();
        try {
            transform.invert();
        } catch (NoninvertibleTransformException e) {
            final AssertionError assertionError = new AssertionError(
                    "Model to view transform must be invertible");
            assertionError.initCause(e);
            throw assertionError;
        }
        return transform;
    }

    private double getScale() {
        final Rectangle environmentBounds = getEnvironmentBounds();

        final Insets insets = getInsets();
        int width = getWidth() - (insets.left + insets.right + 1);
        int height = getHeight() - (insets.top + insets.bottom + 1);

        final double aspect = (double) width / (double) height;
        final double environmentAspect = environmentBounds.getWidth()
                / environmentBounds.getHeight();
        double scale = (aspect < environmentAspect) ? (width / environmentBounds.getWidth())
                : (height / environmentBounds.getHeight());
        return scale;
    }

    private Rectangle getEnvironmentBounds() {
        final GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        final GraphicsDevice[] screenDevices = localGraphicsEnvironment.getScreenDevices();

        Rectangle result = new Rectangle();
        for (int i = 0; i < screenDevices.length; i++) {
            final GraphicsDevice device = screenDevices[i];
            final GraphicsConfiguration configuration = device.getDefaultConfiguration();
            result = result.union(configuration.getBounds());
        }

        return result;
    }

    public void setSelectedDevice(GraphicsDevice selectedDevice) {
        if (selectedDevice == null && !isEmptySelectionAllowed()) {
            throw new NullPointerException("selectedDevice");
        }
        if (this.selectedDevice != selectedDevice) {
            this.selectedDevice = selectedDevice;
            repaint();
            fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "selection"));
        }
    }

    public GraphicsDevice getSelectedDevice() {
        return selectedDevice;
    }

    public GraphicsDevice getDeviceAt(int x, int y) {
        final AffineTransform transform = createViewToModelTransform();
        final Point point = new Point(x, y);
        transform.transform(point, point);

        final GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        final GraphicsDevice[] screenDevices = localGraphicsEnvironment.getScreenDevices();

        for (int i = 0; i < screenDevices.length; i++) {
            final GraphicsDevice device = screenDevices[i];
            final GraphicsConfiguration configuration = device.getDefaultConfiguration();
            if (configuration.getBounds().contains(point)) {
                return device;
            }
        }
        return null;
    }

    public void addActionListener(ActionListener listener) {
        listenerList.add(ActionListener.class, listener);
    }

    protected void fireActionEvent(ActionEvent e) {
        for (ActionListener listener : listenerList.getListeners(ActionListener.class)) {
            listener.actionPerformed(e);
        }
    }
}
