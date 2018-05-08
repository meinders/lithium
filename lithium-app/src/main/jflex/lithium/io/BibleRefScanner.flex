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

import java.io.IOException;

import static lithium.io.BibleRefScanner.TokenKind.*;

/**
 * A lexical analyzer for bible reference parsing.
 *
 * @version 0.9 (2006.02.07)
 * @author Gerrit Meinders
 */
@SuppressWarnings("unused")
%%
%public
%class BibleRefScanner
%implements BibleRefScannerInterface
%function nextToken
%type BibleRefScannerToken
%unicode
%char

%{
    /** @see com.github.meinders.common.parser.Scanner#close() */
    public void close() throws IOException {
        yyclose();
    }

    /**
     * An enumeration of the kinds of tokens scanned by the BibleRefScanner class.
     *
     * @version 0.8 (2005.08.03)
     * @author Gerrit Meinders
     */
    public enum TokenKind {
        /** The end of file */
        EOF,
        /** A colon, which separates chapter and verse numbers */
        COLON,
        /** A comma, which separates combinations of chapter and verse ranges */
        COMMA,
        /** A dash, which separates the start and end of a range */
        DASH,
        /** A number */
        NUMBER,
        /** A string of characters */
        CHARS,
        /** A character that isn't matched by the scanner */
        UNKNOWN;
    }

    /**
     * Creates a token of the given kind and initializes it with the scanner's
     * current token data.
     *
     * @param kind the kind of token
     */
    private BibleRefScannerToken createToken(TokenKind kind) {
        return new BibleRefScannerToken(kind, yytext(), yychar);
    }
%}

LineTerminator = \r | \n | \r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Colon          = ":"
Comma          = ","
Dash           = "-"
Number         = [:digit:]+
Chars          = [:letter:]+
%%

<YYINITIAL> {
    <<EOF>>         {return createToken(EOF);}
    {WhiteSpace}    {/* ignored */}
    {Colon}         {return createToken(COLON);}
    {Comma}         {return createToken(COMMA);}
    {Dash}          {return createToken(DASH);}
    {Number}        {return createToken(NUMBER);}
    {Chars}         {return createToken(CHARS);}
    .               {return createToken(UNKNOWN);}
}

