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

import java.net.*;
import java.util.regex.*;

import com.github.meinders.common.*;
import lithium.catalog.*;

public class PlaylistItem {

	private Object value;

	/**
	 * Delay in milliseconds until an automatic transition. If set to zero,
	 * automatic transitions are disabled.
	 */
	private int transitionDelay = 0;

	public PlaylistItem(Object value) {
		this.value = value;
	}

	public String getTitle() {
		String result;

		if (value == null) {
			result = Resources.get().getLabel("playlist.nullValue");

		} else if (value instanceof LyricRef) {
			LyricRef ref = (LyricRef) value;
			Lyric lyric = CatalogManager.getCatalog().getLyric(ref);
			if (lyric == null) {
				result = ref.getBundle() + " " + ref.getNumber();
			} else {
				result = getTitle(lyric);
			}

		} else if (value instanceof Lyric) {
			result = getTitle((Lyric) value);

		} else if (value instanceof ImageRef) {
			ImageRef imageRef = (ImageRef) value;
			URL url = imageRef.getSource();
			URI uri;
			String path;
			try {
				uri = url.toURI();
				path = uri.getPath();
			} catch (URISyntaxException e) {
				path = url.getPath();
			}
			result = path.substring(path.lastIndexOf('/') + 1);

		} else {
			final String text = value.toString();
			Pattern nonEmptyLine = Pattern.compile("^.*\\S+.*$",
			        Pattern.MULTILINE);
			Matcher matcher = nonEmptyLine.matcher(text);
			if (matcher.find()) {
				result = matcher.group();
			} else {
				result = "(leeg)"; // TODO: i18n
			}
		}

		return result;
	}

	private String getTitle(Lyric lyric) {
		String result;

		Group bundle = CatalogManager.getCatalog().getBundle(lyric);
		final ResourceUtilities resources = Resources.get("playlistCellRenderer");
		if (lyric == null) {
			String lyricRefString = resources.getString("lyricRef",
			        lyric.getNumber(), bundle.getName());
			result = resources.getString("notFound", lyricRefString);
		} else {
			result = resources.getString("lyric", lyric.getNumber(),
			        lyric.getTitle(), bundle.getName());
		}

		return result;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getTransitionDelay() {
		return transitionDelay;
	}

	public void setTransitionDelay(int transitionDelay) {
		this.transitionDelay = (transitionDelay < 0) ? 0 : transitionDelay;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + value + "]";
	}
}
