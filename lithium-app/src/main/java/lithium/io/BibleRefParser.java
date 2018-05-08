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

import java.io.*;

import com.github.meinders.common.parser.*;
import lithium.catalog.*;
import lithium.io.BibleRefScanner.*;

import static lithium.io.BibleRefScanner.TokenKind.*;

/*
 * BNF-grammar in plain text
 * (notice the fancy LL(2) in <verse-chapter-range-tail>)
 *
 * <reference>                ::= <book> <range>
 * <book>                     ::= "NUMBER" "CHARS"
 *                              | "CHARS"
 * <chapter>                  ::= "NUMBER"
 * <verse>                    ::= "NUMBER" <verse-tail>
 * <verse-tail>               ::= "CHARS"
 *                              | empty
 * <range>                    ::= <chapter-range>
 *                              | "CHARS" <verse-range>
 * <chapter-range>            ::= <chapter> <chapter-range-tail>
 * <chapter-range-tail>       ::= "DASH" <chapter>
 *                              | "COLON" <verse> <verse-chapter-range>
 *                              | empty
 * <verse-chapter-range>      ::= "DASH" <verse-chapter-range-tail>
 *                              | <multiple-verse-ranges>
 *                              | empty
 * <verse-chapter-range-tail> ::= <verse> <multiple-verse-ranges>
 *                              | <chapter> "COLON" <verse>
 *                              | empty
 * <multiple-verse-ranges>    ::= "COMMA" <verse-range> <multiple-verse-ranges>
 *                              | empty
 * <verse-range>              ::= <verse> <verse-range-tail>
 * <verse-range-tail>         ::= "DASH" <verse>
 *                              | empty
 */

