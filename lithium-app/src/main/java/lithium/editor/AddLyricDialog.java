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

import lithium.*;
import lithium.catalog.*;

/**
 * A dialog allowing the user to add a lyric to a bundle.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class AddLyricDialog extends EditLyricDialog {
	private Group bundle;

	public AddLyricDialog(Window parent, Group bundle) {
		super(Resources.get().getString("addLyricDialog.title"), parent, null,
		        false);
		this.bundle = bundle;
	}

	@Override
	protected boolean validateNumber(int number) {
		return bundle.getLyric(number) == null;
	}

	@Override
	protected void performOkAction(int number, String title, String text,
	        String originalTitle, String copyrights) {
		super.performOkAction(number, title, text, originalTitle, copyrights);
		bundle.addLyric(getLyric());
	}
}
