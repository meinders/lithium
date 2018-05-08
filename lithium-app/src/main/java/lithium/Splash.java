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

import java.awt.*;

import com.github.meinders.common.*;

/**
 * Facade for {@link SplashScreen} that simplifies displaying status information
 * on the splash screen.
 *
 * @author Gerrit Meinders
 */
public class Splash implements StatusListener
{
	private static Splash instance;

	public static synchronized Splash getInstance()
	{
		if (instance == null)
		{
			instance = new Splash();
		}
		return instance;
	}

	private Splash()
	{
	}

	@Override
	public void setStatus(String status)
	{
		SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash != null)
		{
			Rectangle bounds = splash.getBounds();

			Graphics2D g = splash.createGraphics();

			g.setBackground(new Color(0, true));
			g.clearRect(0, bounds.height - 22, bounds.width, 22);

			g.setColor(Color.WHITE);
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			g.drawString(status, 5, 130);

			g.dispose();

			splash.update();
		}
	}
}
