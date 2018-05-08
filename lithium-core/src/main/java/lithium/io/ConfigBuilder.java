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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import com.github.meinders.common.*;
import lithium.*;
import lithium.Announcement.*;
import lithium.Announcement.Parameter;
import lithium.Config.*;
import lithium.Mp3Options.*;
import lithium.RecorderConfig.*;
import lithium.config.*;
import org.w3c.dom.*;

import static lithium.io.ConfigIO.*;

/**
 * This class provides the ability to read and write opwViewer configuration
 * files.
 *
 * @version 0.9 (2006.03.12)
 * @author Gerrit Meinders
 */
public class ConfigBuilder
extends ConfigurationSupport
implements Builder<Document>
{
	/** The configuration settings to build an XML representation of. */
	private Config config;

	/** The document being constructed. */
	protected Document document;

	/** The writer that receives the output of the builder. */
	private Writer out = null;

	/** The URL used to resolve relative URIs. */
	private URL context;

	/**
	 * Creates the default context URL, which points to the current/working
	 * directory.
	 *
	 * @return the default context URL
	 */
	private static URL createDefaultContextURL()
	{
		try
		{
			return new File("").toURI().toURL();
		}
		catch (MalformedURLException e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Constructs a new builder to build an XML representation of the given
	 * configuration settings. URLs are stored relative to the default context
	 * URL (a URL referring to of {@code new File("")}).
	 *
	 * @param config the configuration settings
	 */
	public ConfigBuilder(Config config)
	{
		this(config, createDefaultContextURL());
	}

	/**
	 * Constructs a new builder to build an XML representation of the given
	 * configuration settings.
	 *
	 * @param config the configuration settings
	 * @param context the URL used to relativize URIs.
	 */
	public ConfigBuilder(Config config, URL context)
	{
		this.config = config;
		this.context = context;
	}

	/**
	 * Sets the output target to which the builder should write its result.
	 *
	 * @param out the output target
	 */
	public void setOutput(Writer out)
	{
		this.out = out;
	}

	/**
	 * Builds an XML document representation of the catalog.
	 *
	 * @return the constructed document
	 */
	public Document call() throws IOException
	{
		Document document = buildDocument();
		if (out != null)
		{
			writeDocument();
		}
		return document;
	}

	/**
	 * Constructs an XML representation of the configuration settings.
	 *
	 * @return the XML document
	 */
	protected Document buildDocument()
	{
		DocumentBuilder builder;
		try
		{
			// create document builder
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			builder = builderFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
			return null;
		}

		// create document
		DOMImplementation domImpl = builder.getDOMImplementation();
		DocumentType docType = null;
		document = domImpl.createDocument(NAMESPACE, "config", docType);

		// build XML document
		Element documentElement = document.getDocumentElement();
		documentElement.setAttribute("version", "1.0");
		buildConfig(documentElement);

		// insert whitespace to improve readability
		XMLFormatter.formatXML(document, false);

		return document;
	}

	private void buildConfig(Element element)
	{
		/*
		 * Display configuration
		 */
		Element displays = document.createElement("displays");
		Acceleration acceleration = config.getAcceleration();
		if (acceleration != Acceleration.SYSTEM_DEFAULT)
		{
			displays.setAttribute("acceleration",
			        acceleration.toString().toLowerCase());
		}
		for (String id : config.getDisplayConfigIds())
		{
			DisplayConfig displayConfig = config.getDisplayConfig(id);
			displays.appendChild(buildDisplayConfig(id, displayConfig));
		}
		element.appendChild(displays);

		/*
		 * Catalogs
		 */
		Element catalogsElement = document.createElement("catalogs");
		String defaultBundle = config.getDefaultBundle();
		catalogsElement.setAttribute("default-bundle", defaultBundle);
		for (URL url : config.getCatalogURLs())
		{
			catalogsElement.appendChild(buildCatalogURL(url));
		}
		for (URL url : config.getCollectionURLs())
		{
			catalogsElement.appendChild(buildCollectionURL(url));
		}
		element.appendChild(catalogsElement);

		/*
		 * Appearance
		 */
		element.appendChild(buildAppearance());

		/*
		 * Recent files
		 */
		File[] recentFiles = config.getRecentFiles();
		Element recentFilesElement = document.createElement("recent-files");
		for (int i = 0; i < recentFiles.length; i++)
		{
			Element recentFileElement = document.createElement("recent-file");
			recentFileElement.setAttribute("name", recentFiles[i].toString());
			recentFilesElement.appendChild(recentFileElement);
		}
		element.appendChild(recentFilesElement);

		/*
		 * Announcements
		 */
		Element announcementsElement = document.createElement("announcements");
		for (Announcement preset : config.getAnnouncementPresets())
		{
			announcementsElement.appendChild(buildAnnouncementPreset(preset));
		}
		element.appendChild(announcementsElement);

		/*
		 * Utilities
		 */
		Element utilitiesElement = document.createElement("utilities");
		for (Map.Entry<String, File> entry : config.getUtilities().entrySet())
		{
			Element utility = document.createElement("utility");
			utility.setAttribute("name", entry.getKey());
			utility.setAttribute("location", entry.getValue().toString());
			utilitiesElement.appendChild(utility);
		}
		element.appendChild(utilitiesElement);

		/*
		 * Audio recorder
		 */
		Element audioRecorderElement = document.createElement("audio-recorder");
		element.appendChild(audioRecorderElement);

		RecorderConfig recorderConfig = config.getRecorderConfig();

		/*
		 * Audio recorder: recording
		 */
		Element recordElement = document.createElement("record");
		audioRecorderElement.appendChild(recordElement);

		AudioFormat audioFormat = recorderConfig.getAudioFormat();
		if (recorderConfig.getMixerName() != null)
		{
			recordElement.setAttribute("mixer", recorderConfig.getMixerName());
		}
		recordElement.setAttribute("channels",
		        String.valueOf(audioFormat.getChannels()));
		recordElement.setAttribute("bits-per-sample",
		        String.valueOf(audioFormat.getSampleSizeInBits()));
		recordElement.setAttribute("sample-rate",
		        String.valueOf((int) audioFormat.getFrameRate()));
		recordElement.setAttribute("byte-order",
		        audioFormat.isBigEndian() ? "big-endian" : "little-endian");

		/*
		 * Audio recorder: normalization
		 */
		if (recorderConfig.isNormalize())
		{
			Element normalizeElement = document.createElement("normalize");
			audioRecorderElement.appendChild(normalizeElement);

			normalizeElement.setAttribute("scope",
			        recorderConfig.isNormalizePerChannel() ? "channel" : "all");

			Element maximumGainElement = document.createElement("maximum-gain");
			maximumGainElement.appendChild(document.createTextNode(String.valueOf(recorderConfig.getMaximumGain())));
			normalizeElement.appendChild(maximumGainElement);

			Element windowSizeElement = document.createElement("window-size");
			windowSizeElement.appendChild(document.createTextNode(String.valueOf(recorderConfig.getWindowSize())));
			normalizeElement.appendChild(windowSizeElement);
		}

		/*
		 * Audio recorder: encoding
		 */
		EncodingFormat encodingFormat = recorderConfig.getEncodingFormat();

		if (encodingFormat != null)
		{
			Element encodeElement = document.createElement("encode");
			audioRecorderElement.appendChild(encodeElement);

			if (encodingFormat instanceof EncodingFormat.Wave)
			{
				encodeElement.appendChild(document.createElement("wave"));

			}
			else if (encodingFormat instanceof EncodingFormat.MP3)
			{
				Element mp3Element = document.createElement("mp3");
				encodeElement.appendChild(mp3Element);

				EncodingFormat.MP3 mp3Format = (EncodingFormat.MP3) encodingFormat;
				Mp3Options options = mp3Format.getOptions();

				Mode mode = options.getMode();
				if (mode != null)
				{
					String modeName;

					switch (mode)
					{
					default:
					case STEREO:
						modeName = "stereo";
						break;
					case JOINT_STEREO:
						modeName = "joint-stereo";
						break;
					case MONO:
						modeName = "mono";
						break;
					}

					Element modeElement = document.createElement("mode");
					modeElement.appendChild(document.createTextNode(modeName));
					mp3Element.appendChild(modeElement);
				}

				BitRate bitRate = options.getBitRate();
				if (bitRate != null)
				{
					Element bitRateElement = document.createElement("bit-rate");
					mp3Element.appendChild(bitRateElement);

					if (bitRate instanceof ConstantBitRate)
					{
						Element constantElement = document.createElement("constant");
						bitRateElement.appendChild(constantElement);

						ConstantBitRate constantBitRate = (ConstantBitRate) bitRate;
						Integer rate = constantBitRate.getBitRate();
						if (rate != null)
						{
							constantElement.setAttribute("rate",
							        rate.toString());
						}

					}
					else if (bitRate instanceof AverageBitRate)
					{
						Element averageElement = document.createElement("average");
						bitRateElement.appendChild(averageElement);

						int rate = ((AverageBitRate) bitRate).getBitRate();
						averageElement.setAttribute("rate",
						        String.valueOf(rate));

					}
					else if (bitRate instanceof VariableBitRate)
					{
						Element variableElement = document.createElement("variable");
						bitRateElement.appendChild(variableElement);

						VariableBitRate variableBitRate = (VariableBitRate) bitRate;
						Integer quality = variableBitRate.getQuality();
						if (quality != null)
						{
							variableElement.setAttribute("quality",
							        quality.toString());
						}
					}
				}
			}
		}

		/*
		 * Audio recorder: storage
		 */
		Element storeElement = document.createElement("store");
		audioRecorderElement.appendChild(storeElement);

		Element folderElement = document.createElement("folder");
		folderElement.appendChild(document.createTextNode(recorderConfig.getStorageFolder().toString()));
		storeElement.appendChild(folderElement);

		NamingScheme namingScheme = recorderConfig.getNamingScheme();
		if (namingScheme != null)
		{
			Element namingSchemeElement = document.createElement("naming-scheme");
			namingSchemeElement.setAttribute("separator",
			        namingScheme.getSeparator());
			storeElement.appendChild(namingSchemeElement);

			for (NamingScheme.Element namingElement : namingScheme.getElements())
			{
				if (namingElement instanceof NamingScheme.StringElement)
				{
					NamingScheme.StringElement stringElement = (NamingScheme.StringElement) namingElement;

					Element stringElementElement = document.createElement("string");
					stringElementElement.appendChild(document.createTextNode(stringElement.getValue()));
					namingSchemeElement.appendChild(stringElementElement);

				}
				else if (namingElement instanceof NamingScheme.DateElement)
				{
					NamingScheme.DateElement dateElement = (NamingScheme.DateElement) namingElement;

					Element dateElementElement = document.createElement("date");
					dateElementElement.setAttribute("format",
					        dateElement.getFormat().toPattern());
					namingSchemeElement.appendChild(dateElementElement);

				}
				else if (namingElement instanceof NamingScheme.SequenceElement)
				{
					Element dateElementElement = document.createElement("sequence");
					namingSchemeElement.appendChild(dateElementElement);
				}
			}
		}

		/*
		 * Boolean options.
		 */
		for (String option : config.getEnabledOptions())
		{
			Element optionElement = document.createElement("option");
			optionElement.setTextContent(option);
			element.appendChild(optionElement);
		}

		/*
		 * Load-on-startup folder.
		 */
		File loadOnStartupFolder = config.getLoadOnStartupFolder();
		if (loadOnStartupFolder != null)
		{
			Element loadOnStartupElement = document.createElement("load-on-startup");
			loadOnStartupElement.setTextContent(loadOnStartupFolder.toString());
			element.appendChild(loadOnStartupElement);
		}
	}

	private Element buildDisplayConfig(String id, DisplayConfig displayConfig)
	{
		Element element = document.createElement("display");
		element.setAttribute("id", id);
		if (displayConfig.isDeviceSet())
		{
			GraphicsDevice device = displayConfig.getDevice();
			Element deviceElement = document.createElement("device");
			deviceElement.setAttribute("id", device.getIDstring());
			element.appendChild(deviceElement);
		}
		if (displayConfig.isDisplayModeSet())
		{
			DisplayMode mode = displayConfig.getDisplayMode();
			Element modeElement = document.createElement("mode");
			modeElement.setAttribute("width", "" + mode.getWidth());
			modeElement.setAttribute("height", "" + mode.getHeight());
			modeElement.setAttribute("bitDepth", "" + mode.getBitDepth());
			modeElement.setAttribute("refreshRate", "" + mode.getRefreshRate());
			element.appendChild(modeElement);
		}
		return element;
	}

	private Element buildCatalogURL(URL url)
	{
		Element element = document.createElement("lyrics");
		setURLAttribute(element, "url", url);
		return element;
	}

	private Element buildCollectionURL(URL url)
	{
		Element element = document.createElement("books");
		setURLAttribute(element, "url", url);
		return element;
	}

	private void setURLAttribute(Element target, String attributeName, URL url)
	{
		try
		{
			URI contextPath = context.toURI().resolve(".");
			URI absolute = url.toURI();
			URI relative = contextPath.relativize(absolute);
			target.setAttribute(attributeName, relative.toString());
		}
		catch (URISyntaxException e)
		{
			target.setAttribute(attributeName, url.toString());
		}
	}

	private Element buildAppearance()
	{
		Element element = document.createElement("appearance");

		Element scrollBarElement = document.createElement("scrollbar");
		scrollBarElement.setAttribute("visible", ""
		        + config.isScrollBarVisible());
		element.appendChild(scrollBarElement);

		Element dividerElement = document.createElement("divider");
		dividerElement.setAttribute("visible", "" + config.isDividerVisible());
		element.appendChild(dividerElement);

		Element autoScrollElement = document.createElement("auto-scroll");
		autoScrollElement.setAttribute("speed", ""
		        + config.getAutoScrollSpeed());
		element.appendChild(autoScrollElement);

		// background color/image
		Element backgroundElement = document.createElement("background");
		backgroundElement.setAttribute("color", String.format("#%06x",
		        config.getBackgroundColor().getRGB() & 0xffffff));
		if (config.getBackgroundImage() != null)
		{
			setURLAttribute(backgroundElement, "image",
			        config.getBackgroundImage());
		}
		backgroundElement.setAttribute("visibleInPreview", ""
		        + config.isBackgroundVisibleInPreview());
		element.appendChild(backgroundElement);

		// scroll options
		Element scrollerElement = document.createElement("scroller");
		scrollerElement.setAttribute("type",
		        config.getScrollType().toString().toLowerCase());
		scrollerElement.setAttribute("units",
		        config.getScrollUnits().toString().toLowerCase());
		element.appendChild(scrollerElement);

		// fonts
		for (TextKind kind : TextKind.values())
		{
			Font font = config.getFont(kind);
			Element fontElement = document.createElement("font");
			fontElement.setAttribute("use", kind.toString());
			fontElement.setAttribute("family", font.getFamily());
			fontElement.setAttribute("size", String.valueOf(font.getSize()));
			fontElement.setAttribute("bold", String.valueOf(font.isBold()));
			fontElement.setAttribute("italic", String.valueOf(font.isItalic()));
			element.appendChild(fontElement);
		}

		// margins
		Rectangle2D margins = config.getFullScreenMargins();
		Element marginElement = document.createElement("margin");
		marginElement.setAttribute("left", String.valueOf(margins.getMinX()));
		marginElement.setAttribute("bottom", String.valueOf(margins.getMinY()));
		marginElement.setAttribute("right",
		        String.valueOf(1.0 - margins.getMaxX()));
		marginElement.setAttribute("top",
		        String.valueOf(1.0 - margins.getMaxY()));
		element.appendChild(marginElement);

		return element;
	}

	private Element buildAnnouncementPreset(Announcement preset)
	{
		Element element = document.createElement("preset");
		element.setAttribute("name", preset.getName());

		Element textElement = document.createElement("text");
		textElement.appendChild(document.createTextNode(preset.getText()));
		element.appendChild(textElement);

		for (Parameter param : preset.getParameters())
		{
			Element paramElement = document.createElement("param");
			paramElement.setAttribute("tag", param.getTag());

			if (param instanceof TextParameter)
			{
				TextParameter textParam = (TextParameter) param;
				paramElement.setAttribute("type", "text");
				paramElement.setAttribute("label", textParam.getLabel());

			}
			else if (param instanceof DateParameter)
			{
				DateParameter dateParam = (DateParameter) param;
				paramElement.setAttribute("type", "date");
				SimpleDateFormat format = dateParam.getFormat();
				paramElement.setAttribute("format", format.toPattern());
				if (dateParam.getValue() != null)
				{
					paramElement.setAttribute("value",
					        format.format(dateParam.getValue()));
				}

			}
			else
			{
				throw new AssertionError("unknown parameter type: " + param);
			}
			element.appendChild(paramElement);
		}
		return element;
	}

	/**
	 * Writes the constructed XML document to the previously specified output
	 * stream.
	 *
	 * @throws IOException if an exception occurs while writing the document
	 */
	protected void writeDocument() throws IOException
	{
		try
		{
			// transform DOM to Stream
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(out);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(source, result);

		}
		catch (TransformerConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			throw (IOException) new IOException().initCause(e);
		}
	}
}
