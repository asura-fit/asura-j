/*
 * 作成日: 2008/11/24
 */
package javax.vecmath;

/**
 * Copyright 2001-2008 ASURA-FIT. All Rights Reserved. DO NOT ALTER OR REMOVE
 * COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 only, as published by
 * the Free Software Foundation. ASURA-FIT designates this particular file as
 * subject to the "Classpath" exception as provided by ASURA-FIT in the LICENSE
 * file that accompanied this code.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License version 2 for more
 * details (a copy is included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact ASURA-FIT, or visit asura.fit.ac.jp if you need additional
 * information or have any questions.
 * 
 * @author sey
 * 
 * @version $Id: XVecMathUtils.java 714 2008-11-24 08:03:33Z sey $
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
