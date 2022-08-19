package com.example.loomo.slam.tool;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.segway.robot.sdk.vision.frame.Frame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Zhouyao
 * @Date: 2022/4/27
 *
 * @Description:
 * This is a class of a PGM file
 * PGM16 means each pixel is expressed by 16 bits
 *
 * The format of PGM file is as followed:
 * --------------------------------------------------------------------------------------------------------
 * 一个PGM文件由一个或多个PGM图象组成. 在多副图象之间,之前或之后没有任何数据存在. 每一个PGM 图像由以下部分组成:
 *
 * - 一个"magic number",它是用来标定文件格式的. pgm的magic number是"P5".
 * - 空白区域(空格,TAB,回车,换行)
 * - 图象宽, 十进制ASCII码
 * - 空白区域
 * - 图象高, 十进制ASCII码
 * - 空白区域
 * - 最大灰度值(Maxval), 十进制ASCII码.必须小于65536
 * - 新一行或另一个空白区域符
 * - 空栅化的一副宽*高的灰度值图象. 每个灰度值取值是从0到Maxval, 0表示黑,而Maxval表示白.每个灰度值用1-2个字节表示.如果
 *      Maxval小于256,那行就是一个字节,否则为两个字节.最大字节(most significant byte)开始
 * - 每个灰度值是表示当前象素值在CIE Rec.709伽马校正之后的密度值.
 * - 一个很普遍的PGM格式替代格式就是线性灰度值(不经过伽马校正), pnmgamma以这样的一个pgm文件为输入,输出一个标准的pgm文件
 * - #符号之后的一行为注释行,会被省略.
 * - 每个象素的光栅值表示为一个十进制ASCII码值.
 * - 每个象素的光栅值前后各有一个空格. 这样两个象素之间将有大于等于1个空格.
 * - 每行不超过70个字.
 *
 */
public class PGM16 extends File {
    private static String TAG = "PGM_16";

    private int width,height;

    public PGM16(@NonNull String pathname, int width, int height) {
        super(pathname);
        this.width = width;
        this.height = height;

        try {
            FileWriter fw = new FileWriter(this, true);
            fw.write("P2\r\n"); //p2 use String, p5 use byte
            fw.write(String.valueOf(width) + " " + String.valueOf(height) + "\r\n");
            fw.write("65535");
            // Input space to separate image and header
            fw.write(32); // "space"
            fw.write(13); // "Enter"

            fw.flush();
            fw.close();
        }catch (IOException ex){
            ex.printStackTrace();
            Log.e(TAG, "Create failed");
        }
    }

    public void setImageContent(Bitmap bitmap){
        try {
            FileWriter fw = new FileWriter(this, true);

            for(int row = 0; row < height; row++){
                for(int col = 0; col < width; col++){
                    int p = bitmap.getPixel(col, row) & 0x00ffffff;
                    fw.write(String.valueOf(p));
                    fw.write(32); //"Space"
                }
                fw.write(13);
            }

            fw.flush();
            fw.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

//    public void setImageContent(Frame frame){
//        ByteBuffer buffer = frame.getByteBuffer();
//        Bitmap bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565);
//        bitmap.copyPixelsFromBuffer(buffer);
//        buffer.clear();
//
//        try {
//            FileWriter fw = new FileWriter(this,true);
//            for(int row = 0; row < this.height; row++){
//                for(int col = 0; col < this.width; col++){
//                    int index = row * this.width + col;
//                    fw.write(bitmap.getPixel(col, row));
//                    fw.write(32);
//                }
//                fw.write(13);
//            }
//
//            fw.flush();
//            fw.close();
//        }catch (Exception ex){
//            ex.printStackTrace();
//            Log.e(TAG, "setImageContent: ERROR" );
//        }
//    }


}
