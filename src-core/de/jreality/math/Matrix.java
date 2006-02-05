package de.jreality.math;

import java.io.Serializable;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.DoubleArray;

/**
 * 
 * Note: This class is not supposed to be a replaced for a full-fledged mathematical
 * package. It provides a convenient wrapper for double arrays that offers some basic
 * functionality for multiplying and inverting matrices and such, but if you want to
 * do more involved stuff, you probably want to use a dedicated math library.
 * 
 * @author weissman
 *
 **/
public class Matrix implements Serializable {
  
	public final static double TOLERANCE = Rn.TOLERANCE;
	
	/**
	 * @param A
	 * @param B
	 * @return A*B
	 */
	public static Matrix product(Matrix A, Matrix B) {
		return new Matrix(Rn.times(null, A.matrix, B.matrix));
	}

	/**
	 * @param A
	 * @param B
	 * @return A+B
	 */
	public static Matrix sum(Matrix A, Matrix B) {
		return new Matrix(Rn.add(null, A.matrix, B.matrix));
	}

	/**
	 * 
	 * @param A
	 * @param B
	 * @return B * A * B^-1
	 */
	public static Matrix conjugate(Matrix A, Matrix B) {
		return new Matrix(Rn.conjugateByMatrix(null, A.matrix, B.matrix));
	}

	protected double[] matrix;
  
  /**
   * this flag is kept for extending classes, that need to know
   * whether the matrix aray was changed. It's their responsibility
   * to reset this flag.
   */
  protected transient boolean matrixChanged=true;

	public Matrix() {
		this(Rn.setIdentityMatrix(new double[16]));
	}

	/**
	 * copy constructor
	 * @param T
	 */
	public Matrix(Matrix T) {
		matrix = new double[16];
		System.arraycopy(T.matrix, 0, matrix, 0, 16);
	}

    /**
     * plain wrapper for the array m; does NOT make a copy of m!
     * @param m the double array to be wrapped by this Matrix
     */
    public Matrix(double[] m) {
        if (m == null)
            m = Rn.setIdentityMatrix(new double[16]);
        if (m.length != 16)
            throw new IllegalArgumentException(
                    "invalid dimension for 4x4 matrix");
        matrix = m;
    }
    /**
     * TODO
     */
    public Matrix(double x00, double x01, double x02, double x03, double x10,
      double x11, double x12, double x13, double x20, double x21, double x22,
      double x23, double x30, double x31, double x32, double x33) {
    this(new double[] { x00, x01, x02, x03, x10, x11, x12, x13, x20, x21, x22,
        x23, x30, x31, x32, x33 });
  }

    /**
     * this constructor copies the content of the given DoubleArray. Note: the
     * given DoubleArray must have length == 16
     * 
     * @param the
     *          DoubleArray to copy and wrap
     */
    public Matrix(DoubleArray data) {
        this(data.toDoubleArray(null));
    }

    /**
     * this constructor copies the content of the given Transformation.
     * @param the Transformation to copy and wrap
     */
    public Matrix(Transformation data) {
        this(data.getMatrix());
    }

	/**
	 * copies initValue
	 * @param initValue
	 */
	public void assignFrom(double[] initValue) {
    matrixChanged = true;
		System.arraycopy(initValue, 0, matrix, 0, 16);
	}

	/**
	 * copies initValue
	 * @param initValue
	 */
	public void assignFrom(Matrix initValue) {
    matrixChanged = true;
		System.arraycopy(initValue.matrix, 0, matrix, 0, 16);
	}
	
  /**
   * copies initValue
   * @param initValue
   */
	public void assignFrom(DoubleArray data) {
	matrixChanged = true;
    	if (data.getLength() != 16)
			throw new IllegalArgumentException(
					"invalid dimension for 4x4 matrix");
		data.toDoubleArray(matrix);
	}

  /**
   * copies initValue
   * @param initValue
   */
  public void assignFrom(Transformation trafo) {
    matrixChanged = true;
    trafo.getMatrix(matrix);
  }
  
  /**
   * TODO: assign single values!
   */
  public void assignFrom(double x00, double x01, double x02, double x03, double x10,
      double x11, double x12, double x13, double x20, double x21, double x22,
      double x23, double x30, double x31, double x32, double x33) {
    assignFrom(new double[] { x00, x01, x02, x03, x10, x11, x12, x13, x20, x21, x22,
        x23, x30, x31, x32, x33 });
  }


  public void assignTo(double[] array) {
    System.arraycopy(matrix, 0, array, 0, 16);
  }
  
  public void assignTo(Matrix m) {
    m.assignFrom(matrix);
  }
  
  public void assignTo(Transformation trafo) {
    trafo.setMatrix(matrix);
  }

