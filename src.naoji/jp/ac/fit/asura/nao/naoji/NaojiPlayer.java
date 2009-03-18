/*
 * 作成日: 2009/03/19
 */
package jp.ac.fit.asura.nao.naoji;

import jp.ac.fit.asura.naoji.Naoji;
import jp.ac.fit.asura.naoji.NaojiFactory;
import jp.ac.fit.asura.naoji.NaojiModule;

/**
 *
 * Naoji用のbootstrapクラス.
 *
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaojiPlayer implements Naoji {
	static {
		NaojiModule.addFactory(new NaojiFactory() {
			public Naoji create() {
				return new NaojiPlayer();
			}
		});
	}

	public void init(Object arg0) {
	}

	public void exit() {
	}
}
