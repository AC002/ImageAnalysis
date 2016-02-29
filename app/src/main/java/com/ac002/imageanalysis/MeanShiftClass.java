package com.ac002.imageanalysis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Random;

import mattman.ac002.imageanalysis.R;

/**
 * Created by AlfaSqD on 2015-06-21.
 */
public class MeanShiftClass extends AsyncTask<Void, Void, Bitmap>{

    static final String TAG = "OpenCV MEANSHIFT";
    Activity mainActivity;
    Bitmap originalImage;

    public MeanShiftClass(Activity main, Bitmap bitmap){
        this.mainActivity = main;
        this.originalImage = bitmap;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(Bitmap result) {
        ProgressBar progressbar;
        progressbar = (ProgressBar) mainActivity.findViewById(R.id.progressBar);
        progressbar.setVisibility(View.GONE);
        ImageView imageView;
        imageView = (ImageView) mainActivity.findViewById(R.id.mImageView);
        imageView.setImageBitmap(result);
    }

    @Override
    protected Bitmap doInBackground(Void... params) {

            originalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

            // Creating two Mats, for RGBA and RGB
            Mat imageMatRGBA = new Mat();
            Utils.bitmapToMat(originalImage, imageMatRGBA, false);
            Mat imageMatRGB = new Mat();
            Imgproc.cvtColor(imageMatRGBA, imageMatRGB, Imgproc.COLOR_RGBA2RGB);
            Imgproc.pyrMeanShiftFiltering(imageMatRGB,imageMatRGB,10,20);

            int size = (int) (imageMatRGB.total() * imageMatRGB.channels());
            byte[] temp = new byte[size];

            Random rand = new Random();
            Scalar colorDiff;

            Log.i(TAG, "Creating mask...");

            // Something wrong with MeanShift segmentation, it works to the point of MEAN.
            // Leaving this for now
       /* Mat mask = new Mat( imageMatRGB.rows()+2, imageMatRGB.cols()+2, CvType.CV_8U, colorDiff = Scalar.all(1));
        Log.i(TAG, "Entering y for...");

        for( int y = 0; y < imageMatRGB.rows(); y++ ) {
            Log.i(TAG, "Entering x for...");
            for (int x = 0; x < imageMatRGB.cols(); x++) {

                Log.i(TAG, "Entering if...");
                if( mask.get(y+1, x+1, temp) == 0)
                {
                    Mat grayRnd = new Mat(2, 1, CvType.CV_8U);
                    Core.randu(grayRnd, 0, 256);
                    Log.i(TAG, "Creating scalar (randomize)...");
                    Scalar newVal = new Scalar( rand.nextInt(255 - 1 + 1)+1, rand.nextInt(255 - 1 + 1)+1, rand.nextInt(255 - 1 + 1)+1 );
                    Log.i(TAG, "Floodfilling...");
                    Imgproc.floodFill(imageMatRGB, mask, new Point(x, y), newVal, new Rect(new Point(0, 0), new Point(50, 50)), colorDiff, colorDiff, Imgproc.FLOODFILL_FIXED_RANGE);
                }
            }
        }*/

            Utils.matToBitmap(imageMatRGB,originalImage);
            //Utils.matToBitmap(mask,originalImage);
            return originalImage;
        }
    }


