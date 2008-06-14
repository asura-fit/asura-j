/*
 * 作成日: 2008/06/14
 */
package jp.ac.fit.asura.nao.vision;

import static jp.ac.fit.asura.nao.vision.GCD.cBLACK;
import static jp.ac.fit.asura.nao.vision.GCD.cCYAN;
import static jp.ac.fit.asura.nao.vision.GCD.cGREEN;
import static jp.ac.fit.asura.nao.vision.GCD.cYELLOW;
import junit.framework.TestCase;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class BlobUtilsTest extends TestCase {
	public void testFormBlobs() {
		BlobUtils utils = new BlobUtils();

		byte[] plane = new byte[] { //
		cBLACK, cCYAN, cCYAN, cBLACK, cBLACK, //
				cBLACK, cYELLOW, cCYAN, cCYAN, cBLACK, //
				cYELLOW, cYELLOW, cGREEN, cYELLOW, cGREEN, //
				cBLACK, cBLACK, cBLACK, cYELLOW, cYELLOW, //
				cBLACK, cBLACK, cBLACK, cBLACK, cBLACK };

		assertEquals(5 * 5, plane.length);
		utils.formBlobs(plane, 5, 5);

		assertEquals(1, utils.nBlobs[cCYAN]);
		assertEquals(1, utils.nBlobs[cYELLOW]);
		assertEquals(1, utils.nBlobs[cGREEN]);

		System.out.println(utils.blobInfo[cCYAN][0]);
		System.out.println(utils.blobInfo[cYELLOW][0]);

		assertEquals(1, utils.blobInfo[cCYAN][0].xmin);
		assertEquals(3, utils.blobInfo[cCYAN][0].xmax);
		assertEquals(0, utils.blobInfo[cCYAN][0].ymin);
		assertEquals(1, utils.blobInfo[cCYAN][0].ymax);
		assertEquals(4, utils.blobInfo[cCYAN][0].mass);

		assertEquals(0, utils.blobInfo[cYELLOW][0].xmin);
		assertEquals(4, utils.blobInfo[cYELLOW][0].xmax);
		assertEquals(1, utils.blobInfo[cYELLOW][0].ymin);
		assertEquals(3, utils.blobInfo[cYELLOW][0].ymax);
		assertEquals(7, utils.blobInfo[cYELLOW][0].mass);
		
		assertFalse(utils.findBlobs(cCYAN, 1, 3).isEmpty());
		assertTrue(utils.findBlobs(cCYAN, 1, 5).isEmpty());
		assertFalse(utils.findBlobs(cYELLOW, 1, 3).isEmpty());
	}
}
