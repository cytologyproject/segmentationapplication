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

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Mat                  mGray;
    private Mat                  mResult;
    private Mat                  mForOutput;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

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

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();


        /*if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);

            Imgproc.threshold(mRgba , mResult, 127,255, Imgproc.THRESH_BINARY_INV);
            Imgproc.cvtColor(mRgba , mResult, Imgproc.COLOR_RGBA2GRAY);
        }*/

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

// Draw all the contours such that they are filled in.
        //Mat contourImg = new Mat(image32S.size(), image32S.type());
        //for (int i = 0; i < contours.size(); i++) {
        //    Imgproc.drawContours(contourImg, contours, i, new Scalar(255, 255, 255), -1);
        //}

        //Imgproc.circle(contourImg, (5,5), 3, new Scalar(255, 255, 255), -1);
        //Imgproc.watershed(mResult, contourImg);


        //contourImg.convertTo(mForOutput, CvType.CV_8UC1, 255.0);
        mResult.convertTo(mForOutput, CvType.CV_8UC1);


        //Core.normalize(mTreshold, mTreshold, 0, 255, Core.NORM_MINMAX);

        //Core.normalize(mDist, mGray, 0, 255.0, CvType.CV_8UC1);

        //Imgproc.cvtColor(mRgba , mResult, Imgproc.COLOR_RGBA2GRAY);
        //Mat bin_image = new Mat();
        //Imgproc.threshold(mResult , mResult, 127,255, Imgproc.THRESH_BINARY_INV);

        //Mat dist_image = new Mat();
        //Imgproc.distanceTransform(mResult, mResult, Imgproc.DIST_L2, 3);


        //Core.normalize(dist_image, mResult, 0, 1.0, Core.NORM_MINMAX);

        //Imgproc.threshold(, 0.4, 1.0, Imgproc.THRESH_BINARY);




        return mForOutput;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);


        return new Scalar(pointMatRgba.get(0, 0));
    }

}