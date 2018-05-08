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
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;

import com.github.meinders.common.*;
import lithium.*;

/**
 * A dialog displaying information about the application.
 *
 * @author Gerrit Meinders
 */

public class AboutDialog extends JDialog
{
	/**
	 * Constructs a new about dialog.
	 *
	 * @param owner the {@code Window} from which the dialog is displayed or
	 *            {@code null} if this dialog has no owner
	 */
	public AboutDialog(Window owner)
	{
		super(owner, Resources.get().getString("about.title"),
		        ModalityType.APPLICATION_MODAL);
		init();
	}

	private void init()
	{
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setContentPane(createContentPane());
		setResizable(false);
		pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screen.width - getWidth()) / 2,
		        (screen.height - getHeight()) / 2);
	}

	private JPanel createContentPane()
	{
		BufferedImage background = null;
		try
		{
			background = ImageIO.read(getClass().getResource(
			        "/images/about.png"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		ApplicationDescriptor application = Application.getInstance().getDescriptor();

		String text = "<div style=\"font-family: SansSerif; font-size: 11pt;\">";
		text += "<b>" + application.getTitle() + " " + application.getVersion()
		        + "</b>";
		text += "<br>" + application.getCopyright();

		if (application.getVendor() != null)
		{
			text += "<br>Vendor: " + application.getVendor();
		}
		if (application.getVendorURL() != null)
		{
			text += "<br>" + application.getVendorURL();
		}
		if (application.getVendorEmail() != null)
		{
			text += "<br>E-mail: " + application.getVendorEmail();
		}

		text += "<br><br>" + System.getProperty("java.runtime.name");
		text += "<br>Version: " + System.getProperty("java.runtime.version");
		text += "<br>Vendor: " + System.getProperty("java.vm.vendor");
		text += "</div>";

		SimpleTextScroller scroller = new SimpleTextScroller(text);
		scroller.setBounds(5, 143, background.getWidth() - 10,
		        background.getHeight() - 143 - 5);

		JLabel versionLabel = new JLabel();
		versionLabel.setText(Resources.get().getString("about.version",
		        application.getVersion()));
		versionLabel.setOpaque(false);
		versionLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		versionLabel.setForeground(Color.WHITE);
		versionLabel.setBounds(20, 115, background.getWidth() - 40, 22);
		versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		JPanel panel = new ImagePanel(background);
		panel.setLayout(null);
		panel.add(scroller);
		panel.add(versionLabel);
		return panel;
	}

	private class ImagePanel extends JPanel
	{
		private BufferedImage image;

		private ImagePanel(BufferedImage image)
		{
			this.image = image;
		}

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(image.getWidth(), image.getHeight());
		}

		@Override
		public void paintComponent(Graphics g)
		{
			g.drawImage(image, 0, 0, null);
		}
	}

	private class SimpleTextScroller extends JPanel
	{
		private SimpleTextScroller(String text)
		{
			JEditorPane textComponent = new JEditorPane("text/html", text);
			textComponent.setOpaque(false);
			textComponent.setEditable(false);
			textComponent.setSelectionStart(0);
			textComponent.setSelectionEnd(0);

			JScrollPane scrollPane = new JScrollPane(textComponent);
			scrollPane.setBorder(null);
			scrollPane.setOpaque(false);
			scrollPane.getViewport().setOpaque(false);

			this.setOpaque(false);
			setLayout(new BorderLayout());
			add(scrollPane, BorderLayout.CENTER);
		}
	}
}
