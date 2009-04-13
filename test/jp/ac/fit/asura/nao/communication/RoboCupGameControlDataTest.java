package jp.ac.fit.asura.nao.communication;

import jp.ac.fit.asura.nao.naoji.DatagramSocketService;
import junit.framework.TestCase;

public class RoboCupGameControlDataTest extends TestCase {
	RoboCupGameControlData rcg;
	DatagramSocketService ds;

//	class RoboCupGameControlData {
//	    char[] header = new char[4];           // header to identify the structure
//	    int version;             // version of the data structure
//	    int playersPerTeam;       // The number of players on a team
//	    int state;                // state of the game (STATE_READY, STATE_PLAYING, etc)
//	    int firstHalf;            // 1 = game in first half, 0 otherwise
//	    int kickOffTeam;          // the next team to kick off
//	    int secondaryState;       // Extra state information - (STATE2_NORMAL, STATE2_PENALTYSHOOT, etc)
//	    int dropInTeam;           // team that caused last drop in
//	    int dropInTime;          // number of seconds passed since the last drop in.  -1 before first dropin
//	    int secsRemaining;       // estimate of number of seconds remaining in the half
//	    TeamInfo teams[2];
//	}

	public RoboCupGameControlDataTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		super.setUp();
		rcg = new RoboCupGameControlData();
		ds = new DatagramSocketService();

	}

	@Override
	protected void tearDown() throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		super.tearDown();
	}

	public void testupdate() {
		while(true) {
			rcg.update(ds.receive());
			rcg.debug();
		}
	}

}
