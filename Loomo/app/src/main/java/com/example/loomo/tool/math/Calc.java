package com.example.loomo.tool.math;

public class Calc{
    /**
     * Return a float number whose absolute value is between max val and 0,
     * if it is over ,the return the uptter bound
     * @param up the upper bound
     * @param val value
     * @return
     */
    public static float clamp(float up, float val){
        return clamp(up, 0, val);
    }

    /**
     * Return a float number whose absolute value is between 2 bound,
     * if it is over ,the return the bound
     * @param up upper bound
     * @param down lower bound
     * @param val value
     * @return
     */
    public static float clamp(float up, float down, float val){
        if(Math.abs(val) > up){
            return  up;
        }else if(Math.abs(val) < down){
            return  down;
        }else {
            return  val;
        }
    }

}
