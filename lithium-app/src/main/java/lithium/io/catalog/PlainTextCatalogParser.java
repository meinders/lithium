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

package lithium.io.catalog;

import java.io.*;
import java.net.*;
import java.util.*;

import lithium.catalog.*;
import lithium.config.*;
import lithium.io.*;

/**
 * A parser for catalogs stored as plain text files.
 *
 * @version 0.9 (2006.02.24)
 * @author Gerrit Meinders
 */
public class PlainTextCatalogParser extends ConfigurationSupport
implements
        Parser<MutableCatalog> {
	private Reader in = null;

	private boolean renumberSequentially;

	private boolean renumberStartAtEnabled;

	private int renumberStartAt;

	public PlainTextCatalogParser() {
		setRenumberSequentially(false);
		setRenumberStartAt(true, 1);
	}

	/**
	 * Sets the input source of the parser.
	 *
	 * @param in the reader to be used as an input source
	 */
	public void setInput(Reader in) {
		this.in = in;
	}

	public Integer getRenumberStartAt() {
		return renumberStartAtEnabled ? renumberStartAt : null;
	}

	public boolean isRenumberSequentially() {
		return renumberSequentially;
	}

	public void setRenumberSequentially(boolean renumberSequentially) {
		this.renumberSequentially = renumberSequentially;
	}

	public void setRenumberStartAt(boolean enabled) {
		setRenumberStartAt(enabled, 1);
	}

	public void setRenumberStartAt(boolean enabled, int start) {
		this.renumberStartAtEnabled = enabled;
		this.renumberStartAt = start;
	}

	public MutableCatalog parse(File file) throws IOException {
		return parse(file.toURI().toURL());
	}

	public MutableCatalog parse(URL url) throws IOException {
		in = new InputStreamReader(url.openStream());
		return call();
	}

	public MutableCatalog call() throws IOException {
		if (in == null) {
			throw new NullPointerException("input source not set");
		}

		// XXX: progressListener.setStarted();
		// XXX: progressListener.setIndeterminate(true);

		LineNumberReader in = new LineNumberReader(this.in);

		Bundle bundle = (Bundle) Group.createBundle();
		// XXX: bundle name?

		DefaultCatalog catalog = new DefaultCatalog();
		catalog.addBundle(bundle);

		{
			boolean emptyLine = false;
			boolean endOfFile = false;
			LinkedList<String> lineBuffer = new LinkedList<String>();
			Lyric lyric = null;
			int number = 0;

			while (true) {
				// read a line
				String line;
				line = in.readLine();

				if (line == null) {
					endOfFile = true;
					// the remainder of the loop adds the last Lyric to bundle

				} else {
					line = line.trim();

					// skip empty lines that are preceded by an empty line or
					// nothing
					if (line.length() == 0) {
						if (emptyLine || lineBuffer.size() == 0) {
							continue;
						} else {
							emptyLine = true;
						}
					} else {
						emptyLine = false;
					}
				}

				// parse and perform checks on textual content
				try {
					int nextNumber = 0;
					if (!endOfFile) {
						nextNumber = Integer.parseInt(line);
						if (nextNumber == 0) {
							assert false : "warning/error handling not implemented";
							// TODO: design: warning/error handling
						}
					}

					// end of previous Lyric
					if (number != 0) {
						if (bundle.getLyric(number) == null) {
							// remove leading whitespace
							while (lineBuffer.getFirst().length() == 0) {
								lineBuffer.removeFirst();
							}

							// set title
							lyric.setTitle(lineBuffer.removeFirst());

							// remove leading and trailing whitespace
							while (lineBuffer.getFirst().length() == 0) {
								lineBuffer.removeFirst();
							}
							while (lineBuffer.getLast().length() == 0) {
								lineBuffer.removeLast();
							}

//							// find start of copyrights
//							int lastEmptyLine = lineBuffer.lastIndexOf("");
//							if (lastEmptyLine == -1) {
//								lastEmptyLine = lineBuffer.size();
//							}

							// merge lines into text and copyrights
							StringBuffer textBuffer = new StringBuffer();
							StringBuffer copyrightsBuffer = new StringBuffer();
							int count = 0;
							for (String bufferedLine : lineBuffer) {
//								if (count < lastEmptyLine) {
									if (textBuffer.length() > 0) {
										textBuffer.append('\n');
									}
									textBuffer.append(bufferedLine);
//								} else if (count > lastEmptyLine) {
//									if (copyrightsBuffer.length() > 0) {
//										copyrightsBuffer.append('\n');
//									}
//									copyrightsBuffer.append(bufferedLine);
//								} else {
//									// skip line between text and copyrights
//								}
								count++;
							}

							// set property in Lyric
							String text = textBuffer.toString();
							lyric.setText(text);

							// check copyrights and set property in Lyric
							String copyrights = "";//copyrightsBuffer.toString();
							lyric.setCopyrights(copyrights);

							// add lyric
							bundle.addLyric(lyric);

						} else {
							// warn if duplicate number is found
							// XXX:
							// feedbackListener.setWarning(Resources.get().getString(
							// XXX: "plainTextCatalogParser.duplicate",
							// XXX: in.getLineNumber(), number));
						}
					}

					if (endOfFile) {
						// done
						break;

					} else {
						// continue with next Lyric

						// warn if numbers are out of sequence
						int expected = number + 1;
						if (nextNumber != expected) {
							// XXX:
							// feedbackListener.setWarning(Resources.get().getString(
							// XXX: "plainTextCatalogParser.outOfOrder",
							// XXX: in.getLineNumber(), expected, nextNumber));
						}

						// create new empty Lyric
						number = nextNumber;
						lyric = new DefaultLyric(number, "");
						lineBuffer.clear();
					}

				} catch (NumberFormatException e) {
					// not a number; store the line for later use
					lineBuffer.add(line.trim());
				}
			}
			in.close();
		}

		// renumber: start at
		if (renumberStartAtEnabled) {
			Collection<Lyric> lyricsCollection = bundle.getLyrics();
			Lyric[] lyrics = lyricsCollection.toArray(new Lyric[lyricsCollection.size()]);

			if (renumberSequentially) {
				// renumber sequentially, starting from renumberStartAt
				bundle.removeLyrics();
				for (int i = 0; i < lyrics.length; i++) {
					int number = i + renumberStartAt;
					bundle.addLyric(new DefaultLyric(number, lyrics[i]));
				}

			} else {
				/*
				 * Translate all numbers such that the lowest number becomes
				 * renumberStartAt.
				 */
				int lowestNumber = Integer.MAX_VALUE;
				for (int i = 0; i < lyrics.length; i++) {
					Lyric lyric = lyrics[i];
					if (lyric.getNumber() < lowestNumber) {
						lowestNumber = lyric.getNumber();
					}
				}
				int offset = renumberStartAt - lowestNumber;
				if (offset != 0) {
					bundle.removeLyrics();
					for (int i = 0; i < lyrics.length; i++) {
						Lyric lyric = lyrics[i];
						int number = lyric.getNumber() + offset;
						bundle.addLyric(new DefaultLyric(number, lyric));
					}
				}
			}
		}

		// XXX: progressListener.setDone();
		return catalog;
	}
}
