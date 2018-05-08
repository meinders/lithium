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

import com.github.meinders.common.parser.*;
import lithium.io.BibleRefScanner.*;

/**
 * A helper class to work around JFlex's lacking support of generics.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
class BibleRefScannerToken extends Token<TokenKind> {
    public BibleRefScannerToken(TokenKind kind, String text, int position) {
        super(kind, text, position);
    }
}
