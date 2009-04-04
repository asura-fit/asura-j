package jp.ac.fit.asura.nao.naoji;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

public class DatagramSocketServiceTest extends TestCase {
	static DatagramSocketService ds;


	public DatagramSocketServiceTest(String name) {
		super(name);
	}

	//debug
	public static void printByteArray(byte[] b) {
		int cnt = 0;
		for (int i=0; i<b.length; i++) {
			if (cnt % 10 == 0) {
				System.out.println();
				System.out.print(cnt + ": ");
			}
			System.out.print(b[i] + " ");
			cnt++;
		}

		System.out.println();

	}


	public void testreceive() {
		System.out.println("test receive()");

		assertNotNull("nullpointer", ds.soc);
		assertNotNull("nullpointer", ds.rcv);

		byte[] buf;
		ByteBuffer bbuf = ByteBuffer.allocate(DatagramSocketService.size);

		buf = ds.receive();
		System.out.println("get a packet");
		printByteArray(buf);

		ds.receive(bbuf);
		System.out.println("get a packet");
		printByteArray(bbuf.array());

		System.out.println("eot");


		try {
			Thread.sleep(200);
		} catch (Exception e) {}

	}

	public void testsend() {
		System.out.println("test send()");

		assertNotNull("nullpo", ds.soc);
		assertNotNull("nullpo", ds.rcv);

		byte[] buf = new byte[ds.size];
		buf[0] = 42; buf[1] = 42; buf[2] = 42; buf[3] = 127; buf[4] = 100;
		buf[5] = 1; buf[6] = 2; buf[7] = 3; buf[8] = 4; buf[9] = 5;
		for (int i = 10; i<ds.size; i++)
			buf[i] = 0;
		ByteBuffer bb = ByteBuffer.wrap(buf);

		System.out.println("send a packet");
		ds.send(bb);
		printByteArray(bb.array());

		System.out.println("eot");

	}

	@Override
	protected void setUp() throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		super.setUp();
		ds = new DatagramSocketService();
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		super.tearDown();
	}
}
