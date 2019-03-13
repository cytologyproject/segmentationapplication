package com.example.opencvproject1;

import android.media.Image;

import org.junit.Test;



import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;



import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private Mat                  mRgba;
    private Mat                  mResult;
    private Mat                  mForOutput;


    @Test
    public void SimpleTest(){
        Mat result = this.process();

    }

    public Mat process(Mat input) {

        Mat output;

        Mat kernel = Mat.ones(3, 3, CvType.CV_32F);
        kernel.put(1,1,8);

        Mat kernel2 = Mat.ones(3, 3, CvType.CV_8U);

        Mat mLaplacian = new Mat(mResult.height(), mResult.width(), CvType.CV_8UC1);
        Mat mSubstract = new Mat(mResult.height(), mResult.width(), CvType.CV_8UC1);
        Mat mTreshold = new Mat(mResult.height(), mResult.width(), CvType.CV_8UC1);
        Mat mDist = new Mat(mResult.height(), mResult.width(), CvType.CV_32FC1);

        Mat mTreshold2 = new Mat(mResult.height(), mResult.width(), CvType.CV_32FC1);
        Mat mDilated = new Mat(mResult.height(), mResult.width(), CvType.CV_32FC1);

        Mat mCountours = new Mat(mResult.height(), mResult.width(), CvType.CV_8UC1);

        Imgproc.cvtColor(mRgba , mResult, Imgproc.COLOR_RGBA2GRAY);
        //Imgproc.filter2D(mResult, mLaplacian, -1, kernel);
        //Core.subtract(mResult, mLaplacian, mSubstract);
        Imgproc.threshold(mResult , mTreshold, 40,255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.distanceTransform(mTreshold, mDist, Imgproc.DIST_L2, 3);
        Core.normalize(mDist, mDist, 0, 1.0, Core.NORM_MINMAX);
        Imgproc.threshold(mDist, mTreshold2, 0.4, 1.0, Imgproc.THRESH_BINARY);

        Imgproc.dilate(mTreshold2, mDilated, kernel2);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat image32S = new Mat();
        mDilated.convertTo(image32S, CvType.CV_32SC1);

        Imgproc.findContours(image32S, contours, new Mat(), Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);

        Imgproc.putText (
                mResult,                          // Matrix obj of the image
                "Number of cells: "+7*(int)contours.size(),          // Text to be added
                new Point(50, 150),               // point
                Core.FONT_HERSHEY_SIMPLEX ,      // front face
                4,                               // front scale
                new Scalar(255, 255, 255),             // Scalar object for color
                22                                // Thickness
        );

        mResult.convertTo(mForOutput, CvType.CV_8UC1);

        return output;

    }



    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);


        return new Scalar(pointMatRgba.get(0, 0));
    }




}
