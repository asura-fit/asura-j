/**
 *
 */
package jp.ac.fit.asura.nao.misc;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

/**
 * @author sey
 *
 */
public class XMLWriter {
	List<String> stack;
	Writer wr;

	public XMLWriter(Writer writer) {
		this.wr = writer;
		stack = new ArrayList<String>();
	}

	public void characters(String string) throws SAXException {
		try {
			wr.write(string);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void startDocument() throws SAXException {
		try {
			wr.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void endDocument() throws SAXException {
		try {
			if (stack.size() != 0)
				throw new SAXException(stack.toString());
			wr.flush();
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void startElement(String name) throws SAXException {
		startElement(name, null);
	}

	public void startElement(String name, AttributesImpl attrs)
			throws SAXException {
		try {
			wr.append('<');
			wr.append(name);
			if (attrs != null)
				for (int i = 0; i < attrs.getLength(); i++) {
					wr.append(' ');
					// FIXME QName or LocalName
					wr.append(attrs.getLocalName(i));
					wr.append('=');
					wr.append('"');
					// FIXME encode
					wr.append(attrs.getValue(i));
					wr.append('"');
				}
			wr.append('>');
			stack.add(name);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void emptyElement(String name, AttributesImpl attrs)
			throws SAXException {
		try {
			wr.append('<');
			wr.append(name);
			if (attrs != null)
				for (int i = 0; i < attrs.getLength(); i++) {
					wr.append(' ');
					// FIXME QName or LocalName
					wr.append(attrs.getLocalName(i));
					wr.append('=');
					wr.append('"');
					// FIXME encode
					wr.append(attrs.getValue(i));
					wr.append('"');
				}
			wr.append('/');
			wr.append('>');
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void endElement(String name) throws SAXException {
		try {
			if (!name.equals(stack.get(stack.size() - 1)))
				throw new SAXException("Expected Element:" + name + " but "
						+ stack.get(stack.size() - 1));
			// <tag/>記法を使う?
			wr.append('<');
			wr.append('/');
			wr.append(name);
			wr.append('>');
			stack.remove(stack.size() - 1);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void endElement() throws SAXException {
		try {
			// <tag/>記法を使う?
			wr.append('<');
			wr.append('/');
			wr.append(stack.remove(stack.size() - 1));
			wr.append('>');
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		try {
			wr.write(ch, start, length);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}
}
