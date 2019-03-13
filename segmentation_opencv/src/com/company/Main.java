package com.company;

import java.io.File;

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
        }
    }

    public static void write_me(String prefix, String filename, Mat img){
        Imgcodecs.imwrite(INTERMEDIATE_PATH+prefix+filename, img);
    }

    public static void process(String filename){
        Mat init_img = Imgcodecs.imread(ORIGIN_PATH+filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        write_me("init", filename, init_img);

    }

}