/**
 * <p>
 * A parser for bible references. The following formats are supported:
 * <ul>
 * <li>{book} {chapter}
 * <li>{book} {chapter}-{chapter}
 * <li>{book} {chapter}:{verse}
 * <li>{book} {chapter}:{verse}-{verse}
 * <li>{book} {chapter}:{verse}-{chapter}:{verse}
 * <li>{book} {chapter}:{verse/range},{verse/range},...
 * <li>{book} verse {verse}
 * <li>{book} verse {verse}-{verse}
 * </ul>
 *
 * This results in the following LL(2) grammar:
 *
 * <pre>
 *  &lt;reference&gt;                ::= &lt;book&gt; &lt;range&gt;
 *  &lt;book&gt;                     ::= &quot;NUMBER&quot; &quot;CHARS&quot;
 *                               | &quot;CHARS&quot;
 *  &lt;chapter&gt;                  ::= &quot;NUMBER&quot;
 *  &lt;verse&gt;                    ::= &quot;NUMBER&quot;
 *  &lt;range&gt;                    ::= &lt;chapter-range&gt;
 *                               | &quot;CHARS&quot; &lt;verse-range&gt;
 *  &lt;chapter-range&gt;            ::= &lt;chapter&gt; &lt;chapter-range-tail&gt;
 *  &lt;chapter-range-tail&gt;       ::= &quot;DASH&quot; &lt;chapter&gt;
 *                               | &quot;COLON&quot; &lt;verse&gt; &lt;verse-chapter-range&gt;
 *                               | empty
 *  &lt;verse-chapter-range&gt;      ::= &quot;DASH&quot; &lt;verse-chapter-range-tail&gt;
 *                               | &lt;multiple-verse-ranges&gt;
 *                               | empty
 *  &lt;verse-chapter-range-tail&gt; ::= &lt;verse&gt;
 *                               | &lt;chapter&gt; &quot;COLON&quot; &lt;verse&gt;
 *                               | empty
 *  &lt;multiple-verse-ranges&gt;    ::= &quot;COMMA&quot; &lt;verse-range&gt; &lt;multiple-verse-ranges&gt;
 *                               | empty
 *  &lt;verse-range&gt;              ::= &lt;verse&gt; &lt;verse-range-tail&gt;
 *  &lt;verse-range-tail&gt;         ::= &quot;DASH&quot; &lt;verse&gt;
 *                               | empty
 * </pre>
 *
 * @see BibleRefScanner
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class BibleRefParser extends LLParser<TokenKind> {
    public static BibleRef parse(String data) throws ParserException {
        char[] chars = data.toCharArray();
        Reader in = new CharArrayReader(chars);
        return parse(in);
    }

    public static BibleRef parse(InputStream in) throws ParserException {
        try {
            BibleRefParser parser = new BibleRefParser(new BibleRefScanner(in));
            return parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BibleRef parse(Reader in) throws ParserException {
        try {
            BibleRefParser parser = new BibleRefParser(new BibleRefScanner(in));
            return parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Constructs a new parser using the given scanner.
     *
     * @param scanner the scanner
     */
    public BibleRefParser(BibleRefScanner scanner) throws IOException {
        super(scanner, 2);
    }

    public BibleRef parse() throws ParserException {
        String book = parseBook();
        Range range = parseRange(new Range());
        accept(EOF);
        try {
            return new BibleRef(book, range.getStartChapter(), range
                    .getEndChapter(), range.getStartVerse(), range
                    .getEndVerse());
        } catch (Exception e) {
            throw new ParserException(e);
        }
    }

    private String parseBook() throws ParserException {
        ensure(NUMBER, CHARS);
        String book;
        switch (token.getKind()) {
        case NUMBER:
            book = token.getText();
            accept();
            ensure(CHARS);
            book += " " + token.getText();
            accept();
            break;
        case CHARS:
            book = token.getText();
            accept();
            break;
        default:
            throw new AssertionError("Parser.ensure(...) failed");
        }
        return book;
    }

    private Range parseRange(Range range) throws ParserException {
        ensure(NUMBER, CHARS);
        if (token.getKind() == CHARS) {
            accept();
            return parseVerseRange(range);
        } else {
            return parseChapterRange(range);
        }
    }

    private Range parseChapterRange(Range range) throws ParserException {
        ensure(NUMBER);
        range.setStartChapter(Integer.parseInt(token.getText()));
        accept();
        return parseChapterRangeTail(range);
    }

    private Range parseChapterRangeTail(Range range) throws ParserException {
        switch (token.getKind()) {
        case DASH:
            accept();
            range.setEndChapter(parseChapter());
            break;
        case COLON:
            accept();
            range.setStartVerse(parseVerse());
            parseVerseChapterRange(range);
            break;
        }
        return range;
    }

    private Range parseVerseChapterRange(Range range) throws ParserException {
        if (token.getKind() == DASH) {
            accept();
            range = parseVerseChapterRangeTail(range);
            return parseMultipleVerseRanges(range);
        } else if (token.getKind() == COMMA) {
            return parseMultipleVerseRanges(range);
        } else {
            return range;
        }
    }

    private Range parseVerseChapterRangeTail(Range range)
            throws ParserException {
        if (getLookahead(0).getKind() == NUMBER) {
            if (getLookahead(1).getKind() == COLON) {
                range.setEndChapter(parseChapter());
                accept();
                range.setEndVerse(parseVerse());
            } else {
                range.setEndVerse(parseVerse());
            }
        }
        return range;
    }

    private Range parseMultipleVerseRanges(Range range) throws ParserException {
        while (token.getKind() == COMMA) {
            accept();
            Range verseRange = parseVerseRange(new Range());
            range.include(verseRange);
        }
        return range;
    }

    private Range parseVerseRange(Range range) throws ParserException {
        range.setStartVerse(parseVerse());
        return parseVerseRangeTail(range);
    }

    private Range parseVerseRangeTail(Range range) throws ParserException {
        if (token.getKind() == DASH) {
            accept();
            range.setEndVerse(parseVerse());
        }
        return range;
    }

    private int parseChapter() throws ParserException {
        ensure(NUMBER);
        int chapter = Integer.parseInt(token.getText());
        accept();
        return chapter;
    }

    private int parseVerse() throws ParserException {
        ensure(NUMBER);
        int verse = Integer.parseInt(token.getText());
        accept();
        if (token.getKind() == CHARS) {
            accept();
        }
        return verse;
    }

    private class Range {
        private Integer startChapter = null;

        private Integer endChapter = null;

        private Integer startVerse = null;

        private Integer endVerse = null;

        public Range() {
        }

        public void setStartChapter(Integer startChapter) {
            this.startChapter = startChapter;
        }

        public Integer getStartChapter() {
            return startChapter;
        }

        public void setEndChapter(Integer endChapter) {
            this.endChapter = endChapter;
        }

        public Integer getEndChapter() {
            return endChapter;
        }

        public void setStartVerse(Integer startVerse) {
            this.startVerse = startVerse;
        }

        public Integer getStartVerse() {
            return startVerse;
        }

        public void setEndVerse(Integer endVerse) {
            this.endVerse = endVerse;
        }

        public Integer getEndVerse() {
            return endVerse;
        }

        public void include(int verse) {
            if (startVerse == null) {
                startVerse = verse;
            } else if (endVerse == null) {
                if (verse > startVerse) {
                    endVerse = verse;
                } else if (verse < startVerse) {
                    endVerse = startVerse;
                    startVerse = verse;
                }
            } else {
                if (verse < startVerse) {
                    startVerse = verse;
                } else if (verse > endVerse) {
                    endVerse = verse;
                }
            }
        }

        public void include(Range range) {
            if (range.getStartVerse() != null) {
                include(range.getStartVerse());
                if (range.getEndVerse() != null) {
                    include(range.getEndVerse());
                }
            }
        }

        public String toString() {
            return startChapter + ":" + startVerse + "-" + endChapter + ":"
                    + endVerse;
        }
    }
}
