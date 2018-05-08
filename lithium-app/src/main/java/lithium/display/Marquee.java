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
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import com.github.meinders.common.animation.*;
import com.github.meinders.common.animation.Timer;
import lithium.display.opengl.*;

/**
 * A plugin that displays text in a marquee style, scrolling from right to left.
 *
 * @author Gerrit Meinders
 */
public class Marquee extends GUIPlugin
{
	private static final double DEFAULT_RATE = 0.05;

	private final Timer timer;

	private Linear1D position;

	private Variable1D visibility;

	private List<String> texts;

	private Iterator<String> textIterator;

	private String text;

	private Rectangle2D textBounds;

	private Font font;

	private final FontRenderContext fontRenderContext;

	private Color foreground;

	private Color shadow;

	public Marquee(String text)
	{
		super();

		if (text == null)
		{
			throw new NullPointerException("text");
		}

		texts = Arrays.asList(text.split("\n"));
		if (texts.isEmpty())
		{
			throw new IllegalArgumentException("text");
		}
		textIterator = texts.iterator();

		font = new Font(Font.SANS_SERIF, Font.PLAIN, 40);
		foreground = Color.WHITE;
		shadow = Color.BLACK;

		timer = new Timer();
		position = new Linear1D(0.0, 0.0, DEFAULT_RATE);
		visibility = new Constant1D(1.0);
		fontRenderContext = new FontRenderContext(null, true, true);

		nextText();
	}

	/**
	 * Sets the rate of scrolling per second, relative to the renderer's width.
	 *
	 * @param rate the scroll rate
	 */
	public void setRate(double rate)
	{
		position = Linear1D.branch(position, timer.currentTime(), rate);
	}

	/**
	 * Returns the rate of scrolling per second, relative to the renderer's
	 * width.
	 *
	 * @return the scroll rate
	 */
	public double getRate()
	{
		return position.getRate();
	}

	/**
	 * Sets the color of the marquee's text.
	 *
	 * @param foreground the text color
	 */
	public void setForeground(Color foreground)
	{
		this.foreground = foreground;
	}

	public Font getFont()
	{
		return font;
	}

	/**
	 * Returns the color of the marquee's text. By default, the text color is
	 * white.
	 *
	 * @return the text color
	 */
	public Color getForeground()
	{
		return foreground;
	}

	/**
	 * Sets the color of the text shadow. By default, the shadow color is black.
	 *
	 * @param shadow the shadow color
	 */
	public void setShadow(Color shadow)
	{
		this.shadow = shadow;
	}

	/**
	 * Returns the color of the text shadow.
	 *
	 * @return the shadow color, or <code>null</code> if no shadow color is set
	 */
	public Color getShadow()
	{
		return shadow;
	}

	/**
	 * Returns the current state of the marquee.
	 *
	 * @return Current state of the marquee.
	 */
	public State currentState()
	{
		double currentTime = timer.currentTime();

		double position = this.position.get(currentTime);
		double visibility = this.visibility.get(currentTime);

		double limit = 1.0 + (textBounds.getX() + textBounds.getWidth()) / 1024.0;

		if (position > limit)
		{
			nextText();
			this.position.set(currentTime, 0.0);
		}

		State state = new State(text, textBounds, position, visibility);
		return state;
	}

	private void nextText()
	{
		if (!textIterator.hasNext())
		{
			textIterator = texts.iterator();
		}
		text = textIterator.next();
		textBounds = font.getStringBounds(text, fontRenderContext);
	}

	@Override
	public GLRenderer getGLRenderer()
	{
		return new MarqueeRendererGL(this);
	}

	@Override
	public SwingRenderer getSwingRenderer()
	{
		return null;
	}

	public static class State
	{
		private String text;

		private Rectangle2D textBounds;

		private double position;

		private double visibility;

		private State(String text, Rectangle2D textBounds, double position,
		        double visibility)
		{
			super();
			this.text = text;
			this.textBounds = textBounds;
			this.position = position;
			this.visibility = visibility;
		}

		public String getText()
		{
			return text;
		}

		public Rectangle2D getTextBounds()
		{
			return textBounds;
		}

		public double getPosition()
		{
			return position;
		}

		public double getVisibility()
		{
			return visibility;
		}

	}
}
