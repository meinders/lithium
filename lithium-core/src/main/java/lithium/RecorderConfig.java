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

package lithium;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.sound.sampled.*;

public class RecorderConfig {

	private String mixerName;

	private AudioFormat audioFormat;

	private boolean normalize;

	private double maximumGain;

	private double windowSize;

	private boolean normalizePerChannel;

	private EncodingFormat encodingFormat;

	private File storageFolder;

	private NamingScheme namingScheme;

	public RecorderConfig() {
		mixerName = null;
		audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100,
		        16, 2, 4, 44100, false);
		normalize = false;
		windowSize = 2.0;
		maximumGain = 10.0;
		normalizePerChannel = false;
		encodingFormat = null;
		storageFolder = new File(".");
		namingScheme = new NamingScheme();
		namingScheme.setFormat("\"recording\" date(yyyyMMdd) sequence");
	}

	public RecorderConfig(RecorderConfig original) {
		mixerName = original.mixerName;
		audioFormat = original.audioFormat;
		normalize = original.normalize;
		maximumGain = original.maximumGain;
		windowSize = original.windowSize;
		normalizePerChannel = original.normalizePerChannel;
		encodingFormat = (original.encodingFormat == null) ? null
		        : original.encodingFormat.clone();
		storageFolder = original.storageFolder;
		namingScheme = new NamingScheme(original.namingScheme);
	}

	public String getMixerName() {
	    return mixerName;
    }

	public void setMixerName(String mixerName) {
	    this.mixerName = mixerName;
    }

	/**
	 * Returns the format that audio is recorded in. If an encoding format is
	 * specified (see {@link #getEncodingFormat()}), this format is not
	 * actually written to storage.
	 *
	 * @return Audio format of recorded audio.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	public void setAudioFormat(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public double getMaximumGain() {
		return maximumGain;
	}

	public void setMaximumGain(double maximumGain) {
		this.maximumGain = maximumGain;
	}

	public double getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(double windowSize) {
		this.windowSize = windowSize;
	}

	public boolean isNormalizePerChannel() {
		return normalizePerChannel;
	}

	public void setNormalizePerChannel(boolean normalizePerChannel) {
		this.normalizePerChannel = normalizePerChannel;
	}

	/**
	 * Returns the format used to encode audio recordings before they are
	 * written to storage.
	 *
	 * @return Format used to encode the audio.
	 */
	public EncodingFormat getEncodingFormat() {
		return encodingFormat;
	}

	public void setEncodingFormat(EncodingFormat encodingFormat) {
		this.encodingFormat = encodingFormat;
	}

	public File getStorageFolder() {
		return storageFolder;
	}

	public void setStorageFolder(File storageFolder) {
		this.storageFolder = storageFolder;
	}

	public NamingScheme getNamingScheme() {
		return namingScheme;
	}

	public void setNamingScheme(NamingScheme namingScheme) {
		this.namingScheme = namingScheme;
	}

	/**
	 * Specifies the format used to encode audio recordings.
	 *
		 * @author Gerrit Meinders
	 */
	public abstract static class EncodingFormat implements Cloneable {
		@Override
		public EncodingFormat clone() {
			try {
				return (EncodingFormat) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new AssertionError(e);
			}
		}

		public static final class Wave extends EncodingFormat {
			public Wave() {
				// TODO: Add format options.
			}
		}

		public static final class MP3 extends EncodingFormat {
			private Mp3Options options;

			public MP3() {
				options = new Mp3Options();
			}

			public Mp3Options getOptions() {
				return options;
			}

			public void setOptions(Mp3Options options) {
				this.options = options;
			}
		}
	}

	/**
	 * Naming scheme for files in which audio recordings are stored.
	 *
		 * @author Gerrit Meinders
	 */
	public static class NamingScheme {
		private List<Element> elements;

		private String separator;

		public NamingScheme() {
			elements = new ArrayList<Element>();
			separator = "-";
		}

		public NamingScheme(NamingScheme original) {
			elements = new ArrayList<Element>(original.elements);
			separator = original.separator;
		}

		public List<Element> getElements() {
			return elements;
		}

		public String getSeparator() {
			return separator;
		}

		public void setSeparator(String separator) {
			this.separator = separator;
		}

		public File getFile(File storageFolder, EncodingFormat format) {
			if (storageFolder == null) {
				storageFolder = new File(".").getAbsoluteFile();
			}

			StringBuilder name = new StringBuilder();

			if (elements.isEmpty()) {
				name.append("output");

			} else {
				for (Iterator<Element> i = elements.iterator(); i.hasNext();) {
					Element element = i.next();
					element.append(name, storageFolder);

					if (i.hasNext()) {
						name.append(separator);
					}
				}
			}

			if (format instanceof EncodingFormat.MP3) {
				name.append(".mp3");
			} else if (format instanceof EncodingFormat.Wave) {
				name.append(".wav");
			}

			return new File(storageFolder, name.toString());
		}

		public abstract static class Element {
			abstract void append(StringBuilder result, File storageFolder);
		}

		public static final class StringElement extends Element {
			private String value;

			public StringElement(String value) {
				super();
				this.value = value;
			}

			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}

			@Override
			void append(StringBuilder result, File storageFolder) {
				result.append(value);
			}
		}

		public static final class DateElement extends Element {
			private SimpleDateFormat format;

			public DateElement(String format) {
				this(new SimpleDateFormat(format));
			}

			public DateElement(SimpleDateFormat format) {
				super();
				this.format = format;
			}

			public SimpleDateFormat getFormat() {
				return format;
			}

			public void setFormat(SimpleDateFormat format) {
				this.format = format;
			}

			@Override
			void append(StringBuilder result, File storageFolder) {
				result.append(format.format(new Date()));
			}
		}

		public static final class SequenceElement extends Element {
			@Override
			void append(StringBuilder result, File storageFolder) {
				int inputLength = result.length();
				final String input = result.toString();

				String[] sequenceFiles = storageFolder.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.startsWith(input);
					}
				});

				int sequenceNumber = 1;

				if ((sequenceFiles != null) && (sequenceFiles.length > 0)) {
					do {
						result.append(sequenceNumber++);
						String candidate = result.toString();

						for (String file : sequenceFiles) {
							if (file.startsWith(candidate)) {
								result.setLength(inputLength);
								break;
							}
						}

					} while (result.length() == inputLength);

				} else {
					result.append(sequenceNumber);
				}
			}
		}

		public String getFormat() {
			StringBuilder builder = new StringBuilder();

			for (Element element : elements) {
				if (builder.length() > 0) {
					builder.append(" ");
				}

				if (element instanceof StringElement) {
					StringElement stringElement = (StringElement) element;
					builder.append('"');
					builder.append(stringElement.value);
					builder.append('"');

				} else if (element instanceof DateElement) {
					DateElement dateElement = (DateElement) element;
					builder.append("date(");
					builder.append(dateElement.format.toPattern());
					builder.append(")");

				} else if (element instanceof SequenceElement) {
					SequenceElement sequenceElement = (SequenceElement) element;
					builder.append("sequence");
				}
			}

			return builder.toString();
		}

		public void setFormat(String format) {
			/*
			 * Quoted string (group 2) or identifier (group 4) with optional
			 * parameter (group 6).
			 */
			String regex = "(\"([^\"]*)\")+|((\\w+)(\\(([\\w/-]*)\\))?)";

			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(format);

			elements.clear();

			while (matcher.find()) {
				String string = matcher.group(2);
				String identifier = matcher.group(4);
				String parameter = matcher.group(6);

				if (string != null) {
					elements.add(new StringElement(string));
				} else if ("date".equals(identifier)) {
					elements.add(new DateElement(parameter));
				} else if ("sequence".equals(identifier)) {
					elements.add(new SequenceElement());
				} else {
					throw new IllegalArgumentException(matcher.group(0));
				}
			}
		}
	}
}
