/*
 * 作成日: 2008/10/03
 */
package jp.ac.fit.asura.nao.sensation;

import java.util.Collection;
import java.util.EnumMap;

import jp.ac.fit.asura.nao.Context;
import jp.ac.fit.asura.nao.physical.Nao.Frames;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class SomaticContext extends Context {
	private EnumMap<Frames, FrameState> frames;

	public SomaticContext() {
		frames = new EnumMap<Frames, FrameState>(Frames.class);
		for (Frames frame : Frames.values())
			frames.put(frame, new FrameState(frame));
	}

	public SomaticContext(SomaticContext state) {
		frames = new EnumMap<Frames, FrameState>(Frames.class);
		for (Frames frame : state.frames.keySet())
			frames.put(frame, state.frames.get(frame).clone());
	}

	public FrameState get(Frames frame) {
		return frames.get(frame);
	}

	public Collection<FrameState> getFrames() {
		return frames.values();
	}
}