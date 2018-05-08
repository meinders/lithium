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

import java.util.*;
import javax.xml.namespace.*;

import com.github.meinders.common.*;
import org.xml.sax.helpers.*;

/**
 * A namespace context implementation based on the SAX NamespaceSupport class.
 *
 * @since 0.9
 * @version 0.9 (2006.02.26)
 * @author Gerrit Meinders
 */
class NamespaceSupportContext implements NamespaceContext {
    private NamespaceSupport support;

    public NamespaceSupportContext() {
        support = new NamespaceSupport();
    }

    public void declarePrefix(String prefix, String namespaceURI) {
        support.declarePrefix(prefix, namespaceURI);
    }

    public String getNamespaceURI(String prefix) {
        return support.getURI(prefix);
    }

    public String getPrefix(String namespaceURI) {
        return support.getPrefix(namespaceURI);
    }

    @SuppressWarnings("unchecked")
    public Iterator<String> getPrefixes(String namespaceURI) {
        return Enumerations.iterator(support.getPrefixes(namespaceURI));
    }
}
