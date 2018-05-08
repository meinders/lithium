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

package lithium.catalog;

import java.beans.*;
import java.util.*;

import lithium.*;

/**
 * A referenced lyric, which returns lyric information by dynamically looking up
 * another lyric using a lyric reference. The lyric may be assigned another
 * number than the lyric being referenced.
 *
 * <p>
 * Property change events are only fired when the property change resulted from
 * calling a method on this object.
 *
 * @author Gerrit Meinders
 */
public class ReferenceLyric extends Lyric implements PropertyChangeListener {
    private int number;
    private LyricRef reference;
    protected Lyric lyric;

    public ReferenceLyric(LyricRef reference) {
        this(reference.getNumber(), reference);
    }

    public ReferenceLyric(int number, LyricRef reference) {
        if (reference == null) {
            throw new NullPointerException("reference");
        }
        this.number = number;
        this.reference = reference;
    }

    @Override
    public int getNumber() {
        return number;
    }

    public LyricRef getReference() {
        return reference;
    }

    /**
     * Obtains a lyric instance using the lyric reference, registers listeners
     * on the lyric and sets the lyric instance variable to the obtained lyric.
     *
     * @return whether the lyric could be resolved
     */
    protected boolean obtainLyric() {
        lyric = CatalogManager.getCatalog().getLyric(reference);
        boolean resolved = lyric != null;
        if (resolved) {
            lyric.addPropertyChangeListener(this);
        }
        return resolved;
    }

    /**
     * Releases the lyric last obtained using {@link #obtainLyric}.
     */
    protected void releaseLyric() {
        lyric.removePropertyChangeListener(this);
        lyric = null;
    }

    @Override
    public void setTitle(String title) {
        if (obtainLyric()) {
            lyric.setTitle(title);
            releaseLyric();
        }
    }

    @Override
    public String getTitle() {
        if (obtainLyric()) {
            String title = lyric.getTitle();
            releaseLyric();
            return title;
        } else {
            return null;
        }
    }

    @Override
    public void setText(String text) {
        if (obtainLyric()) {
            lyric.setText(text);
            releaseLyric();
        }
    }

    @Override
    public String getText() {
        if (obtainLyric()) {
            String text = lyric.getText();
            releaseLyric();
            return text;
        } else {
            return null;
        }
    }

    @Override
    public void setOriginalTitle(String originalTitle) {
        if (obtainLyric()) {
            lyric.setOriginalTitle(originalTitle);
            releaseLyric();
        }
    }

    @Override
    public String getOriginalTitle() {
        if (obtainLyric()) {
            String originalTitle = lyric.getOriginalTitle();
            releaseLyric();
            return originalTitle;
        } else {
            return null;
        }
    }

    @Override
    public void setCopyrights(String copyrights) {
        if (obtainLyric()) {
            lyric.setCopyrights(copyrights);
            releaseLyric();
        }
    }

    @Override
    public String getCopyrights() {
        if (obtainLyric()) {
            String copyrights = lyric.getCopyrights();
            releaseLyric();
            return copyrights;
        } else {
            return null;
        }
    }

    @Override
    public void setKeys(Set<String> keys) {
        if (obtainLyric()) {
            lyric.setKeys(keys);
            releaseLyric();
        }
    }

    @Override
    public Set<String> getKeys() {
        if (obtainLyric()) {
            Set<String> keys = lyric.getKeys();
            releaseLyric();
            return keys;
        } else {
            return null;
        }
    }

    @Override
    public void addKey(String key) {
        if (obtainLyric()) {
            lyric.addKey(key);
            releaseLyric();
        }
    }

    @Override
    public void removeKey(String key) {
        if (obtainLyric()) {
            lyric.removeKey(key);
            releaseLyric();
        }
    }

    @Override
    public void setBibleRefs(Set<BibleRef> bibleRefs) {
        if (obtainLyric()) {
            lyric.setBibleRefs(bibleRefs);
            releaseLyric();
        }
    }

    @Override
    public Set<BibleRef> getBibleRefs() {
        if (obtainLyric()) {
            Set<BibleRef> bibleRefs = lyric.getBibleRefs();
            releaseLyric();
            return bibleRefs;
        } else {
            return null;
        }
    }

    @Override
    public void addBibleRef(BibleRef bibleRef) {
        if (obtainLyric()) {
            lyric.addBibleRef(bibleRef);
            releaseLyric();
        }
    }

    @Override
    public void removeBibleRef(BibleRef bibleRef) {
        if (obtainLyric()) {
            lyric.removeBibleRef(bibleRef);
            releaseLyric();
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        PropertyChangeEvent myEvent = new PropertyChangeEvent(this,
                e.getPropertyName(), e.getOldValue(), e.getNewValue());
        myEvent.setPropagationId(e.getPropagationId());
        pcs.firePropertyChange(myEvent);
    }

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == this )
		{
			return true;
		}
		else if ( obj instanceof ReferenceLyric )
		{
			ReferenceLyric other = (ReferenceLyric)obj;
			return reference.equals( other.reference );
		}
		else if ( obj instanceof Lyric )
		{
			Lyric lyric = CatalogManager.getCatalog().getLyric( reference );
			return lyric != null && lyric.equals( obj );
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		Lyric lyric = CatalogManager.getCatalog().getLyric( reference );
		return lyric == null ? 0 : lyric.hashCode();
	}
}
