package com.company;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.opencv.core.*;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.NORM_MINMAX;

public class Main {


    //static String ORIGIN_PATH = "./data/origin/";
    //static String ORIGIN_PATH = "/home/alexander/Загрузки/png-20190313T094648Z-001/png/";
    static String ORIGIN_PATH = "/home/alexander/Загрузки/png2/";
    static String INTERMEDIATE_PATH = "./data/intermediate/";

    public static void main(String[] args) {

        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            //File[] files = new File(ORIGIN_PATH).listFiles();
            Path[] files = Files.list(Paths.get(ORIGIN_PATH)).sorted().toArray(Path[]::new);
            for (Path file : files) {
                //System.out.println("File: " + file.getFileName().toString());
                process(file.getFileName().toString());
                //break;
                System.gc();
            }
        }catch(Exception e){}
    }

    public static void write_me(String prefix, String filename, Mat img){
        Imgcodecs.imwrite(INTERMEDIATE_PATH+prefix+"_"+filename, img);
    }

    public static void process2(String filename, Mat img_gray) {

        Mat img_thr2 = new Mat(img_gray.height(), img_gray.width(), CvType.CV_8UC4);
        Mat img_thr2_prt = new Mat(img_gray.height(), img_gray.width(), CvType.CV_32FC1);
        //Imgproc.threshold(img_dist2, img_thr2, 0.1, 1.0, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(img_gray, img_thr2, 10, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 131, 0);
        Imgproc.erode(img_thr2,img_thr2, Mat.ones(5, 5, CvType.CV_8U));
        img_thr2.convertTo(img_thr2_prt, CvType.CV_8UC1, 255);
        write_me("thr2", filename, img_thr2_prt);


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        img_thr2.convertTo(img_thr2, CvType.CV_32SC1);

        Imgproc.findContours(img_thr2, contours, new Mat(), Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);

        int real_count = 0;

        for(MatOfPoint contour : contours){
            //System.out.println(Imgproc.contourArea(contour));
            double size_ = Imgproc.contourArea(contour);
            if (size_>6000){real_count += 0;}
            else if (size_>4800){real_count += 4;}
            else if (size_>3600){real_count += 3;}
            else if (size_>2400){real_count += 2;}
            else if (size_>1200){real_count += 1;}
            else if (size_<=1200){real_count += 0;} //38*38
        }

        //Imgproc.contour


        System.out.println(real_count);
    }


    public static void process(String filename){
        Mat init_img = Imgcodecs.imread(ORIGIN_PATH+filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        init_img.convertTo(init_img, CvType.CV_8UC4);
        write_me("init", filename, init_img);


        Mat kernel = Mat.ones(3, 3, CvType.CV_32F);
        kernel.put(1,1,-8);

        Mat kernel2 = Mat.ones(3, 3, CvType.CV_32F);

        Mat mLaplacian = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mLaplacian2 = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mLaplacian_prt = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat sharp = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mSubstr = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mSubstr2 = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat mSubstr_prt = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);


/*




        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        List<MatOfPoint> key = new ArrayList<MatOfPoint>();
        int c;
*/



/*
        Imgproc.filter2D(init_img, mLaplacian, CvType.CV_32FC4, kernel);

        init_img.convertTo(sharp,CvType.CV_32FC4);
        Core.subtract(sharp, mLaplacian, mSubstr);


        Core.normalize(mSubstr, mSubstr2, 0,1,NORM_MINMAX);
        mSubstr2.convertTo(mSubstr_prt, CvType.CV_8UC4,255);
        write_me("substr", filename, mSubstr_prt);

*/

        Mat img_gray = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Imgproc.cvtColor(init_img, img_gray, Imgproc.COLOR_RGBA2GRAY);

        write_me("gray", filename, img_gray);



        Mat img_thr = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Imgproc.threshold(img_gray, img_thr, 110, 255, Imgproc.THRESH_BINARY);
        //Imgproc.threshold(img_gray, img_thr, 40, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        write_me("thr", filename, img_thr);




        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        List<MatOfPoint> key = new ArrayList<MatOfPoint>();
        Mat thresh_erode = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat thresh_erode2 = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat thresh_dilate = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Mat thresh_dilate2 = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);



        Imgproc.findContours(img_thr, cnts, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //System.out.println(cnts.size());

        MatOfPoint cntWithBiggestArea = cnts.get(0);
        double biggestArea = Imgproc.contourArea(cnts.get(0));
        for(MatOfPoint contour : cnts){
            if (Imgproc.contourArea(contour) > biggestArea){
                cntWithBiggestArea = contour;
                biggestArea = Imgproc.contourArea(contour);
            }
        }

        Rect rect = Imgproc.boundingRect(cntWithBiggestArea);
        Mat croppedImage = new Mat(img_gray, rect);

        write_me("crop", filename, croppedImage);

        Mat resizedImage = new Mat(1024, 1024, CvType.CV_8UC1);
        Imgproc.resize(croppedImage, resizedImage,  new Size(1024,1024));

        process2(filename, resizedImage);
        return;

    }



}
