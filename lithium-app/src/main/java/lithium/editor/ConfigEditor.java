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

package lithium.editor;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.GroupLayout.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.github.meinders.common.*;
import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.Announcement.*;
import lithium.catalog.Group;
import lithium.display.*;
import lithium.display.java2d.*;
import lithium.gui.*;
import lithium.imagebrowser.*;

import static javax.swing.Action.*;

/**
 * An editor to change configuration settings.
 *
 * @since 0.6
 * @author Gerrit Meinders
 */
public class ConfigEditor
extends BasicConfigEditor
implements WindowFocusListener
{

	/** Allows the user to add a catalog to the default catalogs. */
	private Action addCatalogAction;

	/** Removes the selected catalogs from the default catalogs. */
	private Action removeCatalogAction;

	/** Allows the user to add a collection to the default collections. */
	private Action addCollectionAction;

	/** Removes the select collections from the default collections. */
	private Action removeCollectionAction;

	/** Creates a new announcement preset. */
	private Action newAnnouncementPresetAction;

	/** Deletes the selected announcement preset. */
	private Action deleteAnnouncementPresetAction;

	/** Creates a new announcement parameter. */
	private Action newAnnouncementParameterAction;

	/** Deletes the selected announcement parameter. */
	private Action deleteAnnouncementParameterAction;

	/** Allows the user to edit the selected announcement parameter. */
	private Action editAnnouncementParameterAction;

	/** The list of default catalogs. */
	private JList catalogList;

	/** The list of default collections. */
	private JList collectionList;

	/** A preview component of certain display settings. */
	private LyricView preview;

	/** A slider for controlling the left margin of displayed text. */
	private JSlider marginLeftSlider;

	/** A slider for controlling the width of displayed text. */
	private JSlider marginWidthSlider;

	/** A slider for controlling the top margin of displayed text. */
	private JSlider marginTopSlider;

	/** A slider for controlling the height of displayed text. */
	private JSlider marginHeightSlider;

	/** The list of announcement presets. */
	private JList announcementList;

	/** The model used by the announcement parameter table. */
	private AnnouncementParameterTableModel parameterModel;

	/** The table listing the parameters of the selected announcement. */
	private JTable parameterTable;

	/**
	 * Constructs a new configuration editor for the given configuration.
	 *
	 * @param parent the parent window
	 * @param config the configuration
	 */
	public ConfigEditor(Window parent, Config config)
	{
		super( parent, config );
	}

	public void windowGainedFocus(WindowEvent e)
	{
		preview.getModel().getAutoScroller().setEnabled(true);
	}

	public void windowLostFocus(WindowEvent e)
	{
		preview.getModel().getAutoScroller().setEnabled(false);
	}

	private File selectCatalog()
	{
		JFileChooser fileChooser = FileChoosers.createFileChooser();
		fileChooser.setCurrentDirectory(ConfigManager.getLyricsFolder());
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(FilterManager.getCombinedFilter( FilterType.CATALOG));

		int option = fileChooser.showOpenDialog(this);
		if (option == JFileChooser.APPROVE_OPTION)
		{
			return fileChooser.getSelectedFile();
		}
		else
		{
			return null;
		}
	}

	private File selectCollection()
	{
		JFileChooser fileChooser = FileChoosers.createFileChooser();
		fileChooser.setCurrentDirectory(ConfigManager.getBooksFolder());
		// {
		// @Override
		// public void approveSelection()
		// {
		// final File selection = getSelectedFile();
		// if (selection.exists())
		// {
		// super.approveSelection();
		// }
		// else
		// {
		// JOptionPane.showMessageDialog(this, "File not found n00b!",
		// "File not found", JOptionPane.WARNING_MESSAGE);
		// }
		// }
		// };
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(FilterManager.getCombinedFilter( FilterType.COLLECTION));

		int option = fileChooser.showOpenDialog(this);
		if (option == JFileChooser.APPROVE_OPTION)
		{
			return fileChooser.getSelectedFile();
		}
		else
		{
			return null;
		}
	}

	private void newAnnouncementPreset()
	{
		String newName = Resources.get().getString("announcements.newName");
		String newText = Resources.get().getString("announcements.newText");
		String message = Resources.get().getString(
		        "announcements.nameOfNewAnnouncement");
		String title = Resources.get().getString("add");
		Object input = JOptionPane.showInputDialog(ConfigEditor.this, message,
		        title, JOptionPane.QUESTION_MESSAGE, null, null, newName);
		if (input != null)
		{
			newName = input.toString();
			Set<Announcement> announcements = new LinkedHashSet<Announcement>(
			        config.getAnnouncementPresets());
			announcements.add(new Announcement(newName, newText));
			config.setAnnouncementPresets(announcements);
		}
	}

	private void deleteAnnouncementPreset(Announcement announcement)
	{
		String title = Resources.get().getString("remove");
		String message = Resources.get().getString(
		        "announcements.confirmDelete");
		int choice = JOptionPane.showOptionDialog(this, message, title,
		        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
		        null, null, null);
		if (choice == JOptionPane.YES_OPTION)
		{
			Set<Announcement> announcements = new LinkedHashSet<Announcement>(
			        config.getAnnouncementPresets());
			announcements.remove(announcement);
			config.setAnnouncementPresets(announcements);
		}
	}

	private void newAnnouncementParameter(Announcement announcement)
	{
		ParameterTypeDialog typeDialog = new ParameterTypeDialog(this);
		typeDialog.setVisible(true);
		if (!typeDialog.isCancelled())
		{
			Class<?> type = typeDialog.getSelectedValue();

			String title = Resources.get().getString("announcements.selectTag");
			String message = Resources.get().getString(
			        "announcements.selectTagLong");
			Object result = JOptionPane.showInputDialog(this, message, title,
			        JOptionPane.QUESTION_MESSAGE, null, null, null);
			if (result != null)
			{
				String tag = result.toString();
				if (tag.charAt(0) != '$')
				{
					tag = "$" + tag;
				}

				if (type == DateParameter.class)
				{
					newAnnouncementDateParameter(announcement, tag);
				}
				else if (type == TextParameter.class)
				{
					newAnnouncementTextParameter(announcement, tag);
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Unexpected type: "
					        + type);
				}
			}
		}
	}

	private void newAnnouncementDateParameter(Announcement announcement,
	        String tag)
	{
		String[] patterns = { "d MMMM yyyy", "EEEE d MMMM yyyy", "dd-MM-yyyy",
		        "MM-dd-yyyy", "yyyy-MM-dd", "EEEE", "H:mm", "h:mm a" };
		String[] formattedDates = new String[patterns.length];
		Date now = new Date();
		for (int i = 0; i < patterns.length; i++)
		{
			SimpleDateFormat format = new SimpleDateFormat(patterns[i]);
			formattedDates[i] = format.format(now);
		}

		// select a date format
		String title = Resources.get().getString(
		        "announcements.selectDateFormat");
		String message = Resources.get().getString(
		        "announcements.selectDateFormatLong");
		Object selected = JOptionPane.showInputDialog(this, message, title,
		        JOptionPane.QUESTION_MESSAGE, null, formattedDates,
		        formattedDates[0]);

		if (selected != null)
		{
			// find pattern matching the selected date format
			String pattern = null;
			for (int i = 0; i < patterns.length; i++)
			{
				if (formattedDates[i] == selected)
				{
					pattern = patterns[i];
					break;
				}
			}

			announcement.addParameter(new DateParameter(tag,
			        new SimpleDateFormat(pattern), null));
		}
	}

	private void newAnnouncementTextParameter(Announcement announcement,
	        String tag)
	{
		String title = Resources.get().getString("announcements.enterLabel");
		String message = Resources.get().getString(
		        "announcements.enterLabelLong");
		String label = JOptionPane.showInputDialog(this, message, title,
		        JOptionPane.QUESTION_MESSAGE);
		announcement.addParameter(new TextParameter(tag, label));
	}

	private void editAnnouncementParameter(Announcement announcement,
	        Parameter parameter)
	{
		final Class<? extends Parameter> type = parameter.getClass();
		if (type == DateParameter.class)
		{
			editAnnouncementDateParameter(announcement, parameter);
		}
		else if (type == TextParameter.class)
		{
			editAnnouncementTextParameter(announcement, parameter);
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Unexpected type: " + type);
		}
	}

	private void editAnnouncementDateParameter(Announcement announcement,
	        Parameter parameter)
	{
		// TODO Auto-generated method stub
	}

	private void editAnnouncementTextParameter(Announcement announcement,
	        Parameter parameter)
	{
		// TODO Auto-generated method stub

	}

	private void deleteAnnouncementParameter(Announcement announcement,
	        Parameter parameter)
	{
		announcement.removeParameter(parameter);
	}

	@Override
	protected void init()
	{
		super.init();

		CatalogManager.loadDefaultCatalogs( null );
	}

	@Override
	protected void createActions()
	{
		super.createActions();

		addCatalogAction = new AbstractAction(Resources.get().getString(
		        "ConfigEditor.add"))
		{
			public void actionPerformed(ActionEvent e)
			{
				File catalog = selectCatalog();
				if (catalog != null)
				{
					try
					{
						getConfig().addCatalogURL(catalog.toURI().toURL());
					}
					catch (MalformedURLException ex)
					{
						ex.printStackTrace();
					}
				}
			}
		};
		addCatalogAction.putValue(MNEMONIC_KEY, Resources.get().getMnemonic(
		        "ConfigEditor.add"));

		removeCatalogAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				assert !catalogList.isSelectionEmpty();
				getConfig().removeCatalogURL(
				        (URL) catalogList.getSelectedValue());
			}
		};

		addCollectionAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				File collection = selectCollection();
				if (collection != null)
				{
					try
					{
						getConfig().addCollectionURL(collection.toURI().toURL());
					}
					catch (MalformedURLException ex)
					{
						ex.printStackTrace();
					}
				}
			}
		};

		removeCollectionAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				assert !collectionList.isSelectionEmpty();
				getConfig().removeCollectionURL(
				        (URL) collectionList.getSelectedValue());
			}
		};

		newAnnouncementPresetAction = new AbstractAction(
		        Resources.get().getString("add"))
		{
			public void actionPerformed(ActionEvent e)
			{
				newAnnouncementPreset();
			}
		};
		newAnnouncementPresetAction.putValue(MNEMONIC_KEY,
		        Resources.get().getMnemonic("add"));

		deleteAnnouncementPresetAction = new AbstractAction(
		        Resources.get().getString("remove"))
		{
			public void actionPerformed(ActionEvent e)
			{
				Announcement announcement = (Announcement) announcementList.getSelectedValue();
				if (announcement != null)
				{
					deleteAnnouncementPreset(announcement);
				}
			}
		};
		deleteAnnouncementPresetAction.setEnabled(false);
		deleteAnnouncementPresetAction.putValue(MNEMONIC_KEY,
		        Resources.get().getMnemonic("remove"));

		newAnnouncementParameterAction = new AbstractAction(
		        Resources.get().getString("add"))
		{
			public void actionPerformed(ActionEvent e)
			{
				Announcement announcement = (Announcement) announcementList.getSelectedValue();
				if (announcement != null)
				{
					newAnnouncementParameter(announcement);
				}
			}
		};
		newAnnouncementParameterAction.putValue(MNEMONIC_KEY,
		        Resources.get().getMnemonic("add"));

		deleteAnnouncementParameterAction = new AbstractAction(
		        Resources.get().getString("remove"))
		{
			public void actionPerformed(ActionEvent e)
			{
				Announcement announcement = (Announcement) announcementList.getSelectedValue();
				int row = parameterTable.getSelectedRow();
				Parameter parameter = parameterModel.getParameter(row);
				deleteAnnouncementParameter(announcement, parameter);
			}
		};
		deleteAnnouncementParameterAction.putValue(MNEMONIC_KEY,
		        Resources.get().getMnemonic("remove"));

		editAnnouncementParameterAction = new AbstractAction(
		        Resources.get().getString("edit"))
		{
			public void actionPerformed(ActionEvent e)
			{
				Announcement announcement = (Announcement) announcementList.getSelectedValue();
				int row = parameterTable.getSelectedRow();
				Parameter parameter = parameterModel.getParameter(row);
				editAnnouncementParameter(announcement, parameter);
			}
		};
		editAnnouncementParameterAction.putValue(MNEMONIC_KEY,
		        Resources.get().getMnemonic("edit"));
	}

	@Override
	protected void createTabs( final JTabbedPane tabbedPane )
	{
		super.createTabs( tabbedPane );

		final ResourceUtilities res = Resources.get( "configEditor" );

		JPanel catalogTab = createTab(createCatalogTab());
		final JPanel appearanceTab = createTab(createAppearanceTab());
		JPanel visualStylesTab = createTab(createVisualStylesTab());
		JPanel displayTab = createTab(createDisplayTab());
		JPanel announcementsTab = createTab(createAnnouncementsTab());

		tabbedPane.add(res.getString("catalogs"), catalogTab);
		tabbedPane.add(res.getString("appearance"), appearanceTab);
		tabbedPane.add(res.getString("visuals"), visualStylesTab);
		tabbedPane.add(res.getString("display"), displayTab);
		tabbedPane.add(res.getString("announcements"), announcementsTab);

		tabbedPane.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				boolean previewVisible = tabbedPane.getSelectedComponent() == appearanceTab;
				preview.getModel().getAutoScroller().setEnabled(
				        previewVisible);
			}
		});
	}

	private JPanel createCatalogTab()
	{
		// create catalog list
		catalogList = new JList();
		catalogList.setListData(config.getCatalogURLs().toArray());
		catalogList.setVisibleRowCount(6);
		catalogList.setCellRenderer(new URLCellRenderer());

		// add listener to update catalog list
		config.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				catalogList.setListData(config.getCatalogURLs().toArray());
			}
		});

		ResourceUtilities res = Resources.get();

		EditableList catalogListEditor = new EditableList(catalogList, false);
		catalogListEditor.setAddAction(addCatalogAction);
		catalogListEditor.setRemoveAction(removeCatalogAction);
		catalogListEditor.setBorder(BorderFactory.createCompoundBorder(
		        BorderFactory.createTitledBorder(res.getString("ConfigEditor.defaultCatalogs")),
		        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		collectionList = new JList();
		collectionList.setListData(config.getCollectionURLs().toArray());
		collectionList.setVisibleRowCount(6);
		collectionList.setCellRenderer(new LibraryListCellRenderer());

		// add listener to update catalog list
		config.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				collectionList.setListData(config.getCollectionURLs().toArray());
			}
		});

		EditableList collectionListEditor = new EditableList(collectionList,
		        false);

		collectionListEditor.setAddAction(addCollectionAction);
		collectionListEditor.setRemoveAction(removeCollectionAction);
		collectionListEditor.setBorder(BorderFactory.createCompoundBorder(
		        BorderFactory.createTitledBorder(res.getString("books.defaultBooks")),
		        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		collectionListEditor.setEnabled(false);

		// layout components
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(catalogListEditor);
		panel.add(Box.createRigidArea(new Dimension(5, 5)));
		panel.add(createBundlePanel());
		panel.add(Box.createRigidArea(new Dimension(11, 11)));
		panel.add(collectionListEditor);
		panel.add(Box.createRigidArea(new Dimension(11, 11)));
		panel.add(createLoadOnStartupPanel());
		return panel;
	}

	private JPanel createBundlePanel()
	{
		// get a (sorted) list of unique bundle names
		TreeSet<String> bundleNames = new TreeSet<String>();
		for (Group bundle : CatalogManager.getCatalog().getBundles())
		{
			bundleNames.add(bundle.getName());
		}

		final JComboBox bundleCombo = new JComboBox(
		        bundleNames.toArray(new String[0]));
		bundleCombo.setEditable(true);
		bundleCombo.setSelectedItem(getConfig().getDefaultBundle());

		// add listener to update config
		bundleCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getConfig().setDefaultBundle(
				        (String) bundleCombo.getSelectedItem());
			}
		});

		/*
		 * // set fixed height combobox Dimension maximumSize =
		 * bundleCombo.getMaximumSize(); maximumSize.height =
		 * bundleCombo.getPreferredSize().height;
		 * bundleCombo.setMaximumSize(maximumSize);
		 */

		JLabel bundleLabel = new JLabel(Resources.get().getString("label",
		        Resources.get().getString("ConfigEditor.defaultBundle")));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(bundleLabel);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(bundleCombo);
		return panel;
	}

	private Component createLoadOnStartupPanel()
	{
		JLabel label = new JLabel(Resources.get().getString("label",
		        Resources.get().getString("config.loadOnStartupFolder")));

		final JTextField field = new JTextField();

		File loadOnStartupFolder = getConfig().getLoadOnStartupFolder();
		if (loadOnStartupFolder != null)
		{
			field.setText(loadOnStartupFolder.toString());
		}

		applyActions.add(new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				Config config = getConfig();
				String text = field.getText();
				if (text.isEmpty())
				{
					config.setLoadOnStartupFolder(null);
				}
				else
				{
					config.setLoadOnStartupFolder(new File(text));
				}
				return true;
			}
		});

		JComponent browsableField = createBrowsableFolderField(field);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(label);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(browsableField);
		return panel;
	}

	private JPanel createAppearanceTab()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new SpringLayout());
		panel.add(createFullScreenPanel());
		panel.add(createAutoScrollPanel());
		panel.add(createMarginsPanel());
		SpringUtilities.makeCompactGrid(panel, 3, 1, 0, 0, 5, 5);
		return panel;
	}

	private JPanel createFullScreenPanel()
	{
		final JCheckBox scrollBarCheck = new JCheckBox(
		        Resources.get().getString("ConfigEditor.scrollBarVisible"));
		scrollBarCheck.setSelected(getConfig().isScrollBarVisible());
		scrollBarCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getConfig().setScrollBarVisible(scrollBarCheck.isSelected());
			}
		});

		final JCheckBox dividerCheck = new JCheckBox(Resources.get().getString(
		        "ConfigEditor.dividerVisible"));
		dividerCheck.setSelected(getConfig().isDividerVisible());
		dividerCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getConfig().setDividerVisible(dividerCheck.isSelected());
			}
		});

		// layout components
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createCompoundBorder(
		        BorderFactory.createTitledBorder(Resources.get().getString(
		                "ConfigEditor.fullScreenMode")),
		        BorderFactory.createEmptyBorder(0, 0, 5, 0)));
		panel.add(scrollBarCheck);
		panel.add(dividerCheck);
		return panel;
	}

	private JPanel createAutoScrollPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(Resources.get().getString(
		        "ConfigEditor.autoScroll")));
		panel.add(createAutoScrollSlidersPanel());
		panel.add(Box.createHorizontalStrut(5));
		panel.add(createAutoScrollPreviewPanel());
		return panel;
	}

	private JPanel createAutoScrollSlidersPanel()
	{
		// convert speed to integer units
		int speed = scrollSpeedModelToView(getConfig().getAutoScrollSpeed());

		// create slider
		final JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 20, speed);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(2);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				if (!slider.getValueIsAdjusting())
				{
					float speed = scrollSpeedViewToModel(slider.getValue());
					getConfig().setAutoScrollSpeed(speed);

					// preview.getModel().getAutoScroller().setScrollRate(
					// getConfig().getAutoScrollSpeed());
					// preview.getModel().getScroller().setTarget(5);
					// preview.getModel().getScroller().setValue(5);
				}
			}
		});

		// create labels for delaySlider
		Hashtable<Integer, JLabel> sliderLabels = new Hashtable<Integer, JLabel>();
		for (int i = 0; i <= 20; i += 10)
		{
			String labelText = Resources.get().getString(
			        "ConfigEditor.speedTickLabel", i);
			sliderLabels.put(i, new JLabel(labelText));
		}
		slider.setLabelTable(sliderLabels);
		slider.setPaintLabels(true);

		// label
		JLabel sliderLabel = new JLabel(Resources.get().getString(
		        "ConfigEditor.speed"));

		// set alignments
		slider.setAlignmentX(CENTER_ALIGNMENT);
		sliderLabel.setAlignmentX(CENTER_ALIGNMENT);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		panel.add(sliderLabel);
		panel.add(slider);
		return panel;
	}

	/**
	 * Converts the given scroll rate value to an integer used by the view,
	 * ranging from 0 (slow) to 10 (normal) to 20 (fast).
	 *
	 * @see #scrollSpeedViewToModel
	 */
	private int scrollSpeedModelToView(float model)
	{
		int view;
		if (model < 0.25)
		{
			view = (int) (10 * (model - 0.1) / 0.15);
		}
		else
		{
			view = (int) (10 + 10 * (model - 0.25) / 0.75);
		}
		view = view < 0 ? 0 : view;
		view = view > 20 ? 20 : view;
		return view;
	}

	/**
	 * Converts the given view units back to a scroll rate value.
	 *
	 * @see #scrollSpeedModelToView
	 */
	private float scrollSpeedViewToModel(int view)
	{
		if (view < 10)
		{
			return 0.15f * (view) / 10f + 0.1f;
		}
		else
		{
			return 0.75f * (view - 10) / 10f + 0.25f;
		}
	}

	private JPanel createAutoScrollPreviewPanel()
	{
		JLabel label = new JLabel(Resources.get().getString(
		        "ConfigEditor.preview"));

		ViewModel model = new ViewModel(true);
		preview = new LyricView(model, true);
		preview.setConfig(getConfig());
		preview.getModel().setContent(createPreviewText());
		preview.setPreferredSize(new Dimension(300, 100));
		preview.setMaximumSize(new Dimension(300, 100));
		preview.setBorder(BorderFactory.createLoweredBevelBorder());
		preview.setEnabled(false);
		preview.getModel().getScroller().setTarget(5);
		preview.getModel().getScroller().setValue(5);
		preview.getModel().getAutoScroller().setEnabled(true);

		addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				preview.setConfig(getConfig());
			}
		});

		// clean up scrollers
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				// preview.dispose();
			}
		});

		label.setAlignmentX(CENTER_ALIGNMENT);
		preview.setAlignmentX(CENTER_ALIGNMENT);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		panel.add(label);
		panel.add(Box.createVerticalStrut(5));
		panel.add(preview);
		return panel;
	}

	private JPanel createMarginsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(Resources.get().getString(
		        "ConfigEditor.margins")));
		panel.add(createMarginSlidersPanel());
		panel.add(Box.createHorizontalStrut(5));
		panel.add(createMarginsPreviewPanel());
		return panel;
	}

	private JPanel createMarginSlidersPanel()
	{
		// get margins and enforce minimum/maximum margins
		Rectangle2D margins = getConfig().getFullScreenMargins();

		// left-margin slider
		marginLeftSlider = createMarginSlider(Config.MINIMUM_MARGIN,
		        Config.MAXIMUM_MARGIN, margins.getX(), 0.1);
		marginLeftSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				getConfig().setFullScreenMargins(getFullScreenMargins());
			}
		});
		marginLeftSlider.setAlignmentX(CENTER_ALIGNMENT);

		// left-margin slider label
		JLabel marginLeftLabel = new JLabel(Resources.get().getString(
		        "ConfigEditor.marginLeft"));
		marginLeftLabel.setAlignmentX(CENTER_ALIGNMENT);

		// margin-width slider
		marginWidthSlider = createMarginSlider(1.0 - 2 * Config.MAXIMUM_MARGIN,
		        1.0 - 2 * Config.MINIMUM_MARGIN, margins.getWidth(), 0.2);
		marginWidthSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				getConfig().setFullScreenMargins(getFullScreenMargins());
			}
		});
		marginWidthSlider.setAlignmentX(CENTER_ALIGNMENT);

		// margin-width slider label
		JLabel marginWidthLabel = new JLabel(Resources.get().getString(
		        "ConfigEditor.marginWidth"));
		marginWidthLabel.setAlignmentX(CENTER_ALIGNMENT);

		// top-margin slider
		marginTopSlider = createMarginSlider(Config.MINIMUM_MARGIN,
		        Config.MAXIMUM_MARGIN, margins.getX(), 0.1);
		marginTopSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				getConfig().setFullScreenMargins(getFullScreenMargins());
			}
		});
		marginTopSlider.setAlignmentX(CENTER_ALIGNMENT);

		// top-margin slider label
		JLabel marginTopLabel = new JLabel(Resources.get().getString(
		        "ConfigEditor.marginTop"));
		marginTopLabel.setAlignmentX(CENTER_ALIGNMENT);

		// margin-height slider
		marginHeightSlider = createMarginSlider(
		        1.0 - 2 * Config.MAXIMUM_MARGIN,
		        1.0 - 2 * Config.MINIMUM_MARGIN, margins.getHeight(), 0.2);
		marginHeightSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				getConfig().setFullScreenMargins(getFullScreenMargins());
			}
		});
		marginHeightSlider.setAlignmentX(CENTER_ALIGNMENT);

		// margin-height slider label
		JLabel marginHeightLabel = new JLabel(Resources.get().getString(
		        "ConfigEditor.marginHeight"));
		marginWidthLabel.setAlignmentX(CENTER_ALIGNMENT);

		// layout in a panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		panel.add(marginLeftLabel);
		panel.add(marginLeftSlider);
		panel.add(marginWidthLabel);
		panel.add(marginWidthSlider);
		panel.add(marginTopLabel);
		panel.add(marginTopSlider);
		panel.add(marginHeightLabel);
		panel.add(marginHeightSlider);
		return panel;
	}

	private Rectangle2D getFullScreenMargins()
	{
		double left = marginLeftSlider.getValue() / 100.0;
		double width = marginWidthSlider.getValue() / 100.0;
		double top = marginTopSlider.getValue() / 100.0;
		double height = marginHeightSlider.getValue() / 100.0;
		double bottom = 1.0 - top - height;
		return new Rectangle2D.Double(left, bottom, width, height);
	}

	private JSlider createMarginSlider(double from, double to, double initial,
	        double labelSpacing)
	{
		int fromInt = (int) (from * 100);
		int toInt = (int) (to * 100);
		int initialInt = (int) (initial * 100);
		int labelSpacingInt = (int) (labelSpacing * 100);

		JSlider slider = new JSlider(JSlider.HORIZONTAL, fromInt, toInt,
		        initialInt);
		slider.setMinorTickSpacing(labelSpacingInt / 10);
		slider.setMajorTickSpacing(labelSpacingInt);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);

		// create labels for margin slider
		Hashtable<Integer, JLabel> sliderLabels = new Hashtable<Integer, JLabel>();
		for (int i = fromInt; i <= toInt; i += labelSpacingInt)
		{
			String labelText = Resources.get().getString(
			        "ConfigEditor.marginsTickLabel", i / 100.0f);
			sliderLabels.put(i, new JLabel(labelText));
		}
		slider.setLabelTable(sliderLabels);
		slider.setPaintLabels(true);

		return slider;
	}

	private JPanel createMarginsPreviewPanel()
	{
		JLabel label = new JLabel(Resources.get().getString(
		        "ConfigEditor.preview"));

		MarginsPreview preview = new MarginsPreview();
		preview.setPreferredSize(new Dimension(200, 150));
		preview.setMaximumSize(new Dimension(200, 150));
		preview.setBorder(BorderFactory.createLoweredBevelBorder());
		addPropertyChangeListener(preview);

		label.setAlignmentX(CENTER_ALIGNMENT);
		preview.setAlignmentX(CENTER_ALIGNMENT);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		panel.add(label);
		panel.add(Box.createVerticalStrut(5));
		panel.add(preview);
		return panel;
	}

	private JPanel createVisualStylesTab()
	{
		// create background images list
		int thumbnailWidth = 120;
		int thumbnailHeight = 90;
		ThumbnailLoader loader = new ThumbnailLoader(thumbnailWidth,
		        thumbnailHeight);
		final ThumbnailListModel model = new ThumbnailListModel(loader);
		final JIconList<URL> list = new JIconList<URL>(model);
		list.setTextPosition(SwingConstants.BOTTOM);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setMinimumSize(new Dimension(0, 0));
		listScroller.setPreferredSize(new Dimension(400, 300));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				finishBackgroundImagesList(model, list);
			}
		});

		// create a descriptive label
		JLabel infoLabel = createMultiLineLabel(Resources.get().getString(
		        "ConfigEditor.backgroundInfoLabel"), 3);

		// create checkbox for backgroundVisibleInPreview property
		final JCheckBox showInPreviewCheck = new JCheckBox(
		        Resources.get().getString(
		                "ConfigEditor.showBackgroundInPreview"));
		showInPreviewCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getConfig().setBackgroundVisibleInPreview(
				        showInPreviewCheck.isSelected());
			}
		});
		showInPreviewCheck.setSelected(getConfig().isBackgroundVisibleInPreview());

		// create foreground color combo
		final JColorComboBox colorCombo = createColorCombo(getConfig().getForegroundColor());
		colorCombo.setColorChooserTitle(Resources.get().getString(
		        "ConfigEditor.foregroundColorChooser"));

		// create background color combo
		final JColorComboBox backColorCombo = createColorCombo(getConfig().getBackgroundColor());
		backColorCombo.setColorChooserTitle(Resources.get().getString(
		        "ConfigEditor.backgroundColorChooser"));

		// listen to color selection changes
		colorCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getConfig().setForegroundColor(
				        (Color) colorCombo.getSelectedItem());
			}
		});
		backColorCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getConfig().setBackgroundColor(
				        (Color) backColorCombo.getSelectedItem());
			}
		});

		// Font choosers...
		final FontChooser defaultFontChooser = new FontChooser();
		final FontChooser titleFontChooser = new FontChooser();

		defaultFontChooser.setSelectedFont(config.getFont(Config.TextKind.DEFAULT));
		titleFontChooser.setSelectedFont(config.getFont(Config.TextKind.TITLE));

		/*
		 * Layout components.
		 */
		JPanel panel = new JPanel();

		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		panel.setLayout(layout);

		// Single column vertical layout.
		ParallelGroup singleColumn = layout.createParallelGroup();
		SequentialGroup rows = layout.createSequentialGroup();

		layout.setHorizontalGroup(singleColumn);
		layout.setVerticalGroup(rows);

		// Label/value pairs in two columns.
		SequentialGroup columns = layout.createSequentialGroup();
		singleColumn.addGroup(columns);

		ParallelGroup labels = layout.createParallelGroup();
		ParallelGroup values = layout.createParallelGroup();

		columns.addGroup(labels);
		columns.addGroup(values);

		// First some components in single column layout.
		singleColumn.addComponent(infoLabel);
		rows.addComponent(infoLabel);

		singleColumn.addComponent(listScroller);
		rows.addComponent(listScroller);

		singleColumn.addComponent(showInPreviewCheck);
		rows.addComponent(showInPreviewCheck);

		// Several rows of font and color choosers in two column layout.
		{
			ParallelGroup row = layout.createBaselineGroup(true, true);
			rows.addGroup(row);

			JLabel label = new JLabel(Resources.get().getString("label",
			        Resources.get().getString("ConfigEditor.foregroundColor")));
			row.addComponent(label);
			labels.addComponent(label);

			row.addComponent(colorCombo);
			values.addComponent(colorCombo);
		}

		{
			ParallelGroup row = layout.createBaselineGroup(true, true);
			rows.addGroup(row);

			JLabel label = new JLabel(Resources.get().getString("label",
			        Resources.get().getString("ConfigEditor.backgroundColor")));
			row.addComponent(label);
			labels.addComponent(label);

			row.addComponent(backColorCombo);
			values.addComponent(backColorCombo);
		}

		{
			ParallelGroup row = layout.createBaselineGroup(true, true);
			rows.addGroup(row);

			JLabel label = new JLabel(Resources.get().getString("label",
			        Resources.get().getString("config.visuals.fonts.default")));
			row.addComponent(label);
			labels.addComponent(label);

			row.addComponent(defaultFontChooser);
			values.addComponent(defaultFontChooser);
		}

		{
			ParallelGroup row = layout.createBaselineGroup(true, true);
			rows.addGroup(row);

			JLabel label = new JLabel(Resources.get().getString("label",
			        Resources.get().getString("config.visuals.fonts.title")));
			row.addComponent(label);
			labels.addComponent(label);

			row.addComponent(titleFontChooser);
			values.addComponent(titleFontChooser);
		}

		applyActions.add(new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				Config config = getConfig();
				config.setFont(Config.TextKind.DEFAULT,
				        defaultFontChooser.getSelectedFont());
				config.setFont(Config.TextKind.TITLE,
				        titleFontChooser.getSelectedFont());
				return true;
			}
		});

		return panel;
	}

	private void finishBackgroundImagesList(ThumbnailListModel model,
	        final JIconList<URL> list)
	{
		// add "no background" item
		model.add(null, Resources.get().getString("ConfigEditor.noBackground"));

		// add current background
		URL currentBackground = getConfig().getBackgroundImage();
		if (currentBackground != null)
		{
			String fileName;
			try
			{
				fileName = currentBackground.toURI().getPath();
			}
			catch (URISyntaxException e)
			{
				fileName = currentBackground.getPath();
			}
			fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
			model.add(currentBackground, fileName);
		}

		// add other background images
		File folder = getConfig().getBackgroundsFolder();
		ExtensionFileFilter imageFilter = new ExtensionFileFilter("",
		        ImageIO.getReaderFormatNames(), false, true);
		File[] imageFiles = folder.listFiles(imageFilter);
		if (imageFiles != null)
		{
			for (File file : imageFiles)
			{
				try
				{
					URL fileURL = file.toURI().toURL();
					if (!fileURL.equals(getConfig().getBackgroundImage()))
					{
						model.add(fileURL, file.getName());
					}
				}
				catch (MalformedURLException e)
				{
					throw new AssertionError(e);
				}
			}
		}

		// select current background
		if (getConfig().getBackgroundImage() == null)
		{
			list.setSelectedIndex(0);
		}
		else
		{
			list.setSelectedValue(getConfig().getBackgroundImage(), false);
		}

		// list to selection changes
		list.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting())
				{
					getConfig().setBackgroundImage(list.getSelectedValue());
				}
			}
		});
	}

	private JColorComboBox createColorCombo(Color selectedColor)
	{
		ColorComboBoxModel colorComboModel = ColorComboBoxModel.createRainbowModel(true);
		final JColorComboBox colorCombo = new JColorComboBox(colorComboModel);
		colorComboModel.setSelectedItem(selectedColor);
		return colorCombo;
	}

	private JPanel createDisplayTab()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new SpringLayout());
		panel.add(createMultiLineLabel(Resources.get().getString(
		        "ConfigEditor.display.about"), 3));
		panel.add(createDisplayPanel(DisplayConfig.PRESENTATION_MODE));
		panel.add(Box.createRigidArea(new Dimension(5, 5)));
		panel.add(createDisplayPanel(DisplayConfig.EDITOR_MODE));
		SpringUtilities.makeCompactGrid(panel, 4, 1, 0, 0, 5, 5);
		return panel;
	}

	private JPanel createDisplayPanel(final String mode)
	{
		DisplayConfig displayConfig = getConfig().getDisplayConfig(mode);

		final GraphicsEnvironmentView view = new GraphicsEnvironmentView();
		view.setSelectedDevice(displayConfig.getDevice());
		view.setEmptySelectionAllowed(false);
		view.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DisplayConfig displayConfig = getConfig().getDisplayConfig(mode);
				displayConfig.setDevice(view.getSelectedDevice());
			}
		});

		String borderTitle = "";
		if (mode == DisplayConfig.PRESENTATION_MODE)
		{
			borderTitle = Resources.get().getString(
			        "ConfigEditor.display.presentation");
		}
		else if (mode == DisplayConfig.EDITOR_MODE)
		{
			borderTitle = Resources.get().getString(
			        "ConfigEditor.display.editor");
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(borderTitle));
		panel.add(view);
		panel.add(Box.createRigidArea(new Dimension(5, 5)));
		return panel;
	}

	private JPanel createAnnouncementsTab()
	{
		// create text pane to edit announcement text
		final JTextPane textPane = new JTextPane();
		textPane.setPreferredSize(new Dimension(100, 100));
		textPane.setEnabled(false);

		// create table to edit the parameters of an announcement preset
		parameterModel = new AnnouncementParameterTableModel(null);
		parameterTable = new JTable(parameterModel);
		parameterTable.setPreferredScrollableViewportSize(new Dimension(100,
		        100));
		parameterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parameterTable.getSelectionModel().addListSelectionListener(
		        new ListSelectionListener()
		        {
			        public void valueChanged(ListSelectionEvent e)
			        {
				        boolean selected = parameterTable.getSelectedRow() != -1;
				        deleteAnnouncementParameterAction.setEnabled(selected);
			        }
		        });

		// render parameter table values properly
		parameterTable.setDefaultRenderer(Class.class,
		        new DefaultTableCellRenderer()
		        {
			        @Override
			        public Component getTableCellRendererComponent(
			                JTable table, Object value, boolean isSelected,
			                boolean cellHasFocus, int row, int column)
			        {
				        final String displayValue;
				        if (value == DateParameter.class)
				        {
					        displayValue = Resources.get().getString(
					                "announcements.dateParameter");
				        }
				        else if (value == TextParameter.class)
				        {
					        displayValue = Resources.get().getString(
					                "announcements.textParameter");
				        }
				        else
				        {
					        displayValue = Resources.get().getString(
					                "announcements.unknownParameter");
				        }
				        return super.getTableCellRendererComponent(table,
				                displayValue, isSelected, cellHasFocus, row,
				                column);
			        }
		        });
		parameterTable.setDefaultRenderer(Parameter.class,
		        new ParameterRenderer());

		// set parameter table's column headers
		TableColumnModel columnModel = parameterTable.getColumnModel();
		columnModel.getColumn(0).setHeaderValue(
		        Resources.get().getString("announcements.parameterTag"));
		columnModel.getColumn(0).setPreferredWidth(150);
		columnModel.getColumn(1).setHeaderValue(
		        Resources.get().getString("announcements.parameterType"));
		columnModel.getColumn(1).setPreferredWidth(100);
		columnModel.getColumn(2).setHeaderValue(
		        Resources.get().getString("announcements.parameterProperties"));
		columnModel.getColumn(2).setPreferredWidth(300);

		// create list of announcements
		final AnnouncementListModel announcementModel = new AnnouncementListModel(
		        config);
		addPropertyChangeListener(CONFIG_PROPERTY, new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent evt)
			{
				announcementModel.setConfig(config);
			}
		});
		announcementList = new JList(announcementModel);
		announcementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		announcementList.setVisibleRowCount(5);
		announcementList.setCellRenderer(new AnnouncementCellRenderer());

		// handle announcement selection
		announcementList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				Announcement announcement = (Announcement) announcementList.getSelectedValue();
				parameterModel.setAnnouncement(announcement);
				deleteAnnouncementPresetAction.setEnabled(announcement != null);
				textPane.setEnabled(announcement != null);
				newAnnouncementParameterAction.setEnabled(announcement != null);
				if (announcement == null)
				{
					textPane.setText("");
				}
				else
				{
					textPane.setText(announcement.getText());
				}
			}
		});

		// handle text changes
		textPane.getDocument().addDocumentListener(new DocumentListener()
		{
			public void textChanged()
			{
				Announcement announcement = (Announcement) announcementList.getSelectedValue();
				if (announcement != null)
				{
					announcement.setText(textPane.getText());
				}
			}

			public void insertUpdate(DocumentEvent e)
			{
				textChanged();
			}

			public void removeUpdate(DocumentEvent e)
			{
				textChanged();
			}

			public void changedUpdate(DocumentEvent e)
			{
				textChanged();
			}
		});

		EditableList announcementListEditor = new EditableList(
		        announcementList, false);
		announcementListEditor.setAddAction(newAnnouncementPresetAction);
		announcementListEditor.setRemoveAction(deleteAnnouncementPresetAction);

		JScrollPane textPaneScroller = new JScrollPane(textPane);

		EditableList parameterListEditor = new EditableList(parameterTable);
		parameterListEditor.setAddAction(newAnnouncementParameterAction);
		parameterListEditor.setEditAction(editAnnouncementParameterAction);
		parameterListEditor.setRemoveAction(deleteAnnouncementParameterAction);

		// layout components
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(createLabel("configEditor.announcements"));
		panel.add(announcementListEditor);
		panel.add(Box.createRigidArea(new Dimension(5, 5)));
		panel.add(createLabel("configEditor.announcements.text"));
		panel.add(textPaneScroller);
		panel.add(Box.createRigidArea(new Dimension(5, 5)));
		panel.add(createLabel("configEditor.announcements.parameters"));
		panel.add(parameterListEditor);
		return panel;
	}

	private String createPreviewText()
	{
		ApplicationDescriptor application = Application.getInstance().getDescriptor();

		StringBuilder text = new StringBuilder();
		for (int i = 0; i < 5; i++)
		{
			text.append(application.getTitle());
			text.append(' ');
			text.append(application.getVersion());
			text.append('\n');
			text.append(application.getCopyright());
			text.append('\n');
			if (application.getVendor() != null)
			{
				text.append(application.getVendor());
				if (application.getVendorURL() != null)
				{
					text.append(application.getVendorURL());
				}
				text.append('\n');
			}
		}
		return text.toString();
	}

	private JLabel createLabel(String resourceName)
	{
		JLabel label = new JLabel(Resources.get().getString(resourceName));
		label.setAlignmentX(CENTER_ALIGNMENT);
		Dimension maximumSize = label.getMaximumSize();
		maximumSize.width = Integer.MAX_VALUE;
		label.setMaximumSize(maximumSize);
		return label;
	}

	private class MarginsPreview extends JPanel implements
	        PropertyChangeListener
	{
		/** Constructs a new margins preview component. */
		public MarginsPreview()
		{
			getConfig().addPropertyChangeListener(this);
			ConfigManager configManager = ConfigManager.getInstance();
			configManager.addPropertyChangeListener(this);
		}

		public void propertyChange(PropertyChangeEvent e)
		{
			if (e.getPropertyName() == Config.FULL_SCREEN_MARGINS_PROPERTY)
			{
				repaint();
			}
			else if (e.getPropertyName() == ConfigEditor.CONFIG_PROPERTY)
			{
				if (e.getOldValue() instanceof Config)
				{
					Config config = (Config) e.getOldValue();
					config.removePropertyChangeListener(this);
				}
				getConfig().addPropertyChangeListener(this);
			}
		}

		@Override
		public void paintComponent(Graphics g)
		{
			Color bgColor = config.getBackgroundColor();
			g.setColor(bgColor.brighter());
			g.fillRect(0, 0, getWidth(), getHeight());

			Insets insets = getInsets();
			Rectangle textArea = new Rectangle(insets.left, insets.top,
			        getWidth() - insets.left - insets.right - 1, getHeight()
			                - insets.top - insets.bottom - 1);
			Rectangle2D margins = config.getFullScreenMargins();
			textArea.x += (int) (margins.getX() * textArea.width);
			textArea.width = (int) (margins.getWidth() * textArea.width);

			g.setColor(bgColor);
			g.fillRect(textArea.x, textArea.y, textArea.width, textArea.height);

			g.setColor(Color.WHITE);
			g.drawLine(textArea.x, 0, textArea.x, getHeight());
			g.drawLine(textArea.x + textArea.width, 0, textArea.x
			        + textArea.width, getHeight());
			g.drawLine(0, textArea.y, getWidth(), textArea.y);
			g.drawLine(0, textArea.y + textArea.height, getWidth(), textArea.y
			        + textArea.height);
		}
	}

	private class ParameterRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table,
		        Object value, boolean isSelected, boolean cellHasFocus,
		        int row, int column)
		{
			final String displayValue;
			if (value instanceof DateParameter)
			{
				DateParameter parameter = (DateParameter) value;

				SimpleDateFormat format = parameter.getFormat();
				Date date = parameter.getValue();
				String preview = format.format(date == null ? new Date() : date);

				String dateString;
				if (date == null)
				{
					dateString = Resources.get().getString(
					        "announcements.dateParameter.currentDateTime");
				}
				else
				{
					dateString = format.format(date);
				}

				displayValue = Resources.get().getString(
				        "announcements.dateParameter.properties", dateString,
				        preview);

			}
			else if (value instanceof TextParameter)
			{
				TextParameter parameter = (TextParameter) value;
				displayValue = Resources.get().getString(
				        "announcements.textParameter.properties",
				        parameter.getLabel());

			}
			else
			{
				displayValue = "";
			}
			return super.getTableCellRendererComponent(table, displayValue,
			        isSelected, cellHasFocus, row, column);
		}
	}
}
