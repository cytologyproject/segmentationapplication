package com.company;

import java.io.File;

//import org.opencv.core.*;

//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;

public class Main {

    static String ORIGIN_PATH = "./data/origin/";

    public static void main(String[] args) {
        File[] files = new File(ORIGIN_PATH).listFiles();
        for (File file : files) {
            System.out.println("Directory: " + file.getName());
        }
    }
}
