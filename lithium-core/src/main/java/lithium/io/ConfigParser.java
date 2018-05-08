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
import java.util.List;
import javax.sound.sampled.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import lithium.*;
import lithium.Announcement.*;
import lithium.Announcement.Parameter;
import lithium.Config.*;
import lithium.RecorderConfig.*;
import lithium.config.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import static javax.xml.xpath.XPathConstants.*;
import static lithium.io.ConfigIO.*;

/**
 * A parser for opwViewer configuration files.
 *
 * @version 0.9 (2006.03.12)
 * @author Gerrit Meinders
 */
public class ConfigParser
extends ConfigurationSupport
implements Parser<Config>
{
	/** The configuration being constructed by the parser. */
	private Config config;

	/** The input source of the parser. */
	private Reader in = null;

	/** The URL used to resolve relative URIs. */
	private URL context;

	/** Constructs a new config parser. */
	public ConfigParser()
	{
		try
		{
			context = new File("").toURI().toURL();
		}
		catch (MalformedURLException e)
		{
			// nothing to be done about it
			e.printStackTrace();
		}
	}

	/**
	 * Sets the input source of the parser.
	 *
	 * @param in the reader to be used as an input source
	 */
	public void setInput(Reader in)
	{
		this.in = in;
	}

	/**
	 * Sets the URL used to resolve relative URIs in the parsed configuration.
	 *
	 * @param context the context URL
	 */
	public void setContext(URL context)
	{
		this.context = context;
	}

	/**
	 * Reads a configuration from the parser's input source.
	 *
	 * @return the configuration
	 * @throws IOException if an exception occurs while parsing
	 */
	public Config call() throws IOException
	{
		if (in == null)
		{
			throw new NullPointerException("input not set");
		}

		Document document;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new DefaultEntityResolver());
			document = builder.parse(new InputSource(in));
			in.close();

		}
		catch (SAXException e)
		{
			e.printStackTrace();
			throw (IOException) new IOException().initCause(e);
		}
		catch (ParserConfigurationException e)
		{
			throw new AssertionError(e);
		}

		String namespace = document.getDocumentElement().getNamespaceURI();
		if (NAMESPACE.equals(namespace))
		{
			try
			{
				return parseConfig(document);
			}
			catch (XPathExpressionException e)
			{
				throw (IOException) new IOException().initCause(e);
			}
		}
		else
		{
			// use DTD for backward-compatibility
			DocumentType doctype = document.getDoctype();
			if (doctype == null)
			{
				throw (IOException) new IOException().initCause(new ParseException(
				        INVALID_DTD, 0));
			}

			String publicId = doctype.getPublicId();
			if (PUBLIC_ID.equals(publicId))
			{
				return parseOldConfig(document);
			}
			else
			{
				throw (IOException) new IOException().initCause(new ParseException(
				        INVALID_DTD, 0));
			}
		}
	}

	private Config parseConfig(Document document) throws IOException,
	        XPathExpressionException
	{
		config = new Config();

		XPath xpath = XPathFactory.newInstance().newXPath();
		Element root = document.getDocumentElement();

		// define config namespace prefix (XPath needs it)
		NamespaceSupportContext nsContext = new NamespaceSupportContext();
		nsContext.declarePrefix("cfg", NAMESPACE);
		xpath.setNamespaceContext(nsContext);

		// display configuration
		Element displaysElement = (Element) xpath.evaluate("cfg:displays[1]",
		        root, NODE);
		if (displaysElement != null
		        && displaysElement.hasAttribute("acceleration"))
		{
			String value = displaysElement.getAttribute("acceleration");
			Acceleration acceleration = Enum.valueOf(Acceleration.class,
			        value.toUpperCase());
			config.setAcceleration(acceleration);
		}
		NodeList displays = (NodeList) xpath.evaluate(
		        "cfg:displays/cfg:display", root, NODESET);
		for (int i = 0; i < displays.getLength(); i++)
		{
			Element display = (Element) displays.item(i);
			String id = display.getAttribute("id");
			DisplayConfig displayConfig = parseDisplayConfig(display);
			config.addDisplayConfig(id, displayConfig);
		}

		// catalogs
		NodeList catalogElements = (NodeList) xpath.evaluate("cfg:catalogs/*",
		        root, NODESET);
		for (int i = 0; i < catalogElements.getLength(); i++)
		{
			Element catalog = (Element) catalogElements.item(i);
			String tagName = catalog.getTagName().intern();
			URL url;
			try
			{
				url = new URL(context, catalog.getAttribute("url"));
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
				throw (IOException) new IOException().initCause(new ParseException(
				        INVALID_CONTENT, 0));
			}
			if (tagName == "catalog" || tagName == "lyrics")
			{
				config.addCatalogURL(url);
			}
			else if (tagName == "books")
			{
				config.addCollectionURL(url);
			}
			else
			{
				throw (IOException) new IOException().initCause(new ParseException(
				        INVALID_CONTENT, 0));
			}
		}

		String defaultBundle = (String) xpath.evaluate(
		        "cfg:catalogs/@default-bundle", root, STRING);
		if (defaultBundle.length() > 0)
		{
			config.setDefaultBundle(defaultBundle);
		}

		// appearance
		config.setScrollBarVisible(Boolean.parseBoolean(xpath.evaluate(
		        "cfg:appearance/cfg:scrollbar/@visible", root)));
		config.setDividerVisible(Boolean.parseBoolean(xpath.evaluate(
		        "cfg:appearance/cfg:divider/@visible", root)));

		// appearance: auto-scroll
		Element autoScrollElement = (Element) xpath.evaluate(
		        "cfg:appearance/cfg:auto-scroll", root, NODE);
		if (autoScrollElement != null)
		{
			String speed = autoScrollElement.getAttribute("speed");
			config.setAutoScrollSpeed(Float.parseFloat(speed));
		}

		// appearance: background
		Element backgroundElement = (Element) xpath.evaluate(
		        "cfg:appearance/cfg:background", root, NODE);
		if (backgroundElement != null)
		{
			String colorString = backgroundElement.getAttribute("color");
			Color color;
			if (colorString != "")
			{
				color = Color.decode(colorString);
				config.setBackgroundColor(color);
			}

			String imageString = backgroundElement.getAttribute("image");
			URL image = null;
			if (imageString != "")
			{
				try
				{
					image = new URL(context, imageString);
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
			}
			if (image != null)
			{
				config.setBackgroundImage(image);
			}

			boolean visibleInPreview = Boolean.parseBoolean(backgroundElement.getAttribute("visibleInPreview"));
			config.setBackgroundVisibleInPreview(visibleInPreview);
		}

		// appearance: scroll options
		Element scrollerElement = (Element) xpath.evaluate(
		        "cfg:appearance/cfg:scroller", root, NODE);
		if (scrollerElement != null)
		{
			String type = scrollerElement.getAttribute("type");
			if (type.equals("plain"))
			{
				config.setScrollType(Config.ScrollType.PLAIN);
			}
			else
			{
				config.setScrollType(Config.ScrollType.SMOOTH);
			}

			String units = scrollerElement.getAttribute("units");
			if (units.equals("characters"))
			{
				config.setScrollUnits(Config.ScrollUnits.CHARACTERS);
			}
			else
			{
				config.setScrollUnits(Config.ScrollUnits.LINES);
			}
		}

		// Appearance: fonts
		NodeList fontElements = (NodeList) xpath.evaluate(
		        "cfg:appearance/cfg:font", root, NODESET);
		for (int i = 0; i < fontElements.getLength(); i++)
		{
			Element fontElement = (Element) fontElements.item(i);
			String use = fontElement.getAttribute("use");

			TextKind kind;
			try
			{
				kind = TextKind.valueOf(use);
			}
			catch (IllegalArgumentException e)
			{
				continue;
			}

			String family = fontElement.getAttribute("family");
			int size = fontElement.hasAttribute("size") ? Integer.parseInt(fontElement.getAttribute("size"))
			        : kind.getDefaultSize();
			boolean bold = fontElement.hasAttribute("bold")
			        && Boolean.parseBoolean(fontElement.getAttribute("bold"));
			boolean italic = fontElement.hasAttribute("italic")
			        && Boolean.parseBoolean(fontElement.getAttribute("italic"));
			int style = (bold ? Font.BOLD : Font.PLAIN)
			        | (italic ? Font.ITALIC : Font.PLAIN);

			Font font = new Font(family, style, size);
			config.setFont(kind, font);
		}

		// appearance: margins
		Element marginElement = (Element) xpath.evaluate(
		        "cfg:appearance/cfg:margin", root, NODE);
		if (marginElement != null)
		{
			double left = Double.parseDouble(marginElement.getAttribute("left"));
			double bottom = Double.parseDouble(marginElement.getAttribute("bottom"));
			double right = Double.parseDouble(marginElement.getAttribute("right"));
			double top = Double.parseDouble(marginElement.getAttribute("top"));
			config.setFullScreenMargins(new Rectangle2D.Double(left, bottom,
			        1.0 - left - right, 1.0 - bottom - top));
		}

		// recent-files
		NodeList recentFiles = (NodeList) xpath.evaluate(
		        "cfg:recent-files/cfg:recent-file", root, NODESET);
		for (int i = recentFiles.getLength() - 1; i >= 0; i--)
		{
			Element recentFile = (Element) recentFiles.item(i);
			File file = new File(recentFile.getAttribute("name"));
			config.setRecentFile(file);
		}

		// utilities
		NodeList utilityNodes = (NodeList) xpath.evaluate(
		        "cfg:utilities/cfg:utility", root, NODESET);
		for (int i = 0; i < utilityNodes.getLength(); i++)
		{
			Element utilityNode = (Element) utilityNodes.item(i);
			String name = utilityNode.getAttribute("name");
			String location = utilityNode.getAttribute("location");
			File utility = new File(location);
			config.addUtility(name, utility);
		}

		// announcement presets
		NodeList announcementNodes = (NodeList) xpath.evaluate(
		        "cfg:announcements/cfg:preset", root, NODESET);
		Set<Announcement> announcements = new LinkedHashSet<Announcement>();
		for (int i = 0; i < announcementNodes.getLength(); i++)
		{
			Element announcementNode = (Element) announcementNodes.item(i);
			String name = announcementNode.getAttribute("name");
			String text = xpath.evaluate("cfg:text", announcementNode).trim();
			Announcement announcement = new Announcement(name, text);

			NodeList paramNodes = (NodeList) xpath.evaluate("cfg:param",
			        announcementNode, NODESET);
			for (int j = 0; j < paramNodes.getLength(); j++)
			{
				Element paramNode = (Element) paramNodes.item(j);
				Parameter parameter = parseParameter(paramNode);
				announcement.addParameter(parameter);
			}

			announcements.add(announcement);
		}
		config.setAnnouncementPresets(announcements);

		/*
		 * Audio recorder.
		 */
		RecorderConfig recorderConfig = new RecorderConfig();
		config.setRecorderConfig(recorderConfig);

		Element audioRecorderElement = (Element) xpath.evaluate(
		        "cfg:audio-recorder", root, NODE);
		if (audioRecorderElement != null)
		{
			/*
			 * Audio recorder: recording settings.
			 */
			Element recordElement = (Element) xpath.evaluate("cfg:record",
			        audioRecorderElement, NODE);

			String mixer = (String) xpath.evaluate("@mixer", recordElement,
			        STRING);
			int channels = ((Number) xpath.evaluate("@channels", recordElement,
			        NUMBER)).intValue();
			int bitsPerSample = ((Number) xpath.evaluate("@bits-per-sample",
			        recordElement, NUMBER)).intValue();
			int sampleRate = ((Number) xpath.evaluate("@sample-rate",
			        recordElement, NUMBER)).intValue();
			String byteOrder = xpath.evaluate("@byte-order", recordElement);

			AudioFormat audioFormat = new AudioFormat(
			        AudioFormat.Encoding.PCM_SIGNED, sampleRate, bitsPerSample,
			        channels, bitsPerSample * channels / 8, sampleRate,
			        "big-endian".equals(byteOrder));

			if (!mixer.isEmpty())
			{
				recorderConfig.setMixerName(mixer);
			}
			recorderConfig.setAudioFormat(audioFormat);

			/*
			 * Audio recorder: normalization settings.
			 */
			Element normalizeElement = (Element) xpath.evaluate(
			        "cfg:normalize", audioRecorderElement, NODE);
			if (normalizeElement != null)
			{
				String scope = xpath.evaluate("@scope", normalizeElement);
				double windowSize = ((Number) xpath.evaluate("cfg:window-size",
				        normalizeElement, NUMBER)).doubleValue();
				double maxGain = ((Number) xpath.evaluate("cfg:maximum-gain",
				        normalizeElement, NUMBER)).doubleValue();

				recorderConfig.setNormalize(true);
				recorderConfig.setNormalizePerChannel("channel".equals(scope));
				recorderConfig.setMaximumGain(maxGain);
				recorderConfig.setWindowSize(windowSize);
			}

			/*
			 * Audio recorder: encoding settings.
			 */
			Element encodeElement = (Element) xpath.evaluate("cfg:encode",
			        audioRecorderElement, NODE);
			if (encodeElement != null)
			{
				Element formatElement = (Element) xpath.evaluate(
				        "cfg:wave|cfg:mp3", encodeElement, NODE);
				String formatName = formatElement.getTagName();

				if ("wave".equals(formatName))
				{
					recorderConfig.setEncodingFormat(new RecorderConfig.EncodingFormat.Wave());

				}
				else if ("mp3".equals(formatName))
				{
					RecorderConfig.EncodingFormat.MP3 format = new RecorderConfig.EncodingFormat.MP3();
					Mp3Options options = format.getOptions();

					String mode = xpath.evaluate("mode", formatElement);
					if ("stereo".equals(mode))
					{
						options.setMode( Mp3Options.Mode.STEREO);
					}
					else if ("joint-stereo".equals(mode))
					{
						options.setMode( Mp3Options.Mode.JOINT_STEREO);
					}
					else if ("mono".equals(mode))
					{
						options.setMode( Mp3Options.Mode.MONO);
					}

					Element bitRate = (Element) xpath.evaluate(
					        "cfg:bit-rate/cfg:*", formatElement, NODE);
					if (bitRate != null)
					{
						String bitRateType = bitRate.getTagName();

						if ("constant".equals(bitRateType))
						{
							Integer rate = null;
							if (bitRate.hasAttribute("rate"))
							{
								rate = Integer.valueOf(bitRate.getAttribute("rate"));
							}
							options.setBitRate(new Mp3Options.ConstantBitRate(rate));

						}
						else if ("average".equals(bitRateType))
						{
							int rate = Integer.parseInt(bitRate.getAttribute("rate"));
							options.setBitRate(new Mp3Options.AverageBitRate(rate));

						}
						else if ("variable".equals(bitRateType))
						{
							Integer quality = null;
							if (bitRate.hasAttribute("quality"))
							{
								quality = Integer.valueOf(bitRate.getAttribute("quality"));
							}
							options.setBitRate(new Mp3Options.VariableBitRate(
							        quality));
						}
					}

					recorderConfig.setEncodingFormat(format);
				}
			}

			/*
			 * Audio recorder: storage settings.
			 */
			Element storeElement = (Element) xpath.evaluate("cfg:store",
			        audioRecorderElement, NODE);
			File storageFolder = new File(xpath.evaluate("cfg:folder",
			        storeElement));
			recorderConfig.setStorageFolder(storageFolder);

			Element namingSchemeElement = (Element) xpath.evaluate(
			        "cfg:naming-scheme", storeElement, NODE);
			if (namingSchemeElement != null)
			{
				NamingScheme namingScheme = new NamingScheme();
				recorderConfig.setNamingScheme(namingScheme);

				if (namingSchemeElement.hasAttribute("separator"))
				{
					String separator = namingSchemeElement.getAttribute("separator");
					namingScheme.setSeparator(separator);
				}

				List<NamingScheme.Element> elements = namingScheme.getElements();
				NodeList elementNodes = (NodeList) xpath.evaluate("cfg:*",
				        namingSchemeElement, NODESET);
				for (int i = 0; i < elementNodes.getLength(); i++)
				{
					Element elementNode = (Element) elementNodes.item(i);
					String elementType = elementNode.getTagName();

					if ("string".equals(elementType))
					{
						String value = xpath.evaluate(".", elementNode);
						elements.add(new NamingScheme.StringElement(value));

					}
					else if ("date".equals(elementType))
					{
						String format = elementNode.getAttribute("format");
						elements.add(new NamingScheme.DateElement(format));

					}
					else if ("sequence".equals(elementType))
					{
						elements.add(new NamingScheme.SequenceElement());
					}
				}
			}
		}

		/*
		 * Boolean options, e.g. debug flags.
		 */
		NodeList optionElements = (NodeList) xpath.evaluate("cfg:option", root,
		        NODESET);
		for (int i = 0; i < optionElements.getLength(); i++)
		{
			Element optionElement = (Element) optionElements.item(i);
			config.setEnabled(optionElement.getTextContent(), true);
		}

		/*
		 * Load-on-startup folder.
		 */
		String loadOnStartupPath = xpath.evaluate("cfg:load-on-startup", root);
		if (!loadOnStartupPath.isEmpty())
		{
			config.setLoadOnStartupFolder(new File(loadOnStartupPath));
		}

		return config;
	}

	private DisplayConfig parseDisplayConfig(Element element)
	        throws IOException
	{
		DisplayConfig displayConfig = new DisplayConfig();

		NodeList devices = element.getElementsByTagName("device");
		if (devices.getLength() > 0)
		{
			Element device = (Element) devices.item(0);
			String deviceId = device.getAttribute("id");
			displayConfig.setDevice(deviceId);
		}

		NodeList modes = element.getElementsByTagName("mode");
		if (modes.getLength() > 0)
		{
			Element mode = (Element) modes.item(0);
			try
			{
				int width = Integer.parseInt(mode.getAttribute("width"));
				int height = Integer.parseInt(mode.getAttribute("height"));
				int bitDepth = Integer.parseInt(mode.getAttribute("bitDepth"));
				int refreshRate = Integer.parseInt(mode.getAttribute("refreshRate"));
				DisplayMode displayMode = new DisplayMode(width, height,
				        bitDepth, refreshRate);
				displayConfig.setDisplayMode(displayMode);

			}
			catch (NumberFormatException e)
			{
				throw (IOException) new IOException().initCause(e);
			}
		}

		return displayConfig;
	}

	private Parameter parseParameter(Element element) throws IOException
	{
		String tag = element.getAttribute("tag");
		String type = element.getAttribute("type");

		if (type.equals("text"))
		{
			String label = element.getAttribute("label");
			TextParameter param = new TextParameter(tag, label);
			return param;

		}
		else if (type.equals("date"))
		{
			String formatPattern = element.getAttribute("format");
			SimpleDateFormat format = new SimpleDateFormat(formatPattern);
			Date value;
			if (element.hasAttribute("value"))
			{
				String formattedValue = element.getAttribute("value");
				if (formattedValue.length() == 0)
				{
					value = null;
				}
				else if ("today".equals(formattedValue))
				{
					value = new Date();
				}
				else
				{
					try
					{
						value = format.parse(formattedValue);
					}
					catch (ParseException e)
					{
						throw (IOException) new IOException().initCause(e);
					}
				}
			}
			else
			{
				value = null;
			}
			DateParameter param = new DateParameter(tag, format, value);
			return param;

		}
		else
		{
			throw new IOException("Invalid announcement parameter type: "
			        + type);
		}
	}

	private Config parseOldConfig(Document document) throws IOException
	{
		Element rootElement = document.getDocumentElement();
		config = new Config();

		NodeList displays = rootElement.getElementsByTagName("display");
		for (int i = 0; i < displays.getLength(); i++)
		{
			Element display = (Element) displays.item(i);
			String id = display.getAttribute("id");

			DisplayConfig displayConfig = new DisplayConfig();

			NodeList devices = display.getElementsByTagName("device");
			if (devices.getLength() > 0)
			{
				Element device = (Element) devices.item(0);
				String deviceId = device.getAttribute("id");
				displayConfig.setDevice(deviceId);
			}

			NodeList modes = display.getElementsByTagName("mode");
			if (modes.getLength() > 0)
			{
				Element mode = (Element) modes.item(0);
				try
				{
					int width = Integer.parseInt(mode.getAttribute("width"));
					int height = Integer.parseInt(mode.getAttribute("height"));
					int bitDepth = Integer.parseInt(mode.getAttribute("bitDepth"));
					int refreshRate = Integer.parseInt(mode.getAttribute("refreshRate"));
					DisplayMode displayMode = new DisplayMode(width, height,
					        bitDepth, refreshRate);
					displayConfig.setDisplayMode(displayMode);

				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			}

			config.addDisplayConfig(id, displayConfig);
		}

		NodeList catalogsNodeList = rootElement.getElementsByTagName("catalogs");
		Element catalogsElement = (Element) catalogsNodeList.item(0);
		NodeList catalogs = catalogsElement.getElementsByTagName("catalog");
		for (int i = 0; i < catalogs.getLength(); i++)
		{
			Element catalog = (Element) catalogs.item(i);
			try
			{
				URL url = new URL(context, catalog.getAttribute("url"));
				config.addCatalogURL(url);

			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
				throw (IOException) new IOException().initCause(new ParseException(
				        INVALID_CONTENT, 0));
			}
		}

		NodeList defaultBundleNodeList = rootElement.getElementsByTagName("default-bundle");
		Element defaultBundleElement = (Element) defaultBundleNodeList.item(0);
		config.setDefaultBundle(defaultBundleElement.getAttribute("id"));

		NodeList scrollBarNodeList = rootElement.getElementsByTagName("scrollbar");
		Element scrollBarElement = (Element) scrollBarNodeList.item(0);
		config.setScrollBarVisible(Boolean.parseBoolean(scrollBarElement.getAttribute("visible")));

		NodeList dividerNodeList = rootElement.getElementsByTagName("divider");
		Element dividerElement = (Element) dividerNodeList.item(0);
		config.setDividerVisible(Boolean.parseBoolean(dividerElement.getAttribute("visible")));

		// recent-files (optional)
		NodeList recentFilesNodeList = rootElement.getElementsByTagName("recent-files");
		if (recentFilesNodeList.getLength() > 0)
		{
			Element recentFilesElement = (Element) recentFilesNodeList.item(0);
			NodeList recentFiles = recentFilesElement.getElementsByTagName("recent-file");
			for (int i = recentFiles.getLength() - 1; i >= 0; i--)
			{
				Element recentFile = (Element) recentFiles.item(i);
				File file = new File(recentFile.getAttribute("name"));
				config.setRecentFile(file);
			}
		}

		// auto-scroll (optional)
		NodeList autoScrollNodeList = rootElement.getElementsByTagName("auto-scroll");
		if (autoScrollNodeList.getLength() > 0)
		{
			Element autoScrollElement = (Element) autoScrollNodeList.item(0);
			String speed = autoScrollElement.getAttribute("speed");
			if (speed != "")
			{
				config.setAutoScrollSpeed(Float.parseFloat(speed));
			}
		}

		// background (optional)
		NodeList backgroundNodeList = rootElement.getElementsByTagName("background");
		if (backgroundNodeList.getLength() > 0)
		{
			Element backgroundElement = (Element) backgroundNodeList.item(0);

			String colorString = backgroundElement.getAttribute("color");
			Color color;
			if (colorString != "")
			{
				color = Color.decode(colorString);
				config.setBackgroundColor(color);
			}

			String imageString = backgroundElement.getAttribute("image");
			URL image = null;
			if (imageString != "")
			{
				try
				{
					image = new URL(context, imageString);
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
			}
			if (image != null)
			{
				config.setBackgroundImage(image);
			}

			boolean visibleInPreview = Boolean.parseBoolean(backgroundElement.getAttribute("visibleInPreview"));
			config.setBackgroundVisibleInPreview(visibleInPreview);
		}

		// scroll options (optional)
		NodeList scrollerNodeList = rootElement.getElementsByTagName("scroller");
		if (scrollerNodeList.getLength() > 0)
		{
			Element scrollerElement = (Element) scrollerNodeList.item(0);
			String type = scrollerElement.getAttribute("type");
			if (type.equals("plain"))
			{
				config.setScrollType(Config.ScrollType.PLAIN);
			}
			else
			{
				config.setScrollType(Config.ScrollType.SMOOTH);
			}

			String units = scrollerElement.getAttribute("units");
			if (units.equals("characters"))
			{
				config.setScrollUnits(Config.ScrollUnits.CHARACTERS);
			}
			else
			{
				config.setScrollUnits(Config.ScrollUnits.LINES);
			}
		}

		// utilities (optional)
		NodeList utilitiesNodes = rootElement.getElementsByTagName("utilities");
		if (utilitiesNodes.getLength() > 0)
		{
			Element utilitiesElement = (Element) utilitiesNodes.item(0);
			NodeList utilityNodes = utilitiesElement.getElementsByTagName("utility");
			for (int i = 0; i < utilityNodes.getLength(); i++)
			{
				Element utilityNode = (Element) utilityNodes.item(i);
				String name = utilityNode.getAttribute("name");
				String location = utilityNode.getAttribute("location");
				File utility = new File(location);
				if ("ppt".equals(name))
				{
					if (utility == null)
					{
						config.removeUtility(Config.UTILITY_PPT);
					}
					else
					{
						config.addUtility(Config.UTILITY_PPT, utility);
					}
				}
			}
		}

		return config;
	}
}
