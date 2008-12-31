/*
 * 作成日: 2008/06/14
 */
package jp.ac.fit.asura.nao.vision.perception;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.ac.fit.asura.nao.vision.GCD;
import jp.ac.fit.asura.nao.vision.VisualContext;

/**
 * Blob関係のクラス
 * 
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class BlobVision {
	public static final int MAX_BLOBS = 200;

	protected int[] nBlobs;

	protected Blob[][] blobInfo;

	private VisualContext context;

	/**
	 * 横線の情報を保持するクラス.
	 * 
	 * ここでの線とは，画像の水平ライン(一次元)のなかで連続した色のまとまりのことである．
	 * 
	 * 例えば，***BBB**bbb***という行の場合はカラーBlueのBBBとbbbの二つのSegmentが作成される．
	 * 
	 * VisualCortexでの線(2D)とは違うので注意.
	 * 
	 */
	private static class Segment1D {
		int xmin, xmax;
		int length;

		// このSegmentが属するblobId
		int blobId;
		byte color;

		public Segment1D(int xmin, int xmax, byte color) {
			set(xmin, xmax, color);
		}

		void set(int xmin, int xmax, byte color) {
			this.xmin = xmin;
			this.xmax = xmax;
			length = xmax - xmin + 1;
			this.color = color;
			blobId = -1;

			if (color < 0) {
//				System.out.println(this);
				this.color = 0;
			}
		}

		public String toString() {
			return String.format("Start: %d End: %d Color: %d Number: %d",
					xmin, xmax, (int) color, (int) blobId);
		}
	}

	public static class Blob {
		public int xmin, ymin, xmax, ymax;
		public int mass;
		public boolean bigEnough;

		void set(int xi, int xa, int yi, int ya, int a) {
			xmin = xi;
			xmax = xa;
			ymin = yi;
			ymax = ya;
			mass = a;
		}

		/**
		 * 行yのセグメントsegをこのblobにマージします.
		 * 
		 * @param blob2
		 */
		public void merge(int y, Segment1D seg) {
			xmin = Math.min(xmin, seg.xmin);
			xmax = Math.max(xmax, seg.xmax);
			ymin = Math.min(ymin, y);
			ymax = Math.max(ymax, y);
			mass += seg.length;
		}

		/**
		 * blob2をこのblobにマージします.
		 * 
		 * @param blob2
		 */
		public void merge(Blob blob2) {
			xmin = Math.min(xmin, blob2.xmin);
			xmax = Math.max(xmax, blob2.xmax);
			ymin = Math.min(ymin, blob2.ymin);
			ymax = Math.max(ymax, blob2.ymax);
			mass += blob2.mass;
		}

		public String toString() {
			return String.format("X: %d-%d Y:%d-%d Mass:%d", xmin, xmax, ymin,
					ymax, mass);
		}

		public Rectangle getArea() {
			return new Rectangle(xmin, ymin, xmax - xmin + 1, ymax - ymin + 1);
		}
	}

	/**
	 * 
	 */
	public BlobVision() {
		// BLACKはblobにならないので -1
		nBlobs = new int[GCD.COLOR_NUM - 1];
		blobInfo = new Blob[GCD.COLOR_NUM - 1][BlobVision.MAX_BLOBS];
		for (int i = 0; i < blobInfo.length; i++) {
			for (int j = 0; j < BlobVision.MAX_BLOBS; j++)
				blobInfo[i][j] = new Blob();
		}
	}

	public void formBlobs() {
		byte[] plane = context.gcdPlane;
		int width = context.camera.width;
		int height = context.camera.height;
		// 初期化
		Arrays.fill(nBlobs, 0);
		for (int i = 0; i < blobInfo.length; i++)
			for (int j = 0; j < BlobVision.MAX_BLOBS; j++)
				blobInfo[i][j].mass = 0;

		// ラインごとの線
		List<List<Segment1D>> segments = new ArrayList<List<Segment1D>>(height);

		// 各行でSegmentを作る
		for (int i = 0; i < width * height; i += width) {
			List<Segment1D> list = new ArrayList<Segment1D>(16);

			// 二つの同じ色に挟まれた1pixelを補完する
			// ex:C*CとなっているのをCCCにする
			for (int j = i; j < i + width - 2; j++) {
				if (plane[j] == plane[j + 2])
					plane[j + 1] = plane[j];
			}

			// 一行分の画像からSegmentを抽出する
			for (int start = 0, end = 0, j = i; end < width; start = end) {
				byte color = plane[j];

				while (end < width && plane[j] == color) {
					end++;
					j++;
				}

				// 黒以外なら線分を作成
				// JavaだとsignedなのでFFが-1になる
				if (color >= 0 && color < GCD.cBLACK) {
					list.add(new Segment1D(start, end - 1, color));
				}
			}
			segments.add(list);
		}

		// Blob を作るのだ! とりあえず, 最初のラインに含まれる blob と成り得る物を列挙
		for (int i = 0; i < segments.get(0).size(); i++) {
			Segment1D seg = segments.get(0).get(i);

			// ignoreでなければblobにする
			if (seg.color < GCD.cBLACK) {
				allocateBlob(0, seg);
			}
		}

		// Merge blobs every 2 lines
		// 各水平ラインごとにマージしていきましょう
		for (int y = 1; y < height; y++) {
			int prev_line_i = 0;

			// 現在の行のSegmentをマージ
			for (int segNo = 0; segNo < segments.get(y).size(); segNo++) {
				assert (0 <= segNo && segNo < width);
				// 現在見ている segment
				Segment1D current = segments.get(y).get(segNo);

				// クラス化されていない色ならば無視
				if (current.color == GCD.cBLACK)
					continue;

				// 直前のラインのすべての segment を見ていく
				for (; prev_line_i < segments.get(y - 1).size(); prev_line_i++) {
					// 直前のラインで現在見ている segment
					Segment1D prev = segments.get(y - 1).get(prev_line_i);

					// 確実に交差しない条件
					if (prev.xmin > current.xmax)
						break;

					// マージが必要とされる条件
					// - オーバーラップしている
					// - 同じ色である
					// - 異なった blob 番号を持っている
					if (prev.color == current.color && // same color condition
							prev.xmax >= current.xmin && // overlap check
							prev.blobId != current.blobId // blob id check
					) {
						// current.blob_number == -1
						// ということは現在のsegmentはまだblobになっていない
						// じゃぁ、前のラインのblob にマージするのだ!
						if (current.blobId == -1) {
							current.blobId = prev.blobId;
							blobInfo[prev.color][prev.blobId].merge(y, current);
						} else if (prev.blobId == -1) {
							// current は blob に成ってるけど info1 が blob になっていない場合
							prev.blobId = current.blobId;
							blobInfo[current.color][current.blobId].merge(
									y - 1, prev);
						} else if (prev.blobId < MAX_BLOBS) {
							// info1, info2 ともに blob に成っている場合
							// blob 番号が小さい方にマージする

							// とりあえずprevのblob番号のほうが小さいと仮定
							Blob minBlob = blobInfo[prev.color][prev.blobId];
							Blob maxBlob = blobInfo[current.color][current.blobId];

							if (prev.blobId > current.blobId) {
								// 逆だった
								Blob t = minBlob;
								minBlob = maxBlob;
								maxBlob = t;
								prev.blobId = current.blobId;
							} else {
								current.blobId = prev.blobId;
							}

							minBlob.merge(maxBlob);
							// blob 番号が大きい方の blob は mass = 0 とすることで無効化
							maxBlob.mass = 0;
						}
					}
				}

				// マージの条件にマッチしなかった場合は新しい blob として生成される
				if (current.blobId == -1) {
					allocateBlob(y, current);
				}

				// ちょっと行き過ぎたので一つ戻す
				if (prev_line_i > 0)
					prev_line_i--;
			}
		}
	}

	/**
	 * segmentに新しいblobを割り当てます. 割り当てに成功した場合はtrueを，blobに空きがない場合はfalseを返します．
	 * 
	 * @param blobInfo
	 * @param nblob
	 * @param y
	 * @param segment
	 */
	private boolean allocateBlob(int y, Segment1D segment) {
		// blob 番号が MAX_BLOBS までいった場合は空き(mass == 0) の blob
		// 番号を探して, 再利用
		if (nBlobs[segment.color] >= MAX_BLOBS) {
			for (int i = 0; i < MAX_BLOBS; i++) {
				if (blobInfo[segment.color][i].mass == 0) {
					blobInfo[segment.color][i].set(segment.xmin, segment.xmax,
							y, y, segment.length);
					segment.blobId = i;
					return true;
				}
			}
			// blobの空きがない．
			return false;
		} else {
			segment.blobId = nBlobs[segment.color]++;
			blobInfo[segment.color][segment.blobId].set(segment.xmin,
					segment.xmax, y, y, segment.length);
		}
		return true;
	}

	/**
	 * Blobからオブジェクトになりそうなやつを探す.
	 * 
	 * 選択基準として Blob の中から大きいやつを選択していき max個まで格納.
	 */
	public List<Blob> findBlobs(byte colorIndex, int max, int massThreshold) {
		Blob[] binfo = blobInfo[colorIndex];
		int upto = Math.min(max, nBlobs[colorIndex]);

		List<Blob> list = new ArrayList<Blob>(upto);

		for (int i = 0; i < upto; i++) {
			int biggest = -1, bigIndex = -1;
			for (int j = 0; j < nBlobs[colorIndex]; j++) {
				if (binfo[j].mass > biggest && !list.contains(binfo[j])) {
					// すでに探索したblob以外なら候補にする
					biggest = binfo[j].mass;
					bigIndex = j;
				}
			}

			if (bigIndex == -1) {
				// その色のblobは一個もないよ!
				return list;
			}

			if (biggest < massThreshold) {
				// all remaining blobs are so small,
				// not considered as half beacon
				return list;
			}

			// now bigIndex is set to the index of the next biggest blob
			list.add(binfo[bigIndex]);
		}

		// set the variable so the caller knows how many halfbeacons of this
		// type we found
		return list;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(VisualContext context) {
		this.context = context;
	}
}
