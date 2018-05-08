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

import java.util.*;

/**
 * The default implementation of a lyric, which directly contains the lyric and
 * the additional related information.
 *
 * @author Gerrit Meinders
 */
public class DefaultLyric extends Lyric {
    private final int number;
    private String title;

    private String text;
    private String originalTitle;
    private String copyrights;
    private Set<String> keys = new LinkedHashSet<String>();
    private Set<BibleRef> bibleRefs = new TreeSet<BibleRef>();

    public DefaultLyric(int number, String title) {
        this.number = number;
        setTitle(title);
    }

    public DefaultLyric(int number, Lyric lyric) {
        this.number = number;
        this.title = lyric.getTitle();
        this.text = lyric.getText();
        this.originalTitle = lyric.getOriginalTitle();
        this.copyrights = lyric.getCopyrights();
        this.keys.addAll(lyric.getKeys());
        this.bibleRefs.addAll(lyric.getBibleRefs());
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setTitle(String title) {
        this.title = title.trim();
        setModified(true);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setText(String text) {
        this.text = text.trim();
        setModified(true);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setOriginalTitle(String originalTitle) {
        if (originalTitle == null) {
            this.originalTitle = null;
        } else {
            this.originalTitle = originalTitle.trim();
        }
        setModified(true);
    }

    @Override
    public String getOriginalTitle() {
        return originalTitle;
    }

    @Override
    public void setCopyrights(String copyrights) {
        this.copyrights = copyrights.trim();
        setModified(true);
    }

    @Override
    public String getCopyrights() {
        return copyrights;
    }

    @Override
    public void setKeys(Set<String> keys) {
        if (!this.keys.equals(keys)) {
            this.keys.clear();
            this.keys.addAll(keys);
            setModified(true);
        }
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(keys);
    }

    @Override
    public void addKey(String key) {
        if (keys.add(key.trim())) {
            setModified(true);
        }
    }

    @Override
    public void removeKey(String key) {
        if (keys.remove(key.trim())) {
            setModified(true);
        }
    }

    @Override
    public void setBibleRefs(Set<BibleRef> bibleRefs) {
        if (!this.bibleRefs.equals(bibleRefs)) {
            this.bibleRefs.clear();
            this.bibleRefs.addAll(bibleRefs);
            setModified(true);
        }
    }

    @Override
    public Set<BibleRef> getBibleRefs() {
        return Collections.unmodifiableSet(bibleRefs);
    }

    @Override
    public void addBibleRef(BibleRef bibleRef) {
        if (bibleRefs.add(bibleRef)) {
            setModified(true);
        }
    }

    @Override
    public void removeBibleRef(BibleRef bibleRef) {
        if (bibleRefs.remove(bibleRef)) {
            setModified(true);
        }
    }
}

