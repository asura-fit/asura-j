/**
 *
 */
package jp.ac.fit.asura.nao.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author sey
 *
 */
public class AttributesImpl {
	List<String> names;
	List<String> values;

	public AttributesImpl() {
		names = new ArrayList<String>(8);
		values = new ArrayList<String>(8);
	}

	public AttributesImpl(int capacity) {
		names = new ArrayList<String>(capacity);
		values = new ArrayList<String>(capacity);
	}

	public AttributesImpl(Object name, Object value) {
		names = new ArrayList<String>(4);
		values = new ArrayList<String>(4);
		names.add(name.toString());
		values.add(value.toString());
	}

	public AttributesImpl(Object... attrs) {
		names = new ArrayList<String>(1);
		values = new ArrayList<String>(1);
		if (attrs.length % 2 != 0)
			throw new IllegalArgumentException("" + Arrays.toString(attrs));
		for (int i = 0; i < attrs.length / 2; i++) {
			names.add(attrs[2 * i].toString());
			values.add(attrs[2 * i + 1].toString());
		}
	}

	public void setAttribute(Object name, Object value) {
		int i = names.indexOf(name);
		if (i == -1)
			addAttribute(name, value);
		else
			values.set(i, value.toString());
	}

	public void addAttribute(Object name, Object value) {
		names.add(name.toString());
		values.add(value.toString());
	}

	public int getLength() {
		return names.size();
	}

	public String getLocalName(int index) {
		return names.get(index);
	}

	public String getValue(int index) {
		return values.get(index);
	}
}
