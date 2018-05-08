/*
 * Copyright 2011 Gerrit Meinders
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
import java.util.*;
import java.util.List;

public class Document {
	private final FontRenderContext fontRenderContext;

	// private final Map<Style, Collection<Element>> elements;

	private float width;

	private float leftMargin;

	private float rightMargin;

	private float columnMargin;

	private final List<Column> columns;

	private final List<Row> rows;

	public Document() {
		fontRenderContext = new FontRenderContext(null, true, true);
		// elements = new LinkedHashMap<Style, Collection<Element>>();

		columns = new ArrayList<Column>();
		rows = new ArrayList<Row>();
	}

	private float sum(float... values) {
		float result = 0.0f;
		for (float value : values) {
			result += value;
		}
		return result;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void addRow(Row row) {
		rows.add(row);
	}

	public void removeRow(Row row) {
		rows.remove(row);
	}

	public List<Row> getRows() {
		return rows;
	}

	public void updateLayout() {
		float totalWeight = 0.0f;
		for (Column column : this.columns) {
			totalWeight += column.getWeight();
		}

		float availableWidth = getAvailableWidth();

		float x = leftMargin;
		for (Column column : columns) {
			column.setWidth(availableWidth * column.getWeight() / totalWeight);
			column.setX(x);

			x += column.getWidth() + columnMargin;
		}

		for (Row row : rows) {
			row.updateLayout(this);
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

	public FontRenderContext getFontRenderContext() {
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

	public static void main(String[] args) {
		Document document = new Document();
		document.setWidth(1024.0f);
		document.setMargins(50.0f, 20.0f, 50.0f);

		document.ensureColumnCount(1);

		List<Column> columns = document.getColumns();
		Column column = columns.get(0);

		{
			Paragraph paragraph = new Paragraph();
			paragraph.setText(
			        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam ac eros sed justo imperdiet tincidunt. Pellentesque id est non nulla facilisis imperdiet. Aenean euismod metus eget libero. Nullam scelerisque pellentesque neque. Praesent augue nulla, facilisis nec, elementum eu, blandit ac, lectus. Aenean in dui sit amet odio malesuada fermentum. In nisl arcu, adipiscing eget, varius quis, cursus eu, neque. Phasellus et ligula. Vivamus placerat turpis eget libero. Praesent massa. Morbi tempor. Nulla nec felis nec justo adipiscing aliquam.",
			        new Font(Font.SANS_SERIF, Font.PLAIN, 40));
			paragraph.setColumn(column);
			Row row = new Row();
			row.addParagraph(paragraph);
			document.addRow(row);
		}

		{
			Paragraph paragraph = new Paragraph();
			paragraph.setText(
			        "Vestibulum at massa. Mauris consectetuer. In dignissim sapien vulputate felis convallis fringilla. Praesent euismod, quam eget lacinia consectetuer, elit purus auctor risus, in consectetuer dui enim at magna. Fusce sodales urna sed arcu placerat pellentesque. Praesent nec felis a urna cursus pulvinar. Duis mi arcu, imperdiet vitae, condimentum et, suscipit et, nisi. Integer viverra congue enim. Ut hendrerit malesuada nibh. Donec feugiat semper diam. Nulla facilisi. Proin a velit eget justo faucibus rhoncus. Quisque aliquam elementum eros. Nam mattis, felis eget rutrum tincidunt, nulla eros semper nibh, nec eleifend ante massa sed libero.",
			        new Font(Font.SANS_SERIF, Font.PLAIN, 40));
			paragraph.setColumn(column);
			Row row = new Row();
			row.addParagraph(paragraph);
			document.addRow(row);
		}

		{
			Paragraph paragraph = new Paragraph();
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

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public void setMargins(float left, float column, float right) {
		this.leftMargin = left;
		this.columnMargin = column;
		this.rightMargin = right;
	}

	public float getLeftMargin() {
		return leftMargin;
	}

	public float getColumnMargin() {
		return columnMargin;
	}

	public float getRightMargin() {
		return rightMargin;
	}

	public void ensureColumnCount(int columnCount) {
		while (columns.size() < columnCount) {
			columns.add(new Column(1.0f));
		}
	}

	public float getAvailableWidth() {
		return width - leftMargin - rightMargin - columnMargin
		        * (columns.size() - 1);
	}
}
