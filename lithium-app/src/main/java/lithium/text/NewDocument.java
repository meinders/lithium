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
import javax.swing.*;

/**
 * <pre>
 * Input:
 *
 * text, different styles, line breaks, tabs, ...
 * </pre>
 *
 * <pre>
 * Document
 *  -blocks
 *  -margens
 *
 *  Block
 *  -lines
 *  -relativeWidth
 *  -margins
 *
 *  Row : Element
 *  -fragments
 *  -horizontalAlignment
 *  -verticalAlignment
 *
 *  TextElement : Element
 *  -text,style
 *  -width,height
 * </pre>
 *
 * <pre>
 * Output:
 *
 * -x,y
 * -text (no line breaks, tabs, ...)
 * -style (only one)
 * </pre>
 */
public class NewDocument
{
	private final FontRenderContext fontRenderContext;

	// private final Map<Style, Collection<Element>> elements;

	private float width;

	private float leftMargin;

	private float rightMargin;

	private float columnMargin;

	private final List<Column> columns;

	private final List<Row> rows;

	public NewDocument()
	{
		fontRenderContext = new FontRenderContext(null, true, true);
		// elements = new LinkedHashMap<Style, Collection<Element>>();

		columns = new ArrayList<Column>();
		rows = new ArrayList<Row>();
	}

	private float sum(float... values)
	{
		float result = 0.0f;
		for (float value : values)
		{
			result += value;
		}
		return result;
	}

	public List<Column> getColumns()
	{
		return columns;
	}

	public void addRow(Row row)
	{
		rows.add(row);
	}

	public void removeRow(Row row)
	{
		rows.remove(row);
	}

	public List<Row> getRows()
	{
		return rows;
	}

	public void updateLayout()
	{
		float totalWeight = 0.0f;
		for (Column column : this.columns)
		{
			totalWeight += column.getWeight();
		}

		float availableWidth = getAvailableWidth();

		float x = leftMargin;
		for (Column column : columns)
		{
			column.setWidth(availableWidth * column.getWeight() / totalWeight);
			column.setX(x);

			x += column.getWidth() + columnMargin;
		}

		for (Row row : rows)
		{
			row.updateLayout();
		}
	}

	// public Map<Style, Collection<Element>> getElementsByStyle() {
	// return elements;
	// }
	//
	// public void addElement(Element element) {
	// Style style = element.getStyle();
	// Collection<Element> sameStyleElements = elements.get(style);
	// if (sameStyleElements == null) {
	// sameStyleElements = new ArrayList<Element>(1);
	// elements.put(style, sameStyleElements);
	// }
	// sameStyleElements.add(element);
	// }

	public FontRenderContext getFontRenderContext()
	{
		return fontRenderContext;
	}

	//
	// abstract class Element {
	// protected Rectangle2D bounds;
	//
	// protected Style style;
	//
	// public Style getStyle() {
	// return style;
	// }
	// }
	//
	// class TextElement extends Element {
	// private String text;
	//
	// public TextElement(Point2D location, String text, TextStyle style) {
	// super();
	// this.text = text;
	// this.style = style;
	//
	// style.getBounds(location, text);
	// }
	//
	// public String getText() {
	// return text;
	// }
	//
	// public void setText(String text) {
	// this.text = text;
	// }
	// }
	//
	// public abstract class Style {
	// }
	//
	// public class TextStyle extends Style {
	// private Font font;
	//
	// private Color color;
	//
	// public TextStyle(Font font, Color color) {
	// super();
	// this.font = font;
	// this.color = color;
	// }
	//
	// public Rectangle2D getBounds(Point2D location, String text) {
	// Rectangle2D bounds = font.getStringBounds(text,
	// getFontRenderContext());
	// bounds.setFrame(bounds.getX() + location.getX(), bounds.getY()
	// + location.getY(), bounds.getWidth(), bounds.getHeight());
	// return bounds;
	// }
	//
	// public Font getFont() {
	// return font;
	// }
	//
	// public void setFont(Font font) {
	// this.font = font;
	// }
	//
	// public Color getColor() {
	// return color;
	// }
	//
	// public void setColor(Color color) {
	// this.color = color;
	// }
	// }

