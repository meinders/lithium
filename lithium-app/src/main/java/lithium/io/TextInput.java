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

package lithium.io;

import java.io.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;

import org.apache.poi.hwpf.extractor.*;

/**
 * Provides utility methods for reading text from plain text, rich text and Word
 * documents.
 *
 * @author Gerrit Meinders
 */
public class TextInput
{
	/**
	 * Magic number sequence identifying an RTF file.
	 */
	private static final byte[] RTF_MAGIC = "{\\rtf".getBytes();

	/**
	 * Magic number sequence identifying a Word file.
	 */
	private static final byte[] WORD_MAGIC = new byte[] { (byte) 0xd0,
	        (byte) 0xcf, (byte) 0x11, (byte) 0xe0 };

	public static Object read(File file) throws IOException
	{
		final BufferedInputStream in = new BufferedInputStream(
		        new FileInputStream(file));
		try
		{
			if (checkMagic(in, RTF_MAGIC))
			{
				return readRichText(in);
			}
			else if (checkMagic(in, WORD_MAGIC))
			{
				return readWordText(in);
			}
			else
			{
				return readPlainText(new InputStreamReader(in));
			}
		}
		finally
		{
			in.close();
		}
	}

	public static CharSequence readPlainText(File file) throws IOException
	{
		final BufferedReader in = new BufferedReader(new FileReader(file));
		try
		{
			return readPlainText(in);
		}
		finally
		{
			in.close();
		}
	}

	public static Object readRichText(File file) throws IOException
	{
		final BufferedInputStream in = new BufferedInputStream(
		        new FileInputStream(file));

		try
		{
			return readRichText(in);
		}
		finally
		{
			in.close();
		}
	}

	public static Object readWordText(File file) throws IOException
	{
		final BufferedInputStream in = new BufferedInputStream(
		        new FileInputStream(file));
		try
		{
			return readWordText(in);
		}
		finally
		{
			in.close();
		}
	}

	private static CharSequence readPlainText(final Reader in)
	        throws IOException
	{
		int read;
		StringBuilder text = new StringBuilder();
		while ((read = in.read()) != -1)
		{
			text.append((char) read);
		}
		return text;
	}

	private static Object readRichText(final InputStream in) throws IOException
	{
		final RTFEditorKit editorKit = new RTFEditorKit();
		final Document document = editorKit.createDefaultDocument();
		try
		{
			editorKit.read(in, document, 0);
			return document.getText(0, document.getLength());
		}
		catch (BadLocationException e)
		{
			throw new IOException(e);
		}
	}

	private static Object readWordText(final InputStream in) throws IOException
	{
		StringBuilder buffer = new StringBuilder();
		WordExtractor extractor = new WordExtractor(in);
		String[] paragraphs = extractor.getParagraphText();
		for (String paragraph : paragraphs)
		{
			buffer.append(paragraph.trim());
			buffer.append('\n');
		}
		return buffer;
	}

	/**
	 * Checks the given input stream for the presence of the given magic number
	 * sequence (used to identify the stream's content type.)
	 *
	 * @param in the input stream to read from
	 * @param magic the magic number sequence to look for
	 *
	 * @return whether the given magic number sequence is found.
	 *
	 * @throws IOException if the stream doesn't support mark/reset.
	 */
	private static boolean checkMagic(final InputStream in, final byte[] magic)
	        throws IOException
	{
		if (!in.markSupported())
		{
			throw new IOException("mark not supported");
		}

		in.mark(magic.length);

		boolean result = true;
		for (int i = 0; i < magic.length; i++)
		{
			if (magic[i] != in.read())
			{
				result = false;
				break;
			}
		}

		in.reset();
		return result;
	}
}
