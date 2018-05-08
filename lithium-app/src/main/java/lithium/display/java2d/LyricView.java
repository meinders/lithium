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

package lithium.display.java2d;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.text.*;

import com.github.meinders.common.animation.*;
import lithium.*;
import lithium.animation.legacy.scrolling.*;
import lithium.books.*;
import lithium.catalog.*;
import lithium.display.*;

import static javax.swing.ScrollPaneConstants.*;

/**
 * <p>
 * This component is a view for Lyric objects, and at the same time a controller
 * for the underlying LyricViewModel. The view automatically loads relevant
 * configuration settings directly from the ConfigManager and Config classes.
 *
 * <p>
 * The LyricView is controlled through a seperate LyricViewModel, which may be
 * shared amongst multiple LyricView instances. A shared LyricViewModel results
 * in identical scrolling, lyrics and visibility for all LyricViews sharing that
 * model.
 *
 * @see ViewModel
 * @see ConfigManager
 *
 * @since 0.1
 * @version 0.9 (2006.04.16)
 * @author Gerrit Meinders
 */
public class LyricView extends JLayeredPane implements PropertyChangeListener {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** Style constant: smallest plain text */
    private static final String SMALLEST = "smallest";

    /** Style constant: small plain text */
    private static final String SMALL = "small";

    /** Style constant: plain text */
    private static final String NORMAL = "normal";

    /** Style constant: title */
    private static final String TITLE = "title";

    private static final String BASE_FONT_FAMILY = "Sans Serif";

    private static final int SMALLEST_SIZE = 14;

    private static final int SMALL_SIZE = 24;

    private static final int NORMAL_SIZE = 40;

    private ViewModel model;

    /** @since 0.9x (2005.08.09) */
    private GUIPluginManager pluginManager;

    private boolean preview;

    private final boolean showFramesPerSecond = false;

    private final FrameCounter counter = showFramesPerSecond ? new FrameCounter() : null;

    private Config config;

    private BufferedImage backgroundImage = null;

    private URL backgroundImageURL = null;

    private Image acceleratedBackground = null;

    private float scale = 1.0f;

    private int marginTop = 0;

    private int marginBottom = 0;

    private StyledDocument document;

    private JTextPane textPane;

    private JScrollPane scrollPane;

    /** @since 0.9x (2005.08.09) */
    private JPanel pluginsPanel;

    /**
     * Constructs a new lyric view using the given model. The constructed view
     * will not be a preview.
     *
     * @param model the model
     */
    public LyricView(ViewModel model) {
        this(model, false);
    }

    /**
     * Constructs a new lyric view using the given model either as a preview or
     * regular view.
     *
     * @param model the model
     * @param preview whether the view should be a preview
     */
    public LyricView(ViewModel model, boolean preview) {
        super();
        setModel(model);
        this.preview = preview;
        init();
    }

