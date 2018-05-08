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

package lithium.animation.legacy;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;

import lithium.animation.legacy.scrolling.*;

import static javax.swing.ScrollPaneConstants.*;
import static javax.swing.WindowConstants.*;


public class ScrollingDemo implements Runnable
{
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new ScrollingDemo());
	}

	private final SynchronizationModel synchModel = new SynchronizationModel();

	protected String getTitle()
	{
		return "opwviewer.experimental: Advanced Text Range Model";
	}

	private String getProjectDescription()
	{
		try
		{
			URL resource = getClass().getResource("projectDescription");
			if (resource == null)
			{
				return "Project description unavailable.";
			}
			InputStream in = resource.openStream();
			BufferedReader bIn = new BufferedReader(new InputStreamReader(in));
			StringBuffer lines = new StringBuffer();

			while (true)
			{
				String line = bIn.readLine();
				if (line == null)
				{
					break;
				}

				lines.append(line);
				lines.append('\n');
			}

			return lines.toString();
		}
		catch (IOException e)
		{
			return "Project description unavailable.\n\n" + e.getMessage();
		}
	}

	private JFrame createFrame()
	{
		JFrame frame = new JFrame(getTitle());

		JTextPane text = new JTextPane();
		text.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		text.setBackground(frame.getContentPane().getBackground());
		text.setText(getProjectDescription());
		text.setEditable(false);
		text.setPreferredSize(new Dimension(800, 100));

		frame.setLayout(new BorderLayout());
		frame.add(text, BorderLayout.NORTH);
		return frame;
	}

	public void run()
	{
		final SmoothScroller scroller = new SmoothScroller(false);
		final AutoScroller autoScroller = new AutoScroller(scroller);

		JTextPane view1 = createTextPane(32);
		JTextPane view2 = createTextPane(24);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		ConversionStrategy strategy1 = createStrategy(view1);
		ConversionStrategy strategy2 = createStrategy(view2);

		panel.add(getScrolledTextPane(view1, scroller, strategy1));
		panel.add(getScrolledTextPane(view2, scroller, strategy2));

		ConversionStrategyChart chart1 = new ConversionStrategyChart(strategy1,
		        view1);
		ConversionStrategyChart chart2 = new ConversionStrategyChart(strategy2,
		        view2);

		JPanel chartPanel = new JPanel();
		chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.LINE_AXIS));
		chartPanel.add(new ChartView(chart1, chart2));

		JFrame frame = createFrame();
		frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				autoScroller.dispose();
				scroller.dispose();
			}
		});
		frame.add(panel, BorderLayout.CENTER);
		frame.add(chartPanel, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);

		scroller.start();
		autoScroller.setEnabled(true);
	}

	private ConversionStrategy createStrategy(JTextPane textPane)
	{
		ConversionStrategy strategy;
		// strategy = new LineConversionStrategy(textPane);
		strategy = new SynchronizationModelStrategy(synchModel, textPane);
		strategy.setTopMarginFixed(true);
		return strategy;
	}

	private JTextPane createTextPane(int fontsize)
	{
		JTextPane textPane = new JTextPane();
		textPane.setCaretPosition(0);
		textPane.setEditable(false);

		StyledDocument document = textPane.getStyledDocument();

		// demo text
		Style baseStyle = document.addStyle("base", null);
		StyleConstants.setFontSize(baseStyle, fontsize);
		try
		{
			for (int i = 0; i < 20; i++)
			{
				String text = i + ": Lorem ipsum dolor sit amet, "
				        + "consectetuer adipiscing elit\n";
				document.insertString(document.getLength(), text, baseStyle);
				document.insertString(document.getLength(), i
				        + ": Lorem ipsum\n", baseStyle);
			}

		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}

		// top/bottom margins
		int marginTop = 3 * fontsize;
		int marginBottom = 3 * fontsize;
		Style marginTopStyle = document.addStyle("marginTop", null);
		StyleConstants.setSpaceAbove(marginTopStyle, marginTop);
		Style marginBottomStyle = document.addStyle("marginBottom", null);
		StyleConstants.setSpaceBelow(marginBottomStyle, marginBottom);
		document.setParagraphAttributes(0, 0, document.getStyle("marginTop"),
		        false);
		document.setParagraphAttributes(document.getLength(), 0,
		        document.getStyle("marginBottom"), false);

		return textPane;
	}

	/**
	 * Adds a scroll pane that uses the given scroller.
	 */
	private JComponent getScrolledTextPane(final JTextPane textPane,
	        Scroller scroller, ConversionStrategy strategy)
	{
		BoundedRangeModel model = new AdvancedTextRangeModel(scroller, strategy);
		JScrollPane scrollPane = getScrolledComponent(textPane);
		scrollPane.getVerticalScrollBar().setModel(model);

		JLayeredPane panel = new JLayeredPane();
		panel.setPreferredSize(scrollPane.getPreferredSize());
		scrollPane.setSize(scrollPane.getPreferredSize());
		panel.add(scrollPane);
		JPanel marginPanel = new JPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				int topMargin;
				try
				{
					topMargin = textPane.modelToView(0).y;
				}
				catch (BadLocationException e)
				{
					throw new AssertionError(e);
				}

				g.setColor(Color.BLUE);
				g.drawLine(0, topMargin, getWidth(), topMargin);
			}
		};
		marginPanel.setOpaque(false);
		marginPanel.setSize(scrollPane.getPreferredSize());
		panel.add(marginPanel, JLayeredPane.PALETTE_LAYER);

		return panel;
	}

	private JScrollPane getScrolledComponent(JComponent component)
	{
		JScrollPane scrollPane = new JScrollPane(component,
		        VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(400, 300));
		return scrollPane;
	}

	private class ConversionStrategyChart implements Chart
	{
		private final ConversionStrategy strategy;

		private final JTextPane view;

		private final List<Point> points;

		public ConversionStrategyChart(ConversionStrategy strategy,
		        JTextPane view)
		{
			this.strategy = strategy;
			this.view = view;
			points = new ArrayList<Point>();
			view.addComponentListener(new ComponentListener()
			{
				public void componentHidden(ComponentEvent e)
				{
					// ignore
				}

				public void componentMoved(ComponentEvent e)
				{
					// ignore
				}

				public void componentResized(ComponentEvent e)
				{
					updatePoints();
				}

				public void componentShown(ComponentEvent e)
				{
					updatePoints();
				}
			});
		}

		private void updatePoints()
		{
			points.clear();

			if (false)
			{
				// view to model
				for (int i = 0; i < view.getHeight() / 10; i++)
				{
					int model = (int) (1000 * strategy.viewToModel(i));
					points.add(new Point(i, model));
				}
			}
			else
			{
				// model to view
				final int subdivisions = 2;
				for (int i = 0; i < synchModel.getPointCount() * subdivisions
				        / 10; i++)
				{
					int viewPoint = strategy.modelToView(i
					        / (float) subdivisions);
					System.out.println(i + " -> " + viewPoint);
					points.add(new Point(i, viewPoint));
				}
			}
		}

		public List<Point> getPoints()
		{
			return points;
		}
	}

	private static interface Chart
	{
		public List<Point> getPoints();
	}

	private static class ChartView extends JPanel
	{
		private Chart[] charts;

		public ChartView(Chart... charts)
		{
			this.charts = charts;
			setPreferredSize(new Dimension(600, 200));
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			Color[] colors = { Color.RED, Color.BLUE };
			int colorIndex = 0;

			for (Chart chart : charts)
			{
				g.setColor(colors[colorIndex++]);

				List<Point> points = chart.getPoints();

				int minX = Integer.MAX_VALUE;
				int minY = Integer.MAX_VALUE;
				int maxX = Integer.MIN_VALUE;
				int maxY = Integer.MIN_VALUE;
				for (Point point : points)
				{
					if (point.x < minX)
					{
						minX = point.x;
					}
					if (point.x > maxX)
					{
						maxX = point.x;
					}
					if (point.y < minY)
					{
						minY = point.y;
					}
					if (point.y > maxY)
					{
						maxY = point.y;
					}
				}

				Point lastPoint = null;

				int width = getWidth();
				int height = getHeight();
				int sizeX = (maxX - minX);
				int sizeY = (maxY - minY);

				for (Point point : points)
				{
					if (lastPoint != null)
					{
						int x1 = (int) (width * (lastPoint.getX() - minX) / sizeX);
						int x2 = (int) (width * (point.getX() - minX) / sizeX);
						int y1 = (int) (height * (lastPoint.getY() - minY) / sizeY);
						int y2 = (int) (height * (point.getY() - minY) / sizeY);
						g.drawLine(x1, y1, x2, y2);
					}
					lastPoint = point;
				}
			}
		}
	}
}
