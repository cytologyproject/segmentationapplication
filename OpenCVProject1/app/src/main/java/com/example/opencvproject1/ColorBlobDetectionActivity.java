package com.example.opencvproject1;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;

import static org.opencv.imgproc.Imgproc.COLORMAP_COOL;
import static org.opencv.imgproc.Imgproc.COLORMAP_HSV;
import static org.opencv.imgproc.Imgproc.COLORMAP_JET;
import static org.opencv.imgproc.Imgproc.COLORMAP_PARULA;
import static org.opencv.imgproc.Imgproc.COLORMAP_RAINBOW;
import static org.opencv.imgproc.Imgproc.COLORMAP_SUMMER;

import static org.opencv.core.Core.NORM_MINMAX;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Mat                  mGray;
    private Mat                  mResult;
    private Mat                  mForOutput;

    private Mat                  mCurrentFrame;
    private Mat                  mPreviousFrame;

    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    private Mat kernel;
    private Mat kernel2;


    private Mat mTreshold;
    private Mat mDist;

    private Mat mTreshold2;
    private Mat mDilated;
    private Mat image32S;

    private List<Integer> circledBuffer;
    private int circledBufferPointer;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        circledBuffer = new ArrayList<Integer>(200);
        circledBufferPointer = 0;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mResult = new Mat(height, width, CvType.CV_8UC1);
        mForOutput = new Mat(height, width, CvType.CV_8UC1);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);

        //mCurrentFrame = new Mat(height, width, CvType.CV_8UC1);
        //mPreviousFrame = new Mat(height, width, CvType.CV_8UC1);

        //Kernels
        kernel = Mat.ones(3, 3, CvType.CV_32F);
        kernel.put(1, 1, -8);
        kernel2 = Mat.ones(3, 3, CvType.CV_8U);

        //Intermediate results


        mTreshold = new Mat(mResult.height(), mResult.width(), CvType.CV_8UC1);
        mDist = new Mat(mResult.height(), mResult.width(), CvType.CV_32FC1);

        mTreshold2 = new Mat(mResult.height(), mResult.width(), CvType.CV_32FC1);
        mDilated = new Mat(mResult.height(), mResult.width(), CvType.CV_32FC1);

        image32S = new Mat();
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE, 0, 0, Imgproc.INTER_LINEAR_EXACT);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }


    public static int process2(Mat img_gray) {

        Imgproc.adaptiveThreshold(img_gray, img_gray, 10, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 131, 0);
        Imgproc.erode(img_gray, img_gray, Mat.ones(5, 5, CvType.CV_8U));


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        img_gray.convertTo(img_gray, CvType.CV_32SC1);

        Imgproc.findContours(img_gray, contours, new Mat(), Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);

        int real_count = 0;

        for(MatOfPoint contour : contours){
            double size_ = Imgproc.contourArea(contour);
            if (size_>6000){real_count += 0;}
            else if (size_>4800){real_count += 4;}
            else if (size_>3600){real_count += 3;}
            else if (size_>2400){real_count += 2;}
            else if (size_>1200){real_count += 1;}
            else if (size_<=1200){real_count += 0;} //38*38
        }

        return real_count;
    }

    public static int process(Mat init_img) {
        init_img.convertTo(init_img, CvType.CV_8UC4);

        Mat img_gray = new Mat(init_img.height(), init_img.width(), CvType.CV_8UC1);
        Imgproc.cvtColor(init_img, img_gray, Imgproc.COLOR_RGBA2GRAY);

        Imgproc.threshold(img_gray, img_gray, 110, 255, Imgproc.THRESH_BINARY);


        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();

        Imgproc.findContours(img_gray, cnts, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        MatOfPoint cntWithBiggestArea = cnts.get(0);
        double biggestArea = Imgproc.contourArea(cnts.get(0));
        for (MatOfPoint contour : cnts) {
            if (Imgproc.contourArea(contour) > biggestArea) {
                cntWithBiggestArea = contour;
                biggestArea = Imgproc.contourArea(contour);
            }
        }

        Rect rect = Imgproc.boundingRect(cntWithBiggestArea);

        Mat resizedImage = new Mat(1024, 1024, CvType.CV_8UC1);
        Imgproc.resize(new Mat(img_gray, rect), resizedImage, new Size(1024, 1024));

        int number = process2(resizedImage);
        return number;
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        int number = 0;

        try {
            System.gc();
            number = process(mRgba);

            int number_average = 0;

            for (int val : circledBuffer){
                number_average+=val;
            }
            number_average = number_average / circledBuffer.size();

            Imgproc.putText(
                    mRgba,                          // Matrix obj of the image
                    "Number of cellS: " + (int) number_average + " " + (int) number,          // Text to be added
                    new Point(50, 150),               // point
                    Core.FONT_HERSHEY_SIMPLEX,      // front face
                    3,                               // front scale
                    new Scalar(255, 255, 255),             // Scalar object for color
                    22);                               // Thickness

            mCurrentFrame = mRgba;
        }
        catch(Exception e)
        {
            mCurrentFrame = mPreviousFrame;
        }
        finally
        {
            mPreviousFrame = mCurrentFrame;
            circledBuffer.set(circledBufferPointer, number);
            circledBufferPointer++;
            if (circledBufferPointer >= circledBuffer.size()){
                circledBufferPointer=0;
            }
        }



        return mCurrentFrame;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);


        return new Scalar(pointMatRgba.get(0, 0));
    }




}
