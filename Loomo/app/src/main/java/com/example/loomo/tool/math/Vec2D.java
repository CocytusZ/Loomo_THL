package com.example.loomo.tool.math;

/**
 * Class to encapsulate functions to tackle math in 2D Vector calculation
 * Use degree as the unit of radian
 */

/**
 * Class of a 2d Vector, which is the basic component of this file
 *
 * @param {number} _x
 * @param {number} _y
 */
public class Vec2D {
    public double x, y;

    public Vec2D(){};

    public Vec2D(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double mag() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    /**
     * Normalize this 2D vector
     */
    public Vec2D normalize() {
        double mag = this.mag();

        this.x = (this.x / mag);
        this.y = (this.y / mag);

        return this;
    }

    /**
     * Check whether two vectors equal
     *
     * @param {Vec2D} rhs
     * @returns {boolean}
     */
    public boolean equals(Vec2D rhs) {
        return (rhs.x == this.x && rhs.y == this.y);
    }

    /**
     * Function to get coordinate in a String
     */
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }

    /**
     * Function to get the angle between positive Y-axis and this vector
     *
     * @returns
     */
    public double heading() {
        return Math.atan2(this.y, this.x);
    }

    /**
     * Function to add another vector
     *
     * @param {Vec2D} rhs
     */
    public void add(Vec2D rhs) {
        this.x += rhs.x;
        this.y += rhs.y;
    }

    /**
     * Function to subtract another vector from this
     *
     * @param {Vec2D} rhs
     */
    public void sub(Vec2D rhs) {
        this.x -= rhs.x;
        this.y -= rhs.y;
    }

    /**
     * Function to scale this vector with a coefficient
     *
     * @param {number} val
     */
    public void scale(double val) {
        this.x *= val;
        this.y *= val;
    }


    /**
     * Function to change x and y, when the magnitude is bigger than a certain value
     * so that the magnitude will be this value,
     *
     * @param {number} max the max magnitude for this vector
     */
    public void limit(double max) {
        double mag = this.mag();

        if (mag > max && max != 0) {
            this.x *= max / mag;
            this.y *= max / mag;
        }
    }

    /**
     * Rotate the vector by an degree angle
     *
     * @param {number} radian angle in radian
     */
    public void rotate(double radian) {
        double angle = radianToDegree(radian);

        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);

        this.x = (this.x * cosA - this.y * sinA);
        this.y = (this.x * sinA + this.y * cosA);
    }


    /* *****************************************************************
     *                                                                 *
     *   The following method is static method for Vector calculation  *
     *                                                                 *
     *******************************************************************/

    /**
     * Function to get dot product with another vector
     * @param {Vec2D} rhs
     * @returns {number}
     */
    public static double dot(Vec2D lhs, Vec2D rhs) {
        return lhs.x * rhs.x + lhs.y * rhs.y;
    }

    /**
     * Function to get cross product with another vector
     * @param {Vec2D} rhs
     * @returns {number}
     */
    public static double cross(Vec2D lhs, Vec2D rhs) {
        return lhs.x * rhs.y - lhs.y * rhs.x;
    }

    /**
     * Function to show the result of subtract one vector from another
     *
     * @param {Vec2D} lhs
     * @param {Vec2D} rhs
     * @returns {Vec2D} the result in Vec2D
     */
    public static Vec2D sub(Vec2D lhs, Vec2D rhs) {
        return new Vec2D(lhs.x - rhs.x, lhs.y - rhs.y);
    }

    /**
     * Function to get the distance between two 2D point
     * keep 3 decimal place
     *
     * @param {Vec2D} lhs
     * @param {Vec2D} rhs
     * @returns {number}
     */
    public static double dist(Vec2D lhs, Vec2D rhs) {
        double deltX = lhs.x - rhs.x;
        double deltY = lhs.y - rhs.y;
        return Math.sqrt(deltX * deltX + deltY * deltY);
    }

    /**
     * Calc the angle between two vector, vary from ( - Pi to Pi)
     *
     * @param {Vec2D} lhs
     * @param {Vec2D} rhs
     * @returns {number} angle in radian
     */
    public static double includeAngle(Vec2D lhs, Vec2D rhs) {
        return Math.acos(dot(lhs, rhs) / (lhs.mag() * rhs.mag()));
    }

    /**
     * Convert angle from degree to radian
     *
     * @param {number} degree angle in degree
     * @returns angle in radian
     */
    public static double degreeToRadian(double degree) {
        return degree * Math.PI / 180;
    }

    /**
     * Convert angle from radian to degree
     *
     * @param {number} radian angle in radian
     * @returns angle in degree
     */
    public static double radianToDegree(double radian) {
        return radian / Math.PI * 180;
    }
}

