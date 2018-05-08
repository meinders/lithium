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

package lithium.audio;

import java.awt.*;
import javax.swing.*;

/**
 * Typical volume bar component, showing the amplitude for each audio channel.
 *
 * @author Gerrit Meinders
 */
public class JVolumeBar extends JComponent {
	public static void main(String[] args) {
		final JVolumeBar volumeBar = new JVolumeBar();
		volumeBar.setBackground(Color.WHITE);
		volumeBar.setOpaque(true);
		volumeBar.setChannels(2);
		volumeBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 20));

		JFrame frame = new JFrame();
		frame.add(volumeBar);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setBackground(Color.YELLOW);
		frame.setSize(200, 500);
		frame.setVisible(true);

		Thread t = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					volumeBar.setAmplitude(0, 0.5 * Math.random());
					volumeBar.setAmplitude(1, 0.5 * Math.random());
					volumeBar.setGain(0, 1.0 + Math.random());
					volumeBar.setGain(1, 1.0 + Math.random());
					try {
						Thread.sleep(100L);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

	private int channels;

	private double[] amplitudes;

	private double[] peaks;

	private double[] gains;

	public enum Orientation {
		HORIZONTAL, VERTICAL
	}

	private Orientation orientation;

	public JVolumeBar() {
		setOpaque(false);
		setChannels(2);
		setOrientation(Orientation.VERTICAL);
	}

	public int getChannels() {
		return channels;
	}

	public void setChannels(int channels) {
		this.channels = channels;

		amplitudes = new double[channels];
		peaks = new double[channels];
		gains = new double[channels];
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	public void setAmplitude(int channel, double amplitude) {
		amplitudes[channel] = Math.max(amplitudes[channel] * 0.9, amplitude);
		peaks[channel] = Math.max(peaks[channel] * 0.99, amplitude);

		repaint();
	}

	public void setGain(int channel, double gain) {
		gains[channel] = gain;
	}

	public void clear() {
		for (int i = 0; i < amplitudes.length; i++) {
			amplitudes[i] = 0.0;
			peaks[i] = 0.0;
			gains[i] = 1.0;
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Insets insets = getInsets();

		int margin = 2;
		int barSize = 3;
		int width = getWidth() - insets.left - insets.right;
		int height = getHeight() - insets.top - insets.bottom;

		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(insets.left, insets.top, width, height);
		}

		boolean horizontal = orientation == Orientation.HORIZONTAL;
		int totalBarSize = horizontal ? width : height;
		int bars = (totalBarSize - margin) / (barSize + margin);
		int barOffset;
		if (horizontal) {
			barOffset = (totalBarSize - bars * (barSize + margin) + margin) / 2;
		} else {
			barOffset = (totalBarSize - (bars - 2) * (barSize + margin) - margin) / 2;
		}

		Color inactive = Color.LIGHT_GRAY;

		Color red = new Color(0xcc4444);
		Color yellow = new Color(0xcccc44);
		Color green = new Color(0x44cc44);

		Color redPeak = red.darker();
		Color yellowPeak = yellow.darker();
		Color greenPeak = green.darker();

		Color redGain = red;// new Color(0xee8888);
		Color yellowGain = yellow;// new Color(0xeeee88);
		Color greenGain = green;// new Color(0x88ee88);

		for (int channel = 0; channel < channels; channel++) {
			g.setColor(green);
			double amplitude = amplitudes[channel];
			double peak = peaks[channel];
			double gain = gains[channel];
			double amplitudeWithGain = amplitude * gain;

			for (int bar = 0; bar < bars; bar++) {
				double startAmplitude = (double) bar / (double) bars;
				double halfAmplitude = (double) (bar + 1) / (double) bars;
				double endAmplitude = (double) (bar + 1) / (double) bars;

				if (halfAmplitude > amplitude) {
					if (halfAmplitude > amplitudeWithGain) {
						g.setColor(inactive);
					} else {
						if (halfAmplitude > 0.75) {
							g.setColor(redGain);
						} else if (halfAmplitude > 0.5) {
							g.setColor(yellowGain);
						} else {
							g.setColor(greenGain);
						}
					}
				} else if (halfAmplitude > 0.75) {
					g.setColor(red);
				} else if (halfAmplitude > 0.5) {
					g.setColor(yellow);
				}

				if (startAmplitude < peak && endAmplitude >= peak) {
					Color previousColor = g.getColor();
					if (halfAmplitude > 0.75) {
						g.setColor(redPeak);
					} else if (halfAmplitude > 0.5) {
						g.setColor(yellowPeak);
					} else {
						g.setColor(greenPeak);
					}

					if (horizontal) {
						g.fillRect(barOffset + bar * (barSize + margin)
						        + insets.left, (height * channel) / channels
						        + margin / 2 + insets.top, barSize, height
						        / channels - margin);
					} else {
						g.fillRect((width * channel) / channels + margin / 2
						        + insets.left, height
						        - (barOffset + bar * (barSize + margin))
						        + insets.top, width / channels - margin,
						        barSize);
					}

					g.setColor(previousColor);

				} else {
					if (halfAmplitude > peak
					        && halfAmplitude <= amplitudeWithGain) {
						Color color = g.getColor();
						g.setColor(inactive);
						if (horizontal) {
							g.fillRect(barOffset + bar * (barSize + margin)
							        + insets.left, (height * channel)
							        / channels + margin / 2 + insets.top,
							        barSize, height / channels - margin);
						} else {
							g.fillRect((width * channel) / channels + margin
							        / 2 + insets.left, height
							        - (barOffset + bar * (barSize + margin))
							        + insets.top, width / channels - margin,
							        barSize);
						}
						g.setColor(color);
						if (horizontal) {
							int h = height / channels - margin;
							g.fillRect(barOffset + bar * (barSize + margin)
							        + insets.left, (height * channel)
							        / channels + margin / 2 + insets.top + h
							        / 3, barSize, h - (h / 3) * 2);
						} else {
							int w = width / channels - margin;
							g.fillRect((width * channel) / channels + margin
							        / 2 + insets.left + w / 3, height
							        - (barOffset + bar * (barSize + margin))
							        + insets.top, w - (w / 3) * 2, barSize);
						}
					} else {
						if (horizontal) {
							g.fillRect(barOffset + bar * (barSize + margin)
							        + insets.left, (height * channel)
							        / channels + margin / 2 + insets.top,
							        barSize, height / channels - margin);
						} else {
							g.fillRect((width * channel) / channels + margin
							        / 2 + insets.left, height
							        - (barOffset + bar * (barSize + margin))
							        + insets.top, width / channels - margin,
							        barSize);
						}
					}
				}
			}
		}
	}
}