    /** Releases the resources used by the view, if any. */
    public void dispose() {
        setModel(null);
        setPluginManager(null);
        setConfig(null);
	    ConfigManager configManager = ConfigManager.getInstance();
        configManager.removePropertyChangeListener(this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        scrollPane.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    /**
     * Sets the model to be used by the view.
     *
     * @param model the model
     */
    public void setModel(ViewModel model) {
        if (this.model != null) {
            this.model.removePropertyChangeListener(this);
            this.model.unregister(this);
        }
        this.model = model;
        if (model != null) {
            setPluginManager(model.getGUIPluginManager());

            // update scroll bar models
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                BoundedRangeModel oldRangeModel = vertical.getModel();
                if (oldRangeModel instanceof AdvancedTextRangeModel) {
                    AdvancedTextRangeModel textRangeModel = (AdvancedTextRangeModel) oldRangeModel;
                    textRangeModel.dispose();
                }
                vertical.setModel(createRangeModel(model));
            }

            model.addPropertyChangeListener(this);
            model.register(this);
        }
    }

    /**
     * Sets the plugin manager of the view.
     *
     * @param pluginManager the plugin manager
     */
    private void setPluginManager(GUIPluginManager pluginManager) {
        if (this.pluginManager != null) {
            this.pluginManager.removePropertyChangeListener(this);
        }
        this.pluginManager = pluginManager;
        if (pluginManager != null) {
            pluginManager.addPropertyChangeListener(this);
        }
    }

    /**
     * Returns the view's model. Lyric view models may be shared by multiple
     * views.
     *
     * @return the model
     */
    public ViewModel getModel() {
        return model;
    }

    /**
     * Sets the configuration settings used by the view.
     *
     * @param config the configuration settings
     */
    public void setConfig(Config config) {
        if (this.config != null) {
            this.config.removePropertyChangeListener(this);
        }
        this.config = config;
        if (config != null) {
            config.addPropertyChangeListener(this);
        }
    }

    /**
     * Sets the display scale used by the view.
     *
     * @return the scale
     */
    public float getScale() {
        return scale;
    }

    /**
     * Returns whether the view is a preview.
     *
     * @return {@code true} if the view is a preview; {@code false} otherwise
     */
    public boolean isPreview() {
        return preview;
    }

    /**
     * Sets whether the view's scroll bar should be visible.
     *
     * @param scrollBarVisible whether the scroll bar should be visible
     */
    public void setScrollBarVisible(boolean scrollBarVisible) {
        if (scrollBarVisible) {
            scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        } else {
            scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        }
    }

    /**
     * Sets the background image to be shown in the view.
     *
     * @param backgroundImage the background image
     */
    public void setBackgroundImage(BufferedImage backgroundImage) {
        this.backgroundImage = backgroundImage;
        acceleratedBackground = null;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        Dimension oldSize = getSize();
        super.setBounds(x, y, width, height);

        if (oldSize.getWidth() != width || oldSize.getHeight() != height) {
            // calculate display scale
            scale = getWidth() / 1024.0f;

            // set document padding
            Dimension size = getSize();
            marginTop = calculateTopMargin(size);
            marginBottom = calculateBottomMargin(size);

            // set scroll bar increment
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setUnitIncrement((int) (20 * scale));

            layoutPlugins();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // render acceleratedBackground at new size
                    renderBackground();

                    // update styled document
                    StyledDocument document = createStyledDocument();
                    updateDocument(document);
                    setStyledDocument(document);
                }
            });
        }
    }

    private int calculateTopMargin(Dimension size) {
        int viewAreaHeight = (int) (size.getWidth() / model.getAspectRatio());
        int paddingHeight = (int) ((size.getHeight() - viewAreaHeight) / 2);
        if (paddingHeight < 0)
            paddingHeight = 0;
        return paddingHeight;
    }

    private int calculateBottomMargin(Dimension size) {
        int viewAreaHeight = (int) (size.getWidth() / model.getAspectRatio());
        int paddingHeight = (int) ((size.getHeight() + viewAreaHeight) / 2);
        return paddingHeight;
    }

    private BoundedRangeModel createRangeModel(ViewModel model) {
        Scroller scroller = model.getScroller();
        ConversionStrategy strategy;

        /*
         * FIXME: scroll unit configuration is currently overridden; choosing a
         * scroll unit may be obsolete by now.
         */
        // Config config = ConfigManager.getConfig();
        // switch (config.getScrollUnits()) {
        // case CHARACTERS:
        // strategy = new CharacterConversionStrategy(textPane);
        // break;
        // case LINES:
        // default:
        // strategy = new LineConversionStrategy(textPane);
        // }
        SynchronizationModel synchModel = model.getSynchronizationModel();
        strategy = new SynchronizationModelStrategy(synchModel, textPane);

        strategy.setTopMarginFixed(true);
        AdvancedTextRangeModel rangeModel = new AdvancedTextRangeModel(scroller, strategy);
        return rangeModel;
    }

    @Override
    public void setCursor(Cursor cursor) {
        super.setCursor(cursor);
        if (textPane != null)
            textPane.setCursor(cursor);
    }

    @Override
    public void addMouseListener(MouseListener listener) {
        super.addMouseListener(listener);
        if (textPane != null)
            textPane.addMouseListener(listener);
    }

    @Override
    public void paintChildren(Graphics g) {
        super.paintChildren(g);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        int viewAreaHeight = (int) (getWidth() / model.getAspectRatio());
        int viewAreaTop = (getHeight() - viewAreaHeight) / 2;

        if (model.isBackgroundVisible()) {
            // paint background color
            g.setColor(getBackground());
            g.fillRect(0, viewAreaTop, getWidth(), viewAreaHeight);

            if (isPreview() || viewAreaTop > 0) {
                // paint darker background for non-visible areas
                g.setColor(getBackground().darker());
                g.fillRect(0, 0, getWidth(), viewAreaTop);
                g.fillRect(0, viewAreaTop + viewAreaHeight, getWidth(), getHeight());

                // draw edges of visible area
                g.setColor(getBackground().brighter());
                g.drawLine(0, viewAreaTop - 1, getWidth(), viewAreaTop - 1);
                g.drawLine(0, viewAreaTop + viewAreaHeight, getWidth(), viewAreaTop
                        + viewAreaHeight);
            }

            if (backgroundImage != null && (!preview || config.isBackgroundVisibleInPreview())) {
                if (acceleratedBackground == null) {
                    renderBackground();
                }

                g.drawImage(acceleratedBackground, 0, viewAreaTop, this);
            }
        } else {
            // black background
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            // show visible area in preview
            if (isPreview()) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, viewAreaTop, getWidth(), viewAreaHeight);
            }
        }

        if (showFramesPerSecond) {
            counter.countFrame();
            g.setColor(Color.WHITE);
            g.drawString("FPS: " + counter.getFramesPerSecond(), 10, 50);
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName() == ViewModel.CONTENT_PROPERTY) {
            updateDocument();

        } else if (e.getPropertyName() == ViewModel.CONTENT_VISIBLE_PROPERTY) {
            textPane.setVisible(model.isContentVisible() || isPreview());
            setBaseStyleForeground();
            repaint();

        } else if (e.getPropertyName() == ViewModel.BACKGROUND_VISIBLE_PROPERTY) {
            setBaseStyleForeground();
            repaint();

        } else if (e.getPropertyName() == ViewModel.SCROLLER_PROPERTY) {
            scrollPane.getVerticalScrollBar().setModel(createRangeModel(getModel()));

        } else if (e.getPropertyName() == Config.BACKGROUND_COLOR_PROPERTY
                || e.getPropertyName() == Config.FOREGROUND_COLOR_PROPERTY
                || e.getPropertyName() == Config.BACKGROUND_VISIBLE_IN_PREVIEW_PROPERTY) {
            setBackground(config.getBackgroundColor());
            repaint();

        } else if (e.getPropertyName() == Config.BACKGROUND_IMAGE_PROPERTY) {
            loadBackgroundImage();
            repaint();

        } else if (e.getPropertyName() == Config.SCROLLBAR_VISIBLE_PROPERTY) {
            setScrollBarVisible(config.isScrollBarVisible());

        } else if (e.getPropertyName() == Config.FULL_SCREEN_MARGINS_PROPERTY) {
            StyledDocument document = createStyledDocument();
            updateDocument(document);
            setStyledDocument(document);

        } else if (e.getPropertyName() == ConfigManager.CONFIG_PROPERTY) {
            setConfig(ConfigManager.getConfig());
            loadBackgroundImage();
            setBackground(config.getBackgroundColor());
            setScrollBarVisible(config.isScrollBarVisible());

            StyledDocument document = createStyledDocument();
            updateDocument(document);
            setStyledDocument(document);

            repaint();

        } else if (e.getPropertyName() == GUIPluginManager.PLUGINS_PROPERTY) {
            layoutPlugins();
        }
    }

    private void setStyledDocument(StyledDocument document) {
        this.document = document;
        setBaseStyleForeground();
        textPane.setDocument(document);
    }

    private StyledDocument createStyledDocument() {
        Rectangle2D margins = config.getFullScreenMargins();

        StyledDocument document = new DefaultStyledDocument();

        int marginLeft = (int) (getWidth() * margins.getX());
        int marginWidth = (int) (getWidth() * margins.getWidth());
        int marginRight = getWidth() - (marginLeft + marginWidth);
        TabSet tabSet = new TabSet(new TabStop[] { new TabStop(marginWidth * 0.1f),
                new TabStop(marginWidth * 0.45f), new TabStop(marginWidth * 0.8f) });

        Config config = ConfigManager.getConfig();

        Style baseStyle = document.addStyle("base", null);
        StyleConstants.setFontFamily(baseStyle, BASE_FONT_FAMILY);
        StyleConstants.setForeground(baseStyle, config.getForegroundColor());
        StyleConstants.setLeftIndent(baseStyle, marginLeft);
        StyleConstants.setRightIndent(baseStyle, marginRight);
        StyleConstants.setSpaceAbove(baseStyle, 0);
        StyleConstants.setSpaceBelow(baseStyle, 0);
        StyleConstants.setTabSet(baseStyle, tabSet);

        Style marginTopStyle = document.addStyle("marginTop", null);
        StyleConstants.setSpaceAbove(marginTopStyle, marginTop);

        Style marginBottomStyle = document.addStyle("marginBottom", null);
        StyleConstants.setSpaceBelow(marginBottomStyle, marginBottom);

        Style titleStyle = document.addStyle(TITLE, baseStyle);
        StyleConstants.setBold(titleStyle, true);
        StyleConstants.setItalic(titleStyle, true);
        StyleConstants.setFontSize(titleStyle, (int) (NORMAL_SIZE * scale));

        Style normalStyle = document.addStyle(NORMAL, baseStyle);
        StyleConstants.setBold(normalStyle, true);
        StyleConstants.setFontSize(normalStyle, (int) (NORMAL_SIZE * scale));

        Style smallStyle = document.addStyle(SMALL, baseStyle);
        StyleConstants.setFontSize(smallStyle, (int) (SMALL_SIZE * scale));

        Style smallestStyle = document.addStyle(SMALLEST, baseStyle);
        StyleConstants.setItalic(smallestStyle, true);
        StyleConstants.setFontSize(smallestStyle, (int) (SMALLEST_SIZE * scale));

        document.setParagraphAttributes(0, document.getLength(), baseStyle, false);

        return document;
    }

    private void setBaseStyleForeground() {
        Style baseStyle = document.getStyle("base");
        Config config = ConfigManager.getConfig();
        Color foreground = config.getForegroundColor();
        Color background = model.isBackgroundVisible() ? config.getBackgroundColor()
                : Color.BLACK;
        if (!isPreview() || model.isContentVisible()) {
            // 100% white
            StyleConstants.setForeground(baseStyle, foreground);
        } else {
            // 3/8 white
            int red = (3 * foreground.getRed() + 5 * background.getRed()) / 8;
            int green = (3 * foreground.getGreen() + 5 * background.getGreen()) / 8;
            int blue = (3 * foreground.getBlue() + 5 * background.getBlue()) / 8;
            Color color = new Color(red, green, blue);
            StyleConstants.setForeground(baseStyle, color);
        }
    }

    private void updateDocument() {
        updateDocument(document);
    }

    private void updateDocument(final StyledDocument document) {
        try {
            document.remove(0, document.getLength());
            document.setParagraphAttributes(0, document.getLength(),
                    document.getStyle("base"), false);
        } catch (BadLocationException e) {
            assert false : "This won't happen. Really!";
            e.printStackTrace();
        }

        Object content = model.getContent();
        if (content instanceof Lyric) {
            Lyric lyric = (Lyric) content;
            updateLyricDocument(document, lyric);

        } else if (content instanceof LyricRef) {
            LyricRef ref = (LyricRef) content;
            Catalog catalog = CatalogManager.getCatalog();
            updateLyricDocument(document, catalog.getLyric(ref));

        } else if (content instanceof BibleRef) {
            BibleRef ref = (BibleRef) content;
            ArchiveLibrary bible = null;
            try {
                final List<URL> collectionURLs = config.getCollectionURLs();
                if (!collectionURLs.isEmpty()) {
                    final URL url = collectionURLs.get(0);
                    try {
                        bible = new ArchiveLibrary(url.toURI());
                    } catch (URISyntaxException e) {
                        throw new IOException(e);
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new AssertionError(e);
            }
            updateBibleDocument(document, ref, bible);

        } else if (content != null) {
            updateObjectDocument(document, content);
        }

        // top/bottom margins
        try {
            document.insertString(document.getLength(), "\n\n", document.getStyle(NORMAL));
        } catch (BadLocationException e) {
            throw new AssertionError(e);
        }
        document.setParagraphAttributes(0, 0, document.getStyle("marginTop"), false);
        document.setParagraphAttributes(document.getLength(), 0, document
                .getStyle("marginBottom"), false);
    }

    private void updateBibleDocument(StyledDocument document, BibleRef ref,
            ArchiveLibrary bible) {
        try {
            document.insertString(document.getLength(), bible.getName() + "\n", document
                    .getStyle(SMALL));
            document.insertString(document.getLength(), ref.toString() + "\n", document
                    .getStyle(TITLE));

            Book book = bible.getBook(ref.getBookName());
            if (book == null) {
                document.insertString(document.getLength(), "\n\n(niet beschikbaar)", null);
            } else {
                Integer startChapter = ref.getStartChapter();
                Integer endChapter = ref.getEndChapter();
                if (endChapter == null) {
                    endChapter = startChapter;
                }

                for (int i = startChapter; i <= endChapter; i++) {
                    Chapter chapter = book.getChapter(i);
                    if (chapter == null)
                        break;

                    if (i > startChapter) {
                        document.insertString(document.getLength(), "\n\n", document
                                .getStyle(TITLE));
                        document.insertString(document.getLength(), ref.getBookName() + " "
                                + chapter.getNumber() + "\n\n", document.getStyle(TITLE));
                    }

                    final Collection<Verse> verses;

                    Integer startVerse = ref.getStartVerse();
                    Integer endVerse = ref.getEndVerse();
                    if (startVerse == null) {
                        verses = chapter.getVerses();
                    } else if (endVerse == null) {
                        verses = new HashSet<Verse>();
                        verses.add(chapter.getVerse(startVerse));
                    } else {
                        verses = new TreeSet<Verse>();
                        for (Verse verse : chapter.getVerses()) {
                            boolean inside = true;
                            if (i == startChapter) {
                                if (verse.getRangeEnd() < startVerse) {
                                    inside = false;
                                }
                            }
                            if (i == endChapter) {
                                if (verse.getRangeStart() > endVerse) {
                                    inside = false;
                                }
                            }
                            if (inside) {
                                verses.add(verse);
                            }
                        }
                    }

                    for (Verse verse : verses) {
                        document.insertString(document.getLength(), "\n", document
                                .getStyle(SMALL));
                        document.insertString(document.getLength(), verse.getRange(), document
                                .getStyle(SMALL));
                        document.insertString(document.getLength(), " ", document
                                .getStyle(NORMAL));
                        document.insertString(document.getLength(), verse.getText(), document
                                .getStyle(NORMAL));
                        document.insertString(document.getLength(), " ", document
                                .getStyle(NORMAL));
                    }
                }
            }
        } catch (BadLocationException e) {
            throw new AssertionError(e);
        }
    }

    private void updateObjectDocument(StyledDocument document, Object content) {
        try {
            document.insertString(document.getLength(), content.toString(), document
                    .getStyle(NORMAL));
        } catch (BadLocationException e) {
            throw new AssertionError(e);
        }
    }

    private void updateLyricDocument(StyledDocument document, Lyric lyric) {
        Group bundle = CatalogManager.getCatalog().getBundle(lyric);

        String refString;
        if (bundle == null) {
            refString = Resources.get().getString("lyricView.lyricReference", "",
                    lyric.getNumber())
                    + "\n";
        } else {
            refString = Resources.get().getString("lyricView.lyricReference",
                    bundle.getName(), lyric.getNumber())
                    + "\n";
        }
        String titleString = Resources.get().getString("lyricView.lyricTitle",
                lyric.getTitle())
                + "\n\n";

        try {
            // title
            document.insertString(document.getLength(), refString, document.getStyle(SMALL));
            document.insertString(document.getLength(), titleString, document.getStyle(TITLE));

            // text
            String tabFixedText = lyric.getText();
            tabFixedText = tabFixedText.replaceAll("\t\t", "\t \t");
            // XXX: whitespace handling hack
            /* Spaces in between tabs fix merging of multiple tabs */
            document.insertString(document.getLength(), tabFixedText, document
                    .getStyle(NORMAL));
            document.insertString(document.getLength(), "\n\n", document.getStyle(NORMAL));

            // original title
            String originalTitle = lyric.getOriginalTitle();
            if (originalTitle != null) {
                String originalTitleString = Resources.get().getString(
                        "lyricView.originalTitle", originalTitle);
                document.insertString(document.getLength(), originalTitleString, document
                        .getStyle(SMALLEST));
                document.insertString(document.getLength(), "\n", document.getStyle(SMALLEST));
            }

            // copyrights
            document.insertString(document.getLength(), lyric.getCopyrights(), document
                    .getStyle(SMALLEST));
            document.insertString(document.getLength(), "\n", document.getStyle(SMALLEST));

        } catch (BadLocationException e) {
            assert false : "This won't happen. Really!";
            e.printStackTrace();
        }
    }

    private void init() {
        setConfig(ConfigManager.getConfig());
	    ConfigManager configManager = ConfigManager.getInstance();
        configManager.addPropertyChangeListener(this);

        createComponents();
        loadBackgroundImage();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        // toggle autoscrolling
                        model.setAutoScrollingEnabled(!model.isAutoScrollingEnabled());
                    }
                }
            }
        });

        setStyledDocument(createStyledDocument());
        setPreferredSize(new Dimension(400, 300));

        // listen to config changes
        ConfigManager.getConfig().addPropertyChangeListener(this);
    }

    private void createComponents() {
        // set default background
        setBackground(config.getBackgroundColor());

        // text area
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setHighlighter(null);
        // textPane.setAutoscrolls(false);

        // remove text area caret
        textPane.setCaret(null);
        for (MouseListener listener : textPane.getMouseListeners()) {
            textPane.removeMouseListener(listener);
        }

        // scrollpane
        scrollPane = new JScrollPane(textPane, VERTICAL_SCROLLBAR_ALWAYS,
                HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setUI(new LyricViewScrollPaneUI());
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        // scrollPane.setAutoscrolls(false);

        // scrolling based on LyricViewModel
        scrollPane.getVerticalScrollBar().setModel(createRangeModel(getModel()));

        // set scrollbar visibility
        setScrollBarVisible(config.isScrollBarVisible());

        pluginsPanel = new JPanel();
        pluginsPanel.setLayout(new BoxLayout(pluginsPanel, BoxLayout.PAGE_AXIS));
        pluginsPanel.setOpaque(false);
        pluginsPanel.setAlignmentX(CENTER_ALIGNMENT);

        // setLayout(new BorderLayout());
        setLayout(new OverlayLayout(this));
        add(scrollPane); // , BorderLayout.CENTER
        add(pluginsPanel, PALETTE_LAYER);
    }

    /**
     * Sets the positions of any GUI plugins.
     */
    private void layoutPlugins() {
        pluginsPanel.removeAll();
        pluginsPanel.setFont(getFont().deriveFont(NORMAL_SIZE * getScale()));
        pluginsPanel.add(Box.createRigidArea(new Dimension(0, marginTop)));
        for (GUIPlugin plugin : pluginManager.getPlugins()) {
            GUIPlugin.SwingRenderer renderer = plugin.getSwingRenderer();
            JComponent component = renderer.getComponent();
            pluginsPanel.add(component);
        }
        pluginsPanel.add(Box.createVerticalGlue());
        pluginsPanel.revalidate();
    }

    /**
     * Sets the visibility of the cursor.
     *
     * @param cursorVisible whether the cursor should be visible
     */
    public void setCursorVisible(boolean cursorVisible) {
        if (cursorVisible) {
            setCursor(Cursor.getDefaultCursor());
        } else {
            Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),
                    "transparant");
            setCursor(transparentCursor);
        }
    }

    private void loadBackgroundImage() {
        if (!preview || config.isBackgroundVisibleInPreview()) {
            if (config.getBackgroundImage() == null) {
                backgroundImageURL = null;
                setBackgroundImage(null);
            } else {
                if (!config.getBackgroundImage().equals(backgroundImageURL)) {
                    backgroundImageURL = config.getBackgroundImage();
                    try {
                        setBackgroundImage(ImageIO.read(backgroundImageURL));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void renderBackground() {
        renderBackground(getWidth(), (int) (getWidth() / model.getAspectRatio()));
    }

    private void renderBackground(int width, int height) {
        // nobody will care if it's not visible
        if (!model.isBackgroundVisible()) {
            return;
        }

        acceleratedBackground = createImage(width, height);
        Graphics2D g = (Graphics2D) acceleratedBackground.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.drawImage(backgroundImage, 0, 0, width, height, null);
        g.dispose();
    }
}
