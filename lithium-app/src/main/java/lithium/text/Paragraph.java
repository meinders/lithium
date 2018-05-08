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

package lithium.text;

import java.awt.*;
import java.awt.font.*;
import java.text.*;
import java.text.AttributedCharacterIterator.*;
import java.util.*;
import java.util.List;

public class Paragraph
{
	private AttributedString text;

	private Column column;

	private final List<Line> lines;

	private Font font;

	private float lineHeight = 1.0f;

	private float topMargin;

	private float bottomMargin;

	public Paragraph()
	{
		lines = new ArrayList<Line>();
	}

	/**
	 * Sets the lines that make up the paragraph. Note that this will disable
	 * any reformatting of the text. In most cases, the {@link #setText} method
	 * should be used instead.
	 *
	 * @param lines Lines to be set.
	 */
	public void setLines( final List<Line> lines )
	{
		this.lines.clear();
		this.lines.addAll( lines );
	}

	public void setFont( Font font )
	{
		this.font = font;
	}

	public Column getColumn()
	{
		return column;
	}

	public void setColumn(Column column)
	{
		this.column = column;
	}

	public void setText(String text, Font font)
	{
		AttributedString styledText = new AttributedString(text.isEmpty() ? " "
		        : text);

		this.font = font;

		styledText.addAttribute(TextAttribute.FAMILY, font.getFamily());

		if (font.isBold())
		{
			styledText.addAttribute(TextAttribute.WEIGHT,
			        TextAttribute.WEIGHT_BOLD);
		}

		if (font.isItalic())
		{
			styledText.addAttribute(TextAttribute.POSTURE,
			        TextAttribute.POSTURE_OBLIQUE);
		}

		styledText.addAttribute(TextAttribute.SIZE, font.getSize());
		this.text = styledText;
	}

	public void updateLayout(Document document)
	{
		if ( text != null )
		{
			updateLayoutFromText( document );
		}
	}

	private void updateLayoutFromText(Document document)
	{
		FontRenderContext fontRenderContext = document.getFontRenderContext();

		BreakIterator lineBreakIterator = BreakIterator.getLineInstance();
		LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(
		        text.getIterator(), lineBreakIterator, fontRenderContext);

		Column column = getColumn();
		float wrappingWidth = (column == null) ? document.getAvailableWidth()
		        : column.getWidth();

		lines.clear();
		for (int startIndex = lineBreakMeasurer.getPosition(), endIndex = lineBreakMeasurer.nextOffset(wrappingWidth); startIndex != endIndex; startIndex = endIndex, lineBreakMeasurer.setPosition(startIndex), endIndex = lineBreakMeasurer.nextOffset(wrappingWidth))
		{
			int lineBreak = findExplicitLineBreak(startIndex, endIndex);
			if (lineBreak >= 0)
			{
				endIndex = lineBreak + 1;
			}
			Line line = new Line(this, startIndex, endIndex);
			lines.add(line);
		}
	}

	public AttributedCharacterIterator getIterator(Attribute[] attributes,
	        int startIndex, int endIndex)
	{
		return text.getIterator(attributes, startIndex, endIndex);
	}

	public List<Line> getLines()
	{
		return lines;
	}

	public Font getFont()
	{
		return font;
	}

	public float getLineHeight()
	{
		return lineHeight;
	}

	public void setLineHeight(float lineHeight)
	{
		this.lineHeight = lineHeight;
	}

	public float getTopMargin()
	{
		return topMargin;
	}

	public void setTopMargin(float topMargin)
	{
		this.topMargin = topMargin;
	}

	public float getBottomMargin()
	{
		return bottomMargin;
	}

	public void setBottomMargin(float bottomMargin)
	{
		this.bottomMargin = bottomMargin;
	}

	public int findExplicitLineBreak(int startIndex, int endIndex)
	{
		AttributedCharacterIterator iterator = getIterator(new Attribute[0],
		        startIndex, endIndex);
		for (char c = iterator.first(); c != AttributedCharacterIterator.DONE; c = iterator.next())
		{
			if (c == '\n')
			{
				return iterator.getIndex();
			}
		}
		return -1;
	}
}
