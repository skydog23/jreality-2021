/**
 * JavaScript port of jReality's Pn class.
 * Static methods for n-dimensional real projective space RP^n.
 * Points and vectors are represented in homogeneous coordinates by arrays of length n+1.
 */

import { Rn } from './Rn.js';

// Metric constants
const ELLIPTIC = 1;
const EUCLIDEAN = 0;
const HYPERBOLIC = -1;
const PROJECTIVE = 2;

// Standard direction vectors
const zDirectionP3 = [0.0, 0.0, 1.0, 0.0];

/**
 * Hyperbolic trigonometric functions
 */
function cosh(x) {
    return 0.5 * (Math.exp(x) + Math.exp(-x));
}

function sinh(x) {
    return 0.5 * (Math.exp(x) - Math.exp(-x));
}

function tanh(x) {
    return sinh(x) / cosh(x);
}

function acosh(x) {
    return Math.log((x > 0 ? x : -x) + Math.sqrt(x * x - 1));
}

function asinh(x) {
    return Math.log(x + Math.sqrt(x * x + 1));
}

function atanh(x) {
    return 0.5 * Math.log((1 + x) / (1 - x));
}

/**
 * Calculate the angle between two points with respect to the given metric
 * @param {number[]} u - First point in homogeneous coordinates
 * @param {number[]} v - Second point in homogeneous coordinates
 * @param {number} metric - One of HYPERBOLIC, EUCLIDEAN, ELLIPTIC
 * @returns {number} The angle between the points
 */
function angleBetween(u, v, metric) {
    const uu = innerProductPlanes(u, u, metric);
    const vv = innerProductPlanes(v, v, metric);
    const uv = innerProductPlanes(u, v, metric);
    
    if (uu === 0 || vv === 0) {
        return Number.MAX_VALUE; // error: infinite distance
    }
    
    let f = uv / Math.sqrt(Math.abs(uu * vv));
    f = Math.max(-1.0, Math.min(1.0, f));
    return Math.acos(f);
}

/**
 * Dehomogenize a point or vector
 * @param {number[]} dst - Destination array (can be null)
 * @param {number[]} src - Source array in homogeneous coordinates
 * @returns {number[]} The dehomogenized coordinates
 */
function dehomogenize(dst, src) {
    const sl = src.length;
    if (!dst) dst = new Array(sl);
    const dl = dst.length;
    
    if (dl !== sl && dl + 1 !== sl) {
        throw new Error('Invalid dimensions');
    }
    
    const last = src[sl - 1];
    if (last === 1.0 || last === 0.0) {
        if (src !== dst) {
            for (let i = 0; i < dl; i++) dst[i] = src[i];
        }
        return dst;
    }
    
    const factor = 1.0 / last;
    for (let i = 0; i < dl; i++) {
        dst[i] = factor * src[i];
    }
    if (dl === sl) dst[dl - 1] = 1.0;
    
    return dst;
}

/**
 * Calculate the inner product of two vectors with respect to the given metric
 * @param {number[]} u - First vector
 * @param {number[]} v - Second vector
 * @param {number} metric - One of HYPERBOLIC, EUCLIDEAN, ELLIPTIC
 * @returns {number} The inner product
 */
function innerProduct(u, v, metric) {
    const n = Math.min(u.length, v.length) - 1;
    let sum = 0;
    
    for (let i = 0; i < n; i++) {
        sum += u[i] * v[i];
    }
    
    switch (metric) {
        case HYPERBOLIC:
            return sum - u[n] * v[n];
        case EUCLIDEAN:
            return sum;
        case ELLIPTIC:
            return sum + u[n] * v[n];
        default:
            return sum;
    }
}

/**
 * Calculate the inner product of two planes with respect to the given metric
 * @param {number[]} u - First plane
 * @param {number[]} v - Second plane
 * @param {number} metric - One of HYPERBOLIC, EUCLIDEAN, ELLIPTIC
 * @returns {number} The inner product
 */
function innerProductPlanes(u, v, metric) {
    return innerProduct(u, v, -metric);
}

/**
 * Normalize a vector with respect to the given metric
 * @param {number[]} dst - Destination array (can be null)
 * @param {number[]} src - Source vector
 * @param {number} metric - One of HYPERBOLIC, EUCLIDEAN, ELLIPTIC
 * @returns {number[]} The normalized vector
 */
function normalize(dst, src, metric) {
    const norm = Math.sqrt(Math.abs(innerProduct(src, src, metric)));
    if (norm === 0) return Rn.times(dst, 0, src);
    return Rn.times(dst, 1/norm, src);
}

// Export all functions and constants
export const Pn = {
    // Constants
    ELLIPTIC,
    EUCLIDEAN,
    HYPERBOLIC,
    PROJECTIVE,
    zDirectionP3,
    
    // Functions
    cosh,
    sinh,
    tanh,
    acosh,
    asinh,
    atanh,
    angleBetween,
    dehomogenize,
    innerProduct,
    innerProductPlanes,
    normalize
}; 