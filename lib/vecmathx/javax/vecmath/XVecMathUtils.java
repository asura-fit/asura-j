/*
 * 作成日: 2008/11/24
 */
package javax.vecmath;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class XVecMathUtils {
	/**
	 * Solve mat*x = b directly.
	 * 
	 * @param mat
	 * @param b
	 * @param x
	 */
	public static void solve(GMatrix mat, GVector b, GVector x)
			throws SingularMatrixException {
		int nRow = mat.getNumRow();
		int nCol = mat.getNumCol();

		int size = nRow * nCol;
		double[] temp = new double[size];
		int[] even_row_exchange = new int[1];
		int[] row_perm = new int[mat.nRow];
		int i, j;

		if (nRow != nCol) {
			throw new MismatchedSizeException(VecMathI18N
					.getString("GMatrix19"));
		}

		for (i = 0; i < nRow; i++) {
			for (j = 0; j < nCol; j++) {
				temp[i * nCol + j] = mat.values[i][j];
			}
		}

		// Calculate LU decomposition: Is the matrix singular?
		if (!GMatrix.luDecomposition(nRow, temp, row_perm, even_row_exchange)) {
			// Matrix has no inverse
			throw new SingularMatrixException(VecMathI18N
					.getString("GMatrix21"));
		}

		double[] result = new double[size];

		if (nRow != b.getSize()) {
			throw new MismatchedSizeException(VecMathI18N
					.getString("GVector16"));
		}

		for (i = 0; i < size; i++)
			result[i] = 0.0;
		for (i = 0; i < nRow; i++)
			result[i * nCol] = b.values[i];

		GMatrix.luBacksubstitution(nRow, temp, row_perm, result);

		for (i = 0; i < nRow; i++)
			x.values[i] = result[i * nCol];
	}
}
