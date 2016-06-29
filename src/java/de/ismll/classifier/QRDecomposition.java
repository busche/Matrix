package de.ismll.classifier;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;

/** QR Decomposition.  From Jama
<P>
   For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
   orthogonal matrix Q and an n-by-n upper triangular matrix R so that
   A = Q*R.
<P>
   The QR decompostion always exists, even if the matrix does not have
   full rank, so the constructor will never fail.  The primary use of the
   QR decomposition is in the least squares solution of nonsquare systems
   of simultaneous linear equations.  This will fail if isFullRank()
   returns false.
 */

public class QRDecomposition implements java.io.Serializable {

	/* ------------------------
   Class variables
	 * ------------------------ */

	/** Array for internal storage of decomposition.
   @serial internal array storage.
	 */
	private float[][] QR;

	/** Row and column dimensions.
   @serial column dimension.
   @serial row dimension.
	 */
	private int m, n;

	/** Array for internal storage of diagonal of R.
   @serial diagonal of R.
	 */
	private float[] Rdiag;

	/* ------------------------
   Constructor
	 * ------------------------ */

	/**
	 * QR Decomposition, computed by Householder reflections.
	 * 
	 * @param A
	 *            Rectangular matrix
	 */

	public QRDecomposition (Matrix A) {
		// Initialize.
		QR = Matrices.asArray(A);

		m = A.getNumRows();
		n = A.getNumColumns();
		Rdiag = new float[n];

		// Main loop.
		for (int k = 0; k < n; k++) {
			// Compute 2-norm of k-th column without under/overflow.
			double nrm = 0;
			for (int i = k; i < m; i++) {
				nrm = hypot(nrm,QR[i][k]);
			}

			if (nrm != 0.0) {
				// Form k-th Householder vector.
				if (QR[k][k] < 0) {
					nrm = -nrm;
				}
				for (int i = k; i < m; i++) {
					QR[i][k] /= nrm;
				}
				QR[k][k] += 1.0;

				// Apply transformation to remaining columns.
				for (int j = k+1; j < n; j++) {
					double s = 0.0;
					for (int i = k; i < m; i++) {
						s += QR[i][k]*QR[i][j];
					}
					s = -s/QR[k][k];
					for (int i = k; i < m; i++) {
						QR[i][j] += s*QR[i][k];
					}
				}
			}
			Rdiag[k] = (float) -nrm;
		}
	}

	private static double hypot(double a, double b) {
		double r;
		if (Math.abs(a) > Math.abs(b)) {
			r = b / a;
			r = Math.abs(a) * Math.sqrt(1 + r * r);
		} else if (b != 0) {
			r = a / b;
			r = Math.abs(b) * Math.sqrt(1 + r * r);
		} else {
			r = 0.0;
		}
		return r;
	}

	/* ------------------------
   Public Methods
	 * ------------------------ */

	/** Is the matrix full rank?
   @return     true if R, and hence A, has full rank.
	 */

	public boolean isFullRank () {
		for (int j = 0; j < n; j++) {
			if (Rdiag[j] == 0)
				return false;
		}
		return true;
	}

	/** Return the Householder vectors
   @return     Lower trapezoidal matrix whose columns define the reflections
	 */

	public DefaultMatrix getH () {
		DefaultMatrix X = new DefaultMatrix(m,n);
		float[][] H = X.data;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (i >= j) {
					H[i][j] = QR[i][j];
				} else {
					H[i][j] = 0.0f;
				}
			}
		}
		return X;
	}

	/** Return the upper triangular factor
   @return     R
	 */

	public DefaultMatrix getR () {
		DefaultMatrix X = new DefaultMatrix(n,n);
		float[][] R = X.data;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i < j) {
					R[i][j] = QR[i][j];
				} else if (i == j) {
					R[i][j] = Rdiag[i];
				} else {
					R[i][j] = 0.0f;
				}
			}
		}
		return X;
	}

	/** Generate and return the (economy-sized) orthogonal factor
   @return     Q
	 */

	public DefaultMatrix getQ () {
		DefaultMatrix X = new DefaultMatrix(m,n);
		float[][] Q = X.data;
		for (int k = n-1; k >= 0; k--) {
			for (int i = 0; i < m; i++) {
				Q[i][k] = 0.0f;
			}
			Q[k][k] = 1.0f;
			for (int j = k; j < n; j++) {
				if (QR[k][k] != 0) {
					double s = 0.0;
					for (int i = k; i < m; i++) {
						s += QR[i][k]*Q[i][j];
					}
					s = -s/QR[k][k];
					for (int i = k; i < m; i++) {
						Q[i][j] += s*QR[i][k];
					}
				}
			}
		}
		return X;
	}

	/** Least squares solution of A*X = B
   @param b    A Matrix with as many rows as A and any number of columns.
   @return     X that minimizes the two norm of Q*R*X-B.
   @exception  IllegalArgumentException  Matrix row dimensions must agree.
   @exception  RuntimeException  Matrix is rank deficient.
	 */

	public Vector solve (Vector b) {
		if (b.size() != m) {
			throw new IllegalArgumentException("Matrix row dimensions must agree.");
		}
		if (!this.isFullRank()) {
			throw new RuntimeException("Matrix is rank deficient.");
		}

		b = new DefaultVector(b);

		// Compute Y = transpose(Q)*b
		for (int k = 0; k < n; k++) {
			double s = 0.0;
			for (int i = k; i < m; i++) {
				s += QR[i][k]*b.get(i);
			}
			s = -s/QR[k][k];
			for (int i = k; i < m; i++) {
				b.set(i, (float) (b.get(i) + s*QR[i][k]));
			}
		}

		// Solve R*X = Y;
		for (int k = n-1; k >= 0; k--) {
			b.set(k, b.get(k)/Rdiag[k]);

			for (int i = 0; i < k; i++) {
				b.set(i, b.get(i) - b.get(k)* QR[i][k]);
			}
		}

		Vector ret = new DefaultVector(n);
		for (int i = 0; i <= n-1; i++) {
			ret.set(i,b.get(i));

		}
		return ret;

	}
}