	public static void main(String[] args)
	{
		FontRenderContext fontRenderContext = new FontRenderContext(null, true,
		        true);

		final AttributedString text = new AttributedString(
		        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam ac eros sed justo imperdiet tincidunt. Pellentesque id est non nulla facilisis imperdiet. Aenean euismod metus eget libero. Nullam scelerisque pellentesque neque. Praesent augue nulla, facilisis nec, elementum eu, blandit ac, lectus. Aenean in dui sit amet odio malesuada fermentum. In nisl arcu, adipiscing eget, varius quis, cursus eu, neque. Phasellus et ligula. Vivamus placerat turpis eget libero. Praesent massa. Morbi tempor. Nulla nec felis nec justo adipiscing aliquam.");
		text.addAttribute(TextAttribute.FAMILY, Font.SANS_SERIF, 0, 20);
		text.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, 10,
		        30);
		text.addAttribute(TextAttribute.SIZE, 40, 20, 40);

		BreakIterator lineBreakIterator = BreakIterator.getLineInstance();
		LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(
		        text.getIterator(), lineBreakIterator, fontRenderContext);

		float wrappingWidth = 400.0f;

		final Attribute[] attributes = { TextAttribute.FAMILY,
		        TextAttribute.SIZE, TextAttribute.WEIGHT, TextAttribute.POSTURE };

		for (int beginIndex = lineBreakMeasurer.getPosition(), endIndex = lineBreakMeasurer.nextOffset(wrappingWidth); beginIndex != endIndex; beginIndex = endIndex, lineBreakMeasurer.setPosition(beginIndex), endIndex = lineBreakMeasurer.nextOffset(wrappingWidth))
		// for (int beginIndex = lineBreakIterator.first(), endIndex =
		// lineBreakIterator.next(); endIndex != BreakIterator.DONE; beginIndex
		// = endIndex, endIndex = lineBreakIterator.next())
		{
			AttributedCharacterIterator iterator = text.getIterator(attributes,
			        beginIndex, endIndex);
			for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next())
			{
				System.out.print(c);
			}
			System.out.println();
		}

