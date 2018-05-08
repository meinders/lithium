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

package lithium.display.opengl;

import java.awt.*;
import java.awt.geom.*;
import javax.media.opengl.*;

import com.jogamp.opengl.util.awt.*;
import lithium.display.*;
import lithium.display.Marquee.*;

/**
 * A GUI plugin renderer that displays text in a marquee style, scrolling from
 * right to left.
 *
 * @version 0.9x (2006.03.15)
 * @author Gerrit Meinders
 */
public class MarqueeRendererGL implements GUIPlugin.GLRenderer
{
	private Marquee model;

	private TextRenderer textRenderer;

	/**
	 * Constructs a marquee that displays the given text. Line endings are
	 * replaced by a large horizontal space.
	 *
	 * @param model the marquee to be rendered
	 */
	public MarqueeRendererGL(Marquee model)
	{
		super();
		this.model = model;
	}

	@Override
	public void init(GL gl)
	{
		textRenderer = new TextRenderer(model.getFont());
		textRenderer.setSmoothing(true);
	}

	@Override
	public void render(GL gl, Rectangle2D viewBounds)
	{
		if (textRenderer == null)
		{
			init(gl);
		}

		State state = model.currentState();
		Rectangle2D textBounds = state.getTextBounds();

		float x = (float) (viewBounds.getWidth() * (1.0 - (float) state.getPosition()));
		float y = (float) (viewBounds.getHeight() + textBounds.getY());

		textRenderer.begin3DRendering();

		textRenderer.setColor(withAlpha(model.getShadow(), 0.2f));
		drawBlurry(gl, state.getText(), x, y, viewBounds, 1.01f, 1, 3.0f);

		x += viewBounds.getX();
		y += viewBounds.getY();
		textRenderer.setColor(model.getForeground());
		textRenderer.draw3D(state.getText(), x, y, 0.0f, 1.0f);

		textRenderer.end3DRendering();
	}

	private void drawBlurry(GL gl, String text, float x, float y,
	        Rectangle2D viewBounds, float scale, int steps, float stepSize)
	{
		float shadowX = (float) ((x - viewBounds.getCenterX()) * scale + viewBounds.getCenterX());
		float shadowY = y - 4.0f;

		shadowX += viewBounds.getX();
		shadowY += viewBounds.getY();

		for (int i = -steps; i <= steps; i++)
		{
			for (int j = -steps; j <= steps; j++)
			{
				textRenderer.draw3D(text, shadowX + i * stepSize, shadowY + j
				        * stepSize, 0.0f, scale);
			}
		}
	}

	private Color withAlpha(Color color, double alpha)
	{
		return withAlpha(color, (float) alpha);
	}

	private Color withAlpha(Color color, float alpha)
	{
		float[] components = color.getRGBColorComponents(new float[3]);
		return new Color(components[0], components[1], components[2], alpha);
	}
}
