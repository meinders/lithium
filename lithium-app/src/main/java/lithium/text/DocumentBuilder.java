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
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import lithium.*;
import lithium.books.*;
import lithium.catalog.*;

/**
 * Creates formatted text documents from various data sources, including
 * {@link Lyric}, {@link LyricRef} and {@link BibleRef}.
 *
 * @author G. Meinders
 */
public class DocumentBuilder
{
	private Config config;

	/**
	 * Construct new DocumentBuilder.
	 */
	public DocumentBuilder()
	{
		config = ConfigManager.getConfig();
	}

	public Document newDocument( Object content )
	{
		final Document document;

		if ( content instanceof Lyric )
		{
			Catalog catalog = CatalogManager.getCatalog();
			final Lyric lyric = (Lyric)content;

			document = new Document();

			{
				Row row = new Row();
				document.addRow( row );

				Paragraph paragraph = new Paragraph();
				String reference = catalog.getBundle( lyric ).getName() + " " + lyric.getNumber();
				paragraph.setText( reference, config.getFont( Config.TextKind.REFERENCE ) );
				paragraph.setTopMargin( 20.0f );
				row.addParagraph( paragraph );
			}

			{
				Row row = new Row();
				document.addRow( row );

				Paragraph paragraph = new Paragraph();
				String title = lyric.getTitle();
				paragraph.setText( title, config.getFont( Config.TextKind.TITLE ) );
				row.addParagraph( paragraph );
				paragraph.setBottomMargin( 40.0f );
			}

			{
				appendText( document, lyric.getText() );
			}

			{
				Row row = new Row();
				document.addRow( row );

				Paragraph paragraph = new Paragraph();
				String copyrights = lyric.getCopyrights();
				paragraph.setText( copyrights, config.getFont( Config.TextKind.COPYRIGHTS ) );
				row.addParagraph( paragraph );
				paragraph.setTopMargin( 20.0f );
			}

		}
		else if ( content instanceof LyricRef )
		{
			Catalog catalog = CatalogManager.getCatalog();
			LyricRef lyricRef = (LyricRef)content;
			Lyric lyric = catalog.getLyric( lyricRef );
			document = newDocument( lyric );
		}
		else if ( content instanceof BibleRef )
		{
			// FIXME: This isn't really the place for I/O.
			BibleRef ref = (BibleRef)content;
			ArchiveLibrary bible = null;
			try
			{
				final List<URL> collectionURLs = config.getCollectionURLs();
				if ( !collectionURLs.isEmpty() )
				{
					final URL url = collectionURLs.get( 0 );
					try
					{
						bible = new ArchiveLibrary( url.toURI() );
					}
					catch ( URISyntaxException e )
					{
						throw new IOException( e );
					}
				}
			}
			catch ( IOException e )
			{
				e.printStackTrace();
				throw new AssertionError( e );
			}

			if ( bible == null )
			{
				return null;
			}

			Book book = bible.getBook( ref.getBookName() );
			Font titleFont = config.getFont( Config.TextKind.TITLE );
			Font plainFont = config.getFont( Config.TextKind.DEFAULT );
			Font referenceFont = config.getFont( Config.TextKind.REFERENCE );

			document = new Document();

			{
				Paragraph paragraph = new Paragraph();
				paragraph.setText( bible.getName(), referenceFont );
				paragraph.setTopMargin( 10.0f );
				Row row = new Row();
				row.addParagraph( paragraph );
				document.addRow( row );
			}

			{
				Paragraph paragraph = new Paragraph();
				paragraph.setText( ref.toString(), titleFont );
				paragraph.setBottomMargin( 20.0f );
				Row row = new Row();
				row.addParagraph( paragraph );
				document.addRow( row );
			}

			if ( book == null )
			{
				Paragraph paragraph = new Paragraph();
				// FIXME: i18n
				paragraph.setText( "(niet beschikbaar)", plainFont );
				Row row = new Row();
				row.addParagraph( paragraph );
				document.addRow( row );

			}
			else
			{
				Integer startChapter = ref.getStartChapter();
				Integer endChapter = ref.getEndChapter();
				if ( endChapter == null )
				{
					endChapter = startChapter;
				}

				for ( int i = startChapter; i <= endChapter; i++ )
				{
					Chapter chapter = book.getChapter( i );
					if ( chapter == null )
					{
						break;
					}

					if ( i > startChapter )
					{
						Paragraph paragraph = new Paragraph();
						paragraph.setText( ref.getBookName() + " " + chapter.getNumber(), titleFont );
						paragraph.setBottomMargin( 20.0f );
						Row row = new Row();
						row.addParagraph( paragraph );
						document.addRow( row );
					}

					final Collection<Verse> verses;

					Integer startVerse = ref.getStartVerse();
					Integer endVerse = ref.getEndVerse();
					if ( startVerse == null )
					{
						verses = chapter.getVerses();
					}
					else if ( endVerse == null )
					{
						verses = new HashSet<Verse>();
						verses.add( chapter.getVerse( startVerse ) );
					}
					else
					{
						verses = new TreeSet<Verse>();
						for ( Verse verse : chapter.getVerses() )
						{
							boolean inside = true;
							if ( i == startChapter )
							{
								if ( verse.getRangeEnd() < startVerse )
								{
									inside = false;
								}
							}
							if ( i == endChapter )
							{
								if ( verse.getRangeStart() > endVerse )
								{
									inside = false;
								}
							}
							if ( inside )
							{
								verses.add( verse );
							}
						}
					}

					for ( Verse verse : verses )
					{
						StringBuilder text = new StringBuilder();
						text.append( "(" );
						text.append( verse.getRange() );
						text.append( ") " );

						text.append( verse.getText() );

						Paragraph paragraph = new Paragraph();
						paragraph.setLineHeight( 1.2f );
						paragraph.setText( text.toString(), plainFont );
						Row row = new Row();
						row.addParagraph( paragraph );
						document.addRow( row );
					}
				}
			}
		}
		else if ( content instanceof CharSequence )
		{
			document = new Document();

			appendText( document, content.toString() );

			List<Row> rows = document.getRows();
			if ( !rows.isEmpty() )
			{
				Row row = rows.get( 0 );
				List<Paragraph> paragraphs = row.getParagraphs();
				if ( !paragraphs.isEmpty() )
				{
					Paragraph paragraph = paragraphs.get( 0 );
					paragraph.setTopMargin( 20.0f );
				}
			}
		}
		else
		{
			document = null;
		}

		return document;
	}

