package com.example.loomo.slam.tool;

/**
 * @author Zhouyao
 * @Date: 2022/5/19
 * @Description:
 *  Enum class used by handler in slam to mark the content of image:
 */
public class ImageInfo {

    public static final int SAVE_COMPLETE = 0;

    /**
     * COLOR : rgb image
     */
    public static final int COLOR = 1;

    /**
     * DEPTH : depth image
     */
    public static final int DEPTH = 2;

    /**
     * FILTERED_DEPTH : depth image filtered by Frame filter
     */
    public static final int FILTERED_DEPTH = 3;




    public static final int DEPTH_WIDTH = 320;
    public static final int DEPTH_HEIGHT = 240;
    public static final int COLOR_WIDTH = 640;
    public static final int COLOR_HEIGHT = 480;

}
