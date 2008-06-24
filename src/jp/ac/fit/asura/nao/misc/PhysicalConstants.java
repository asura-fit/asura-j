/*
 * 作成日: 2008/06/21
 */
package jp.ac.fit.asura.nao.misc;

/**
 * @author $Author$
 *
 * @version $Id$
 *
 */
public class PhysicalConstants {
	public static class Ball {
		// mm
		public static final int Radius = 43;
	}

	public static class Nao {
		// mm
		public static final int CameraHeight = 530;
	}

	public static class Goal {
		public static final int Height = 800;
		public static final int FullWidth = 1500;
		public static final int PoleRadius = 50;

		public static final int YellowGoalX = 0;
		public static final int YellowGoalY = -2700;
		public static final int BlueGoalX = 0;
		public static final int BlueGoalY = 2700;
	}

	public static class Field {
		public static final int MaxX = 200;
		public static final int MinX = -200;
		public static final int MaxY = 300;
		public static final int MinY = -300;
	}
}
