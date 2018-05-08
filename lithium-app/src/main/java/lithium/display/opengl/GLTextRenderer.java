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
import java.util.*;
import javax.media.opengl.*;

import com.jogamp.opengl.util.awt.*;
import lithium.*;
import lithium.animation.*;
import lithium.catalog.*;
import lithium.display.*;
import lithium.text.*;

public class GLTextRenderer
implements GLContentRenderer
{
	protected final ViewModel model;

	private final Map<Font, TextRenderer> textRenderers;

	public GLTextRenderer( ViewModel model )
	{
		this.model = model;
		textRenderers = new HashMap<Font, TextRenderer>();
	}

	@Override
	public boolean isViewBackgroundVisible()
	{
		return true;
	}

	@Override
	public boolean accept( Object content )
	{
		return content instanceof Lyric || content instanceof LyricRef
		       || content instanceof BibleRef
		       || content instanceof CharSequence;
	}

	@Override
	public PreparedContent prepare( final Object content )
	{
		final DocumentBuilder builder = new DocumentBuilder();
		final Document document = builder.newDocument( content );

		final PreparedContent preparedContent = new PreparedContent( document, null )
		{
			private final int modificationCount = getModificationCount( content );

			@Override
			public boolean isValid()
			{
				return modificationCount == getModificationCount( content );
			}

			private int getModificationCount( Object object )
			{
				if ( object instanceof Lyric )
				{
					return ( (Lyric)object ).getModificationCount();
				}
				else
				{
					return 0;
				}
			}
		};

		return preparedContent;
	}

	@Override
	public boolean ready( Object prepared )
	{
		return true;
	}

	@Override
	public void render( GL gl, Rectangle2D bounds, Point2D offset,
	                    double contentAlpha, Object prepared )
	{
		Document document = (Document)prepared;
		if ( document == null )
		{
			return;
		}

		float width = (float)bounds.getWidth() + (float)bounds.getMinX();
		float leftMargin = (float)bounds.getMinX();
		// (float) margins.getMinX() * width;
		float rightMargin = 0.0f;
		// (float) (1.0 - margins.getMaxX()) * width;
		float columnMargin = 20.0f;

		if ( ( document.getWidth() != width )
		     || ( document.getLeftMargin() != leftMargin )
		     || ( document.getColumnMargin() != columnMargin )
		     || ( document.getRightMargin() != rightMargin ) )
		{
			document.setWidth( width );
			document.setMargins( leftMargin, columnMargin, rightMargin );
			document.updateLayout();
		}

		// int titleLineHeight = titleMetrics.getHeight();
		float normalLineHeight = getNormalLineHeight();
		// normalMetrics.getHeight();
		// int smallLineHeight = smallMetrics.getHeight();
		// int smallestLineHeight = smallestMetrics.getHeight();

		float[] viewportCoords = new float[4];
		gl.glGetFloatv( GL.GL_VIEWPORT, viewportCoords, 0 );
		float viewportWidth = viewportCoords[ 2 ];
		float viewportHeight = viewportCoords[ 3 ];
		float normalizedViewportHeight = 1024.0f * viewportHeight / viewportWidth;

		Config config = ConfigManager.getConfig();
		Rectangle2D margins = config.getFullScreenMargins();

		int visibleHeight = (int)( 1024.0f / model.getAspectRatio() );
		float visibleBottom = 0.5f * ( normalizedViewportHeight - visibleHeight ) + visibleHeight * (float)margins.getMinY();
		float visibleTop = visibleBottom + visibleHeight * (float)margins.getHeight();

		float scrollValue = (float)offset.getY();
		float lineY = visibleTop + scrollValue * normalLineHeight;
		// FIXME + view.scrollModelToView(scrollValue, normalLineHeight);

		NewScroller scroller = (NewScroller)model.getScroller();
		Color foregroundColor = withAlpha( config.getForegroundColor(), contentAlpha * scroller.getVisibility() );

		if ( document != null )
		{
			TextRenderer currentRenderer = null;

			/*
			 * Experimental: shadow
			 */
			boolean experimentalShadow = config.isEnabled( Config.TEXT_SHADOW );

			float r = 5.0f;
			// double t = System.nanoTime() / 1000000000.0;
			float xoff = 3.0f;// 10.0f * (float) Math.sin(t);
			float yoff = -4.0f;// -10.0f * (float) Math.cos(t);
			int steps = 1;// (int) (r / 2.0f);
			float alpha = 1.0f / ( ( steps * 2 + 1 ) * ( steps * 2 + 1 ) );

			float lineHeight = 0.0f;

			for ( Row row : document.getRows() )
			{
				float rowStartY = lineY;
				float rowEndY = rowStartY;

				for ( Paragraph paragraph : row.getParagraphs() )
				{
					{
						TextRenderer textRenderer = getRenderer( paragraph.getFont() );
						if ( textRenderer == null )
						{
							continue;
						}
						if ( currentRenderer != textRenderer )
						{
							if ( currentRenderer != null )
							{
								currentRenderer.end3DRendering();
							}
							textRenderer.begin3DRendering();
							currentRenderer = textRenderer;
							lineHeight = (float)currentRenderer.getFont().getMaxCharBounds(
							currentRenderer.getFontRenderContext() ).getHeight();
						}
					}

					Column column = paragraph.getColumn();
					float columnLeft = column == null ? document.getLeftMargin()
					                                  : column.getX();

					lineY = rowStartY;
					lineY -= paragraph.getTopMargin();
					lineY -= lineHeight * paragraph.getLineHeight();

					for ( Line line : paragraph.getLines() )
					{
						String text = line.toString();

						if ( config.isEnabled( Config.RENDER_TEXT_BASELINE ) )
						{
							currentRenderer.end3DRendering();
							final GL2 gl2 = gl.getGL2();
							gl2.glPushAttrib( GL2GL3.GL_COLOR );
							gl2.glColor3f( 1.0f, 1.0f, 0.0f );
							gl2.glBegin( GL.GL_LINES );
							gl2.glVertex2d( leftMargin, lineY );
							gl2.glVertex2d( width, lineY );
							gl2.glEnd();
							gl2.glPopAttrib();
							currentRenderer.begin3DRendering();
						}

						if ( ( lineY + lineHeight >= 0 )
						     && ( lineY <= normalizedViewportHeight ) )
						{

							if ( experimentalShadow )
							{
								currentRenderer.setColor( withAlpha( Color.BLACK, alpha ) );
								for ( int x = -steps; x <= steps; x++ )
								{
									for ( int y = -steps; y <= steps; y++ )
									{
										currentRenderer.draw3D( text, columnLeft + 2 * x + xoff, lineY + 2 * y + yoff, 0.0f, 1.0f );
									}
								}
							}

							currentRenderer.setColor( foregroundColor );
							currentRenderer.draw3D( text, columnLeft, lineY, 0.0f, 1.0f );
						}

						lineY -= lineHeight * paragraph.getLineHeight();
					}

					lineY += lineHeight * paragraph.getLineHeight();
					lineY -= paragraph.getBottomMargin();

					if ( lineY < rowEndY )
					{
						rowEndY = lineY;
					}
				}
				lineY = rowEndY;
			}

			if ( currentRenderer != null )
			{
				currentRenderer.end3DRendering();
				currentRenderer = null;
			}
			lineY -= lineHeight;
		}
	}

	protected TextRenderer getRenderer( Font font )
	{
		TextRenderer result = textRenderers.get( font );

		if ( result == null )
		{
			result = new TextRenderer( font, true, false );
			result.setSmoothing( true );
			textRenderers.put( font, result );
		}

		return result;
	}

	private TextRenderer getRenderer( Config.TextKind kind )
	{
		Config config = ConfigManager.getConfig();
		return getRenderer( config.getFont( kind ) );
	}

	public float getNormalLineHeight()
	{
		final TextRenderer normalTextRenderer = getRenderer( Config.TextKind.DEFAULT );
		return normalTextRenderer == null ? 0 : 2.0f * (float)normalTextRenderer.getBounds( "x" ).getHeight();
	}

	protected Color withAlpha( Color color, double alpha )
	{
		return withAlpha( color, (float)alpha );
	}

	private Color withAlpha( Color color, float alpha )
	{
		float[] components = color.getRGBColorComponents( new float[3] );
		return new Color( components[ 0 ], components[ 1 ], components[ 2 ], alpha );
	}
}
