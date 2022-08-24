package com.example.loomo.slam.pos;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.tf.AlgoTfData;
import com.segway.robot.algo.tf.Quaternion;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.perception.sensor.Sensor;

/**
 * @date 04.22.2022
 * @author Zhouyao
 * This class is used to collect Extrinsic Parameters of camera, which include:
 *      1. Position and translation of robot body
 *      2. Position of robot head
 */
public class Extrinsic {
    private String TAG = "SLAM Extrinsic Param";

    private Base base;
    private Sensor sensor;

    public class Pos{
        public double x;
        public double y;
        public double z;
        public Quaternion r;

        public String toString(){
            return  String.valueOf(x) + " " +
                    String.valueOf(y) + " " +
                    String.valueOf(z) + " " +
                    String.valueOf(r.x) + " " +
                    String.valueOf(r.y) + " " +
                    String.valueOf(r.z) + " " +
                    String.valueOf(r.w);
        }
    }



    public Extrinsic(){
        base = Base.getInstance();
        sensor = Sensor.getInstance();
    }

    public Pos getPos(){
        Pos pos = new Pos();

        Pose2D bodyPos = base.getOdometryPose(-1);
        AlgoTfData headPos = sensor.getTfData(Sensor.PLATFORM_CAM_FRAME, Sensor.BASE_ODOM_FRAME, -1, 100);

        pos.x = bodyPos.getX() + headPos.t.x;
        pos.y = bodyPos.getY() + headPos.t.y;
        pos.z = headPos.t.z;

        Quaternion bodyRotate = new Quaternion(0, 0, 0, 0);
        bodyRotate.setEulerRad(bodyPos.getTheta(), 0, 0);
        pos.r = headPos.q.mul(bodyRotate);

        return pos;
    }


}
