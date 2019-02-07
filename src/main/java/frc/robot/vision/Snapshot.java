/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.vision;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.RobotMap;

public class Snapshot {
    private static final int FPS = 15; //frames per second 
    public static final int IMG_WIDTH = 160;
    public static final int IMG_HEIGHT = 120;
    private static final int TELEOP_EXPOSURE = 15;
    private static final int VISION_EXPOSURE = 4; //TODO: test exposure 
    private static UsbCamera camera1 = null;
    private static CvSink cvSink1 = null;
    private static GripPipeline pipeline;
    public static boolean isImageReady = false;
    public static boolean isVisionCommandEnabled = false;
    private static ArrayList<Rect> rectangles = new ArrayList<Rect>();
    static final Scalar WHITE = new Scalar(255, 255, 255);

    private static int lastCount = 0;

    private static final Mat image = new Mat();

public enum WhichSensor {
    lefty,
    middley,
    righty,
    noney
    }
    
public static void cameraInit() {
    camera1 = CameraServer.getInstance().startAutomaticCapture(0);
    camera1.setResolution(IMG_WIDTH, IMG_HEIGHT);
    camera1.setFPS(FPS);
    camera1.setExposureManual(VISION_EXPOSURE); //TODO
    cvSink1 = CameraServer.getInstance().getVideo(camera1);
    cvSink1.setEnabled(true);

    visionFilterStreamer();
    }

private static void visionFilterStreamer() {
    Thread switcherThreader = new Thread(() -> {
    CvSource outputStream = CameraServer.getInstance().putVideo("Contours", IMG_WIDTH, IMG_HEIGHT);
    while(!Thread.interrupted()) {
        if(!isVisionCommandEnabled) {
            Timer.delay(.1);
            continue;
        }
        
        synchronized(image) {
            long framestatus = cvSink1.grabFrame(image);  

            if(framestatus == 0) continue;
        }
    
        processImage();

        outputStream.putFrame(image);

        System.gc();
    }
    });

    switcherThreader.start();

}


public static void processImage() {
    pipeline.process(image);

    ArrayList<MatOfPoint> contours = pipeline.filterContoursOutput();

    synchronized(rectangles) {
        rectangles.clear();

        int c = contours.size();
        for(int x = 0; x < c; x++) {
            Rect r = Imgproc.boundingRect(contours.get(x));
            rectangles.add(r);
            Imgproc.rectangle(image, new Point(r.x, r.y), new Point(r.x+r.width, r.y+r.height), WHITE, 5);       
        }

        if(c != lastCount && isVisionCommandEnabled) {
            System.out.println(" Pipeline Contains " + lastCount);
            lastCount = c;
        }

        isImageReady = true;
    }

}

public static ArrayList<Rect> getContours() {
    synchronized(rectangles) {
        isImageReady = false;
        return(ArrayList<Rect>) rectangles.clone();
    }
}



public static WhichSensor getLineStatus() {
    WhichSensor currentPosition; 

    if(RobotMap.leftLightSensor.get()) {
        currentPosition = WhichSensor.lefty;
    }

    else if(RobotMap.rightLightSensor.get()) {
        currentPosition = WhichSensor.righty;
    }
    
    else if(RobotMap.middleLightSensor.get()) {
        currentPosition = WhichSensor.middley;
    }

    else {
        currentPosition = WhichSensor.noney;
    }

   /* SmartDashboard.putBoolean("Left Sensor", RobotMap.leftLightSensor.get());
    SmartDashboard.putBoolean("Right Sensor", RobotMap.rightLightSensor.get());
    SmartDashboard.putBoolean("Middle Sensor", RobotMap.middleLightSensor.get()); */



    return currentPosition;
}

}