	private void appendText( Document document, String text )
	{
		List<String> lines = new ArrayList<String>(
		Arrays.asList( text.split( "[\\s&&[^\r\n]]*((\r\n?)|\n)" ) ) );
		normalizeWhitespace( lines );

		if ( !config.isEnabled( Config.DISABLE_REFERENCE_EXPANSION ) )
		{
			expandReferences( lines );
		}

		Font font = config.getFont( Config.TextKind.DEFAULT );

		for ( String line : lines )
		{
			Row row = new Row();
			document.addRow( row );

			String[] columns = line.split( "[ ]*\t[ ]*" );

			document.ensureColumnCount( columns.length );

			String extraColumn = null;

			if ( columns.length > 0 )
			{
				int lastColumnIndex = columns.length - 1;
				String lastColumn = columns[ lastColumnIndex ];
				int lastParens = lastColumn.lastIndexOf( ')' );

				if ( lastParens > 0
				     && Character.isWhitespace( lastColumn.charAt( lastParens - 1 ) ) )
				{
					extraColumn = lastColumn.substring( lastParens );
					columns[ lastColumnIndex ] = lastColumn.substring( 0,
					                                                   lastParens );
					document.ensureColumnCount( columns.length + 1 );
				}
			}

			for ( int i = 0; i < columns.length; i++ )
			{
				Column column = document.getColumns().get( i );
				column.setWeight( 8.0f );
				Paragraph paragraph = new Paragraph();
				paragraph.setColumn( column );
				paragraph.setText( columns[ i ], font );
				row.addParagraph( paragraph );
			}

			if ( extraColumn != null )
			{
				Column column = document.getColumns().get( columns.length );
				Paragraph paragraph = new Paragraph();
				paragraph.setColumn( column );
				paragraph.setText( extraColumn, font );
				row.addParagraph( paragraph );
			}
		}
	}

	private void normalizeWhitespace( List<String> lines )
	{
		for ( ListIterator<String> i = lines.listIterator(); i.hasNext(); )
		{
			String line = i.next();

			boolean onlyWhitespace = true;
			for ( int j = 0; j < line.length(); j++ )
			{
				if ( !Character.isWhitespace( line.charAt( j ) ) )
				{
					onlyWhitespace = false;
					break;
				}
			}

			if ( onlyWhitespace )
			{
				i.set( "" );
			}
			else
			{
				i.set( line.replaceAll( "\\s*\t\\s*\\)", " \\)" ) );
			}
		}
	}

	/**
	 * Expands references found in the given text. By adding a label in the text,
	 * all lines following it until the first empty line can be referenced. A label
	 * takes the form of a line with a word followed by a colon. The reference
	 * consists of the same word, enclosed by braces. Additionally, a reference
	 * must be followed by an empty line or the end of the text. Forward references
	 * are not allowed.
	 *
	 * <p> References may contain additional content. E.g. "(Chorus 3x)" is a valid
	 * reference to a label "Chorus:".
	 *
	 * @param lines Text split into lines.
	 */
	private void expandReferences( List<String> lines )
	{
		Map<String, List<String>> referenceMap = new HashMap<String, List<String>>();
		List<String> content = null;
		for ( ListIterator<String> i = lines.listIterator(); i.hasNext(); )
		{
			String line = i.next();

			if ( ( content == null ) && line.endsWith( ":" ) )
			{
				content = new ArrayList<String>();
				String label = line.substring( 0, line.length() - 1 );
				referenceMap.put( label.toLowerCase(), content );
			}
			else if ( line.startsWith( "(" ) && line.endsWith( ")" ) )
			{
				boolean validReference = true;
				if ( i.hasNext() )
				{
					String next = i.next();
					validReference = ( next == null ) || next.isEmpty();
					i.previous();
				}

				if ( validReference )
				{
					String[] parts = line.split( "\\(|\\)|\\s", 3 );
					if ( parts.length >= 2 )
					{
						String reference = parts[ 1 ];
						List<String> referenceContent = referenceMap.get( reference.toLowerCase() );
						if ( referenceContent != null )
						{
							for ( String s : referenceContent )
							{
								i.add( s );
							}
						}
					}
				}
			}
			else if ( line.isEmpty() )
			{
				content = null;
			}
			else if ( content != null )
			{
				content.add( line );
			}
		}
	}
}