		JFrame frame = new JFrame();
		frame.setContentPane(new JComponent()
		{
			{
				setPreferredSize(new Dimension(640, 480));
			}

			@Override
			protected void paintComponent(Graphics g)
			{
				g.drawString(text.getIterator(), 0, 50);
			}
		});
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);

		if (true)
			return;

		NewDocument document = new NewDocument();
		document.setWidth(1024.0f);
		document.setMargins(50.0f, 20.0f, 50.0f);

		document.ensureColumnCount(1);

		List<Column> columns = document.getColumns();
		Column column = columns.get(0);

		{
			Paragraph paragraph = new Paragraph(document);
			paragraph.setText(
			        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam ac eros sed justo imperdiet tincidunt. Pellentesque id est non nulla facilisis imperdiet. Aenean euismod metus eget libero. Nullam scelerisque pellentesque neque. Praesent augue nulla, facilisis nec, elementum eu, blandit ac, lectus. Aenean in dui sit amet odio malesuada fermentum. In nisl arcu, adipiscing eget, varius quis, cursus eu, neque. Phasellus et ligula. Vivamus placerat turpis eget libero. Praesent massa. Morbi tempor. Nulla nec felis nec justo adipiscing aliquam.",
			        new Font(Font.SANS_SERIF, Font.PLAIN, 40));
			paragraph.setColumn(column);
			Row row = new Row();
			row.addParagraph(paragraph);
			document.addRow(row);
		}

		{
			Paragraph paragraph = new Paragraph(document);
			paragraph.setText(
			        "Vestibulum at massa. Mauris consectetuer. In dignissim sapien vulputate felis convallis fringilla. Praesent euismod, quam eget lacinia consectetuer, elit purus auctor risus, in consectetuer dui enim at magna. Fusce sodales urna sed arcu placerat pellentesque. Praesent nec felis a urna cursus pulvinar. Duis mi arcu, imperdiet vitae, condimentum et, suscipit et, nisi. Integer viverra congue enim. Ut hendrerit malesuada nibh. Donec feugiat semper diam. Nulla facilisi. Proin a velit eget justo faucibus rhoncus. Quisque aliquam elementum eros. Nam mattis, felis eget rutrum tincidunt, nulla eros semper nibh, nec eleifend ante massa sed libero.",
			        new Font(Font.SANS_SERIF, Font.PLAIN, 40));
			paragraph.setColumn(column);
			Row row = new Row();
			row.addParagraph(paragraph);
			document.addRow(row);
		}

		{
			Paragraph paragraph = new Paragraph(document);
			paragraph.setText(
			        "Aenean posuere. Curabitur libero nibh, mollis ut, dignissim nec, luctus in, tellus. Maecenas et arcu. Suspendisse potenti. Cras molestie orci id dui. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. In gravida fringilla nisl. Maecenas lobortis fringilla risus. Mauris dolor tellus, placerat et, dignissim et, tincidunt eu, arcu. Nulla ultrices volutpat elit. Nam elit. Mauris congue, lacus sed viverra malesuada, massa libero gravida dui, vitae luctus enim tortor eget nibh. Etiam pellentesque, ligula in sodales egestas, lectus tellus bibendum diam, ac viverra justo nisl ut ipsum. Suspendisse potenti.",
			        new Font(Font.SANS_SERIF, Font.PLAIN, 40));
			paragraph.setColumn(column);
			Row row = new Row();
			row.addParagraph(paragraph);
			document.addRow(row);
		}

		document.updateLayout();

		// for (int lineStart = 0, lineEnd = lineInstance.next(); lineEnd !=
		// BreakIterator.DONE; lineStart = lineEnd, lineEnd =
		// lineInstance.next()) {
		// System.out.println(text.substring(lineStart, lineEnd));
		// }

		// Pattern tokenPattern = Pattern.compile("(\\S+)|(\\s+)");
		// for (Matcher matcher = tokenPattern.matcher(text); matcher.find();) {
		// if (matcher.group(1) != null) {
		//
		// } else {
		//
		// }
		// }

		// TextStyle style = new TextStyle(new Font(Font.SERIF, Font.PLAIN,
		// 14));
		//
		// Column column = documentBuilder.getDocumentColumn();
		// column.addText(loremIpsum, style);
	}

	public float getWidth()
	{
		return width;
	}

	public void setWidth(float width)
	{
		this.width = width;
	}

	public void setMargins(float left, float column, float right)
	{
		this.leftMargin = left;
		this.columnMargin = column;
		this.rightMargin = right;
	}

	public float getLeftMargin()
	{
		return leftMargin;
	}

	public float getColumnMargin()
	{
		return columnMargin;
	}

	public float getRightMargin()
	{
		return rightMargin;
	}

	public void ensureColumnCount(int columnCount)
	{
		while (columns.size() < columnCount)
		{
			columns.add(new Column(1.0f));
		}
	}

	public float getAvailableWidth()
	{
		return width - leftMargin - rightMargin - columnMargin
		        * (columns.size() - 1);
	}

	static class Column
	{
		private float weight;

		private float x;

		private float width;

		public Column(float weight)
		{
			this.weight = weight;
		}

		public float getWeight()
		{
			return weight;
		}

		public void setWeight(float weight)
		{
			this.weight = weight;
		}

		public float getWidth()
		{
			return width;
		}

		public void setWidth(float width)
		{
			this.width = width;
		}

		void setX(float x)
		{
			this.x = x;
		}

		public float getX()
		{
			return x;
		}
	}

	static class Line
	{
		private Paragraph paragraph;

		private int startIndex;

		private int endIndex;

		public Line(Paragraph paragraph, int startIndex, int endIndex)
		{
			super();
			this.paragraph = paragraph;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		public AttributedCharacterIterator getIterator()
		{
			return paragraph.getIterator(new Attribute[0], startIndex, endIndex);
		}

		@Override
		public String toString()
		{
			StringBuilder result = new StringBuilder(endIndex - startIndex + 1);

			AttributedCharacterIterator iterator = getIterator();
			for (char c = iterator.first(); c != AttributedCharacterIterator.DONE; c = iterator.next())
			{
				switch (c)
				{
				case '\t':
					result.append("    ");
				case '\f':
					result.append(' ');
					break;
				case '\n':
				case '\r':
					break;
				default:
					result.append(c);
				}
			}

			return result.toString();
		}
	}

	static class Paragraph
	{
		private AttributedString text;

		private NewDocument document;

		private Column column;

		private final List<Line> lines;

		private Font font;

		private float lineHeight = 1.0f;

		private float topMargin;

		private float bottomMargin;

		public Paragraph(NewDocument document)
		{
			this.document = document;

			lines = new ArrayList<Line>();
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
			AttributedString styledText = new AttributedString(
			        text.isEmpty() ? " " : text);

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

		public void updateLayout()
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
			AttributedCharacterIterator iterator = getIterator(
			        new Attribute[0], startIndex, endIndex);
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

	static class Row
	{
		private List<Paragraph> paragraphs;

		public Row()
		{
			paragraphs = new ArrayList<Paragraph>();
		}

		public void addParagraph(Paragraph paragraph)
		{
			paragraphs.add(paragraph);
		}

		public void removeParagraph(Paragraph paragraph)
		{
			paragraphs.remove(paragraph);
		}

		public List<Paragraph> getParagraphs()
		{
			return paragraphs;
		}

		public void updateLayout()
		{
			for (Paragraph paragraph : paragraphs)
			{
				paragraph.updateLayout();
			}
		}
	}
}
