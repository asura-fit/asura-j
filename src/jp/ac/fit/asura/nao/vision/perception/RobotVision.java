/*
 * 作成日: 2010/02/25
 */
package jp.ac.fit.asura.nao.vision.perception;

import static jp.ac.fit.asura.nao.vision.GCD.cRED;
import static jp.ac.fit.asura.nao.vision.GCD.cBLUE;
import static jp.ac.fit.asura.nao.vision.VisualObjects.RedNao;
import static jp.ac.fit.asura.nao.vision.VisualObjects.BlueNao;

import java.awt.Rectangle;
import java.util.List;

import jp.ac.fit.asura.nao.physical.Field;
import jp.ac.fit.asura.nao.vision.VisualParam.Int;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

import org.apache.log4j.Logger;

/**
 * @author kilo
 * 
 */
public class RobotVision extends AbstractVision {
	private Logger log = Logger.getLogger(RobotVision.class);
	
	public void findRedNao() {
		VisualObject robot = getContext().get(RedNao);
		findRobot((RobotVisualObject) robot, cRED);
	}
	
	public void findBlueNao() {
		VisualObject robot = getContext().get(BlueNao);
		findRobot((RobotVisualObject) robot, cBLUE);
	}
	
	private void findRobot(RobotVisualObject vo, byte color) {
		int threshold = 50;
		List<Blob> blobs = getContext().blobVision.findBlobs(color, 10, threshold);
		List<Blob> set = vo.getBlobs();
		
		for (Blob blob : blobs) {
			set.add(blob);
		}
		
		if (!blobs.isEmpty()) {
			getContext().generalVision.processObject(vo);
			getContext().generalVision.calcBoundary(vo, color);
			ditectRobot(vo);
			calculateDistance(vo);
			checkRobotAngle(vo);
		}
	}
	
	private void ditectRobot(RobotVisualObject vo) {
		
		if (vo.getBlobs().size() < 2) {
			vo.confidence = 0;
		}
		
		return;
	}
	
	private void calculateDistance(RobotVisualObject vo) {
		if (vo.confidence == 0) {
			return;
		}
		
		int dist = -1;
		Rectangle area = vo.area;
		
		dist = (int) (53890 / (area.height + 3.01436f) - 96.5422f);
		
		if (dist * dist > Field.MaxY * Field.MaxX * 4) {
			log.debug(vo.getType() + " dist too far. dist:" + dist);
			vo.confidence = 0;
			return;
		}
		
		if (dist <= 0) {
			log.debug(vo.getType() + " invalid distance dist:" + dist);
			vo.confidence = 0;
			return;
		}
		
		vo.distance = dist;
		vo.distanceUsable = true;
		return;
	}
	
	private void checkRobotAngle(RobotVisualObject robot) {
		if (robot.robotAngle.y > 0.15f) {
			log.debug(robot.getType() + " sanity too high angle.");
			robot.confidence = 0;
		} else if (robot.robotAngle.y < -0.5f) {
			log.debug(robot.getType() + " sanity too low angle.");
			robot.confidence = 0;
		}
	}
}
