package com.example.opencvproject1;

import android.Manifest;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Uri imageUri;
    Bitmap grayBitmap, imageBitmap;
    Button btnTakePic;
    String pathToFile;

    Mat src, dst = new Mat();
    Mat kernel = new Mat();
    int ddepth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTakePic = findViewById(R.id.btnTakePic);
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchPictureTakeAction();
            }
        });
        imageView = (ImageView)findViewById(R.id.imageView);

        OpenCVLoader.initDebug();

    }

    public void openGallery(View v) {
        Intent myIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(myIntent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK && data!=null)
        {
            imageUri = data.getData();

            try
            {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

            imageView.setImageBitmap(imageBitmap);
        }

    }

    private void dispatchPictureTakeAction() {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePic.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createPhotoFile();

            if (photoFile != null) {
                pathToFile = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this, "com.example.opencvproject1.fileprovider", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePic, 1);

            }
        }
    }

    private File createPhotoFile() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageView = null;
        try {
            imageView = File.createTempFile(name, ".jpg", storageDir);
        } catch (IOException e) {
            Log.d("mylog", "Excep : " + e.toString());
        }
        return imageView;
    }

    public void convertToGray(View v)
    {
        Mat Rgba = new Mat();
        Mat grayMat = new Mat();

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither = false;
        o.inSampleSize=4;

        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();

        grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);


        //Bitmap to MAT

        Utils.bitmapToMat(imageBitmap, Rgba);

        Imgproc.cvtColor(Rgba, grayMat, Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(grayMat, grayBitmap);


        imageView.setImageBitmap(grayBitmap);
    }

    /* public static void filter2D(Mat src, Mat dst, int ddepth, Mat kernel) {

        src = Imgcodecs.imread("0001.png", Imgcodecs.IMREAD_COLOR);

        Mat kernel = Mat.ones( 3, 3, CvType.CV_32F );
        kernel.put(1,1, -8);

        filter2D(src, src, CvType.CV_32F, kernel);


        float[][] imgResult = sharp - imgLaplacian

    }
    */



}
