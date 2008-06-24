/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.vision.objects;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public abstract class VisualObject {
	protected EnumMap<Properties, Object> properties;

	protected VisualObjects type;

	protected VisualContext context;

	private Set<Blob> blobs;

	public VisualObject(VisualObjects type) {
		properties = new EnumMap<Properties, Object>(Properties.class);
		blobs = new HashSet<Blob>();
		this.type = type;
	}

	public void setContext(VisualContext context) {
		this.context = context;
	}

	public void clear() {
		blobs.clear();
		clearCache();
	}
	
	public VisualObjects getType() {
		return type;
	}

	public Set<Blob> getBlobs() {
		return blobs;
	}

	public void clearCache() {
		properties.clear();
	}

	public int getInt(Properties prop) {
		if (properties.containsKey(prop)) {
			return ((Integer) properties.get(prop)).intValue();
		}
		get(Integer.class, prop);
		return getInt(prop);
	}

	public boolean getBoolean(Properties prop) {
		if (properties.containsKey(prop)) {
			return ((Boolean) properties.get(prop)).booleanValue();
		}
		get(Boolean.class, prop);
		return getBoolean(prop);
	}

	public <T> T get(Class<T> cl, Properties prop) {
		if (!properties.containsKey(prop))
			updateProperty(prop);
		return getCached(cl, prop);
	}

	protected <T> T getCached(Class<T> cl, Properties prop) {
		assert properties.containsKey(prop);
		assert cl.isInstance(properties.get(prop));
		return (T) properties.get(prop);
	}

	protected void updateProperty(Properties prop) {
		switch (prop) {
		case BottomTouched:
		case TopTouched:
		case LeftTouched:
		case RightTouched:
			context.generalVision.checkTouched(this);
			break;
		case Angle:
			context.generalVision.calcAngle(this);
			break;
		case Center:
			context.generalVision.calcCenter(this);
			break;
		case Confidence:
			context.generalVision.calcConfidence(this);
			break;
		case Area:
			context.generalVision.calcArea(this);
			break;
		default:
			assert false;
		}
	}

	public <T> void setProperty(Properties prop, T c) {
		properties.put(prop, c);
	}
}
