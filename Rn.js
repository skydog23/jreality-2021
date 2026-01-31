/**
 * JavaScript port of jReality's Rn class.
 * Static methods for n-dimensional Euclidean vector space R^n.
 * All vectors are represented as JavaScript arrays.
 */

const TOLERANCE = 1e-8;

// Identity matrices cache (1x1 to 4x4)
const identityMatrices = new Array(5);

// Initialize identity matrices
for (let i = 1; i < 5; i++) {
    identityMatrices[i] = identityMatrix(i);
}

/**
 * Create an identity matrix of given dimension
 * @param {number} dim - The dimension of the matrix
 * @returns {number[]} The identity matrix as a flat array
 */
function identityMatrix(dim) {
    const result = new Array(dim * dim).fill(0);
    for (let i = 0; i < dim; i++) {
        result[i * dim + i] = 1;
    }
    return result;
}

/**
 * Add two vectors
 * @param {number[]} dst - Destination array (can be null)
 * @param {number[]} src1 - First source vector
 * @param {number[]} src2 - Second source vector
 * @returns {number[]} The sum vector
 */
function add(dst, src1, src2) {
    if (!dst) dst = new Array(src1.length);
    const n = Math.min(dst.length, src1.length, src2.length);
    for (let i = 0; i < n; i++) {
        dst[i] = src1[i] + src2[i];
    }
    return dst;
}

/**
 * Subtract two vectors
 * @param {number[]} dst - Destination array (can be null)
 * @param {number[]} src1 - First source vector
 * @param {number[]} src2 - Second source vector
 * @returns {number[]} The difference vector
 */
function subtract(dst, src1, src2) {
    if (!dst) dst = new Array(src1.length);
    const n = Math.min(dst.length, src1.length, src2.length);
    for (let i = 0; i < n; i++) {
        dst[i] = src1[i] - src2[i];
    }
    return dst;
}

/**
 * Calculate the inner product of two vectors
 * @param {number[]} u - First vector
 * @param {number[]} v - Second vector
 * @returns {number} The inner product
 */
function innerProduct(u, v) {
    const n = Math.min(u.length, v.length);
    let sum = 0;
    for (let i = 0; i < n; i++) {
        sum += u[i] * v[i];
    }
    return sum;
}

/**
 * Calculate the Euclidean norm of a vector
 * @param {number[]} vec - Input vector
 * @returns {number} The Euclidean norm
 */
function euclideanNorm(vec) {
    return Math.sqrt(euclideanNormSquared(vec));
}

/**
 * Calculate the squared Euclidean norm of a vector
 * @param {number[]} vec - Input vector
 * @returns {number} The squared Euclidean norm
 */
function euclideanNormSquared(vec) {
    return innerProduct(vec, vec);
}

/**
 * Multiply a vector by a scalar
 * @param {number[]} dst - Destination array (can be null)
 * @param {number} factor - Scalar factor
 * @param {number[]} src - Source vector
 * @returns {number[]} The scaled vector
 */
function times(dst, factor, src) {
    if (!dst) dst = new Array(src.length);
    const n = Math.min(dst.length, src.length);
    for (let i = 0; i < n; i++) {
        dst[i] = factor * src[i];
    }
    return dst;
}

/**
 * Normalize a vector to unit length
 * @param {number[]} dst - Destination array (can be null)
 * @param {number[]} src - Source vector
 * @returns {number[]} The normalized vector
 */
function normalize(dst, src) {
    const norm = euclideanNorm(src);
    if (norm === 0) return times(dst, 0, src);
    return times(dst, 1/norm, src);
}

/**
 * Matrix multiplication with a vector (M * v)
 * @param {number[]} dst - Destination array (can be null)
 * @param {number[]} m - Matrix as flat array
 * @param {number[]} src - Source vector
 * @returns {number[]} The resulting vector
 */
function matrixTimesVector(dst, m, src) {
    const dim = Math.sqrt(m.length);
    if (!Number.isInteger(dim)) {
        throw new Error('Matrix must be square');
    }
    if (!dst) dst = new Array(dim);
    
    for (let i = 0; i < dim; i++) {
        let sum = 0;
        for (let j = 0; j < dim; j++) {
            sum += m[i * dim + j] * (j < src.length ? src[j] : (j === dim-1 ? 1 : 0));
        }
        dst[i] = sum;
    }
    return dst;
}

// Export all functions
export const Rn = {
    TOLERANCE,
    identityMatrix,
    add,
    subtract,
    innerProduct,
    euclideanNorm,
    euclideanNormSquared,
    times,
    normalize,
    matrixTimesVector
}; 