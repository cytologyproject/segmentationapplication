package com.company;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Main {


    static String ORIGIN_PATH = "./data/origin/";
    static String INTERMEDIATE_PATH = "./data/intermediate/";

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        File[] files = new File(ORIGIN_PATH).listFiles();
        for (File file : files) {
            System.out.println("Directory: " + file.getName());
            process(file.getName());
            break;
        }
    }

    public static void write_me(String prefix, String filename, Mat img){
        Imgcodecs.imwrite(INTERMEDIATE_PATH+prefix+"_"+filename, img);
    }

    public static void process(String filename){
        Mat init_img = Imgcodecs.imread(ORIGIN_PATH+filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        init_img.convertTo(init_img, CvType.CV_8UC4);
        write_me("init", filename, init_img);

        Mat img_gray = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Imgproc.cvtColor(init_img, img_gray, Imgproc.COLOR_RGBA2GRAY);
        write_me("gray", filename, img_gray);

        Mat img_thr = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Imgproc.threshold(img_gray, img_thr, 40, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        write_me("thr", filename, img_thr);

        Mat img_dist = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC4);
        Mat img_dist2 = new Mat(init_img.height(), init_img.width(), CvType.CV_32FC1);
        Mat img_dist_prt = new Mat(init_img.height(), init_img.width(), CvType.CV_32FC1);
        Imgproc.distanceTransform(img_thr, img_dist, Imgproc.DIST_L2, 3);
        Core.normalize(img_dist, img_dist2, 0, 1.0, Core.NORM_MINMAX);
        img_dist2.convertTo(img_dist_prt, CvType.CV_8UC1, 255);
        write_me("dist", filename, img_dist_prt);

        Mat img_thr2 = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC4);
        Mat img_thr2_prt = new Mat(init_img.height(), init_img.width(), CvType.CV_32FC1);
        Imgproc.threshold(img_dist2, img_thr2, 0.1, 1.0, Imgproc.THRESH_BINARY);
        img_thr2.convertTo(img_thr2_prt, CvType.CV_8UC1, 255);
        write_me("thr2", filename, img_thr2_prt);


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        img_thr2.convertTo(img_thr2, CvType.CV_32SC1);

        Imgproc.findContours(img_thr2, contours, new Mat(), Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);

        System.out.println(contours.size());
    }

}