  public void assignTo(SceneGraphComponent comp) {
    Transformation t = comp.getTransformation();
    if (t == null) comp.setTransformation(new Transformation());
    assignTo(comp.getTransformation());
  }

	public void assignIdentity() {
    matrixChanged = true;
		Rn.setIdentityMatrix(matrix);
	}

	public double getDeterminant() {
		return Rn.determinant(matrix);
	}

	public double getTrace() {
		return Rn.trace(matrix);
	}

	public double getEntry(int row, int column) {
		return matrix[4 * row + column];
	}

	public void setEntry(int row, int column, double value) {
    if (matrix[4 * row + column] != value) matrixChanged = true;
		matrix[4 * row + column] = value;
	}

	public double[] getRow(int i) {
		return new double[] { matrix[4 * i], matrix[4 * i + 1],
				matrix[4 * i + 2], matrix[4 * i + 3] };
	}

	public void setRow(int i, double[] v) {
    matrixChanged = true;
    matrix[4 * i] = v[0];
		matrix[4 * i + 1] = v[1];
		matrix[4 * i + 2] = v[2];
		matrix[4 * i + 3] = v[3];
	}

	public double[] getColumn(int i) {
		return new double[] { matrix[i], matrix[i + 4], matrix[i + 8],
				matrix[i + 12] };
	}

  /**
   * assigns the values of the ith column with the values from v.
   * if v.length == 3, then the 4th entry of the column is set to 0.
   * @param i
   * @param v
   */
	public void setColumn(int i, double[] v) {
    matrixChanged = true;
		matrix[i] = v[0];
		matrix[i + 4] = v[1];
		matrix[i + 8] = v[2];
		matrix[i + 12] = (v.length > 3) ? v[3] : 0;
	}
    
	/**
	 * 
	 * @return reference to the current matrix
	 */
	public double[] getArray() {
		return matrix;
	}

	/**
	 * Copy the current matrix into <i>aMatrix</i> and return it.
	 * @param aMatrix
	 * @return  the filled in matrix
	 */
	public double[] writeToArray(double[] aMatrix) {
		if (aMatrix != null && aMatrix.length != 16)
			throw new IllegalArgumentException("matrix must have length 16");
		double[] copy = aMatrix == null ? new double[16] : aMatrix;
		System.arraycopy(matrix, 0, copy, 0, 16);
		return copy;
	}

	/**
	 * Let M be the current matrix. Then form the matrix product M*T and store it in M.
	 * 
	 * @param aMatrix
	 */
	public void multiplyOnRight(double[] T) {
    matrixChanged = true;
		Rn.times(matrix, matrix, T);
	}

	/**
	 * Let M be the current matrix. Then form the matrix product M*T and store it in M.
	 * 
	 * @param aMatrix
	 */
	public void multiplyOnRight(Matrix T) {
		multiplyOnRight(T.matrix);
	}

	/**
	 * Let M be the current matrix. Then form the matrix product T*M and store it in M.
	 * @param aMatrix
	 */
	public void multiplyOnLeft(double[] T) {
    matrixChanged = true;
		Rn.times(matrix, T, matrix);
	}

	/**
	 * Let M be the current matrix. Then form the matrix product T*M and store it in M.
	 * @param aMatrix
	 */
	public void multiplyOnLeft(Matrix T) {
		multiplyOnLeft(T.matrix);
	}

	/**
	 * Let M be the current Matrix.
	 * 
	 * assigns T * M * T^-1 
	 * 
	 * @param T
	 */
	public void conjugateBy(Matrix T) {
    matrixChanged = true;
		Rn.conjugateByMatrix(matrix, matrix, T.matrix);
	}

	public void add(Matrix T) {
    matrixChanged = true;
		Rn.add(matrix, matrix, T.matrix);
	}

	public Matrix getInverse() {
		return new Matrix(Rn.inverse(null, matrix));
	}

	public void invert() {
    matrixChanged = true;
		Rn.inverse(matrix, matrix);
	}

	public Matrix getTranspose() {
		return new Matrix(Rn.transpose(null, matrix));
	}

	public void transpose() {
    matrixChanged = true;
		Rn.transpose(matrix, matrix);
	}

  /**
   * TODO: !!! that doesn't return a rotation matrix!!!
   * TODO: this is implicitly euclidean.  I don't think it belongs in a "signature-neutral" class [charles gunn]
   * @return
   */
	public Matrix getRotation() {
	  Matrix ret = new Matrix(this);
      ret.setRow(3, new double[4]);
      ret.setColumn(3, new double[]{0,0,0,1});
      return ret;
    }
  
    public double[] multiplyVector(double[] vector) {
        return Rn.matrixTimesVector(null, matrix, vector);
    }
    
  public boolean equals(Matrix T) {
		return Rn.equals(matrix, T.matrix);
	}

  public String toString() {
    return Rn.matrixToString(matrix);
  }

}