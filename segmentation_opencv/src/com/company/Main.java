package com.company;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.NORM_MINMAX;

public class Main {


    static String ORIGIN_PATH = "./data/origin/";
    static String INTERMEDIATE_PATH = "./data/intermediate/";

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        File[] files = new File(ORIGIN_PATH).listFiles();
        for (File file : files) {
            System.out.println("Directory: " + file.getName());
            process(file.getName());
            //break;
        }
    }

    public static void write_me(String prefix, String filename, Mat img){
        Imgcodecs.imwrite(INTERMEDIATE_PATH+prefix+"_"+filename, img);
    }

    public static void process(String filename){
        Mat init_img = Imgcodecs.imread(ORIGIN_PATH+filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        init_img.convertTo(init_img, CvType.CV_8UC4);
        write_me("init", filename, init_img);


        Mat kernel = Mat.ones(3, 3, CvType.CV_32F);
        kernel.put(1,1,-8);

        Mat mLaplacian = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mLaplacian2 = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mLaplacian_prt = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat sharp = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mSubstr = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mSubstr2 = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mSubstr_prt = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);

        Imgproc.filter2D(init_img, mLaplacian, CvType.CV_32FC4, kernel);

        init_img.convertTo(sharp,CvType.CV_32FC4);
        Core.subtract(sharp, mLaplacian, mSubstr);

        Core.normalize(mSubstr, mSubstr2, 0,1,NORM_MINMAX);
        mSubstr2.convertTo(mSubstr_prt, CvType.CV_8UC4,255);
        write_me("substr", filename, mSubstr_prt);



        Mat img_gray = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Imgproc.cvtColor(mSubstr_prt, img_gray, Imgproc.COLOR_RGBA2GRAY);
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
        //Imgproc.threshold(img_dist2, img_thr2, 0.1, 1.0, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(img_dist_prt, img_thr2, 10, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 111, 0);
        Imgproc.erode(img_thr2,img_thr2, Mat.ones(3, 3, CvType.CV_8U));
        img_thr2.convertTo(img_thr2_prt, CvType.CV_8UC1, 255);
        write_me("thr2", filename, img_thr2_prt);


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        img_thr2.convertTo(img_thr2, CvType.CV_32SC1);

        Imgproc.findContours(img_thr2, contours, new Mat(), Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);

        int real_count = 0;

        for(MatOfPoint contour : contours){
            real_count += Imgproc.contourArea(contour) > 500 ? 1 : 0;
        }

        System.out.println(real_count);

    }

}
