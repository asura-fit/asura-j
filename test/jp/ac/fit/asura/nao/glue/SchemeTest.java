/*
 * 作成日: 2008/12/30
 */
package jp.ac.fit.asura.nao.glue;

import java.io.FileReader;

import jp.ac.fit.asura.nao.AsuraCoreTest;
import jp.ac.fit.asura.nao.RobotContext;
import junit.framework.TestCase;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class SchemeTest extends TestCase {
	public void testScheme1() throws Exception {
		RobotContext context = AsuraCoreTest.createCore().getRobotContext();
		SchemeGlue glue = new SchemeGlue();
		glue.init(context);
		glue.load(new FileReader("scheme/asura-js.scm"));
		glue.load(new FileReader("scheme/robot-webots6.scm"));
	}
}