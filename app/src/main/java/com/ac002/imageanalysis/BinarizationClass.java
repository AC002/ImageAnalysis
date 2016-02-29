package com.ac002.imageanalysis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import mattman.ac002.imageanalysis.R;

/**
 * Created by AC002 on 2015-06-21.
 */
public class BinarizationClass extends AsyncTask<Void, Void, Bitmap> {

    static final String TAG = "OpenCV BINARIZATION";
    Activity mainActivity;
    Bitmap originalImage;
    int threshold;

    public BinarizationClass(Activity main, Bitmap bitmap, int threshold){
        this.mainActivity = main;
        this.originalImage = bitmap;
        this.threshold = threshold;
    }

    @Override
    protected void onPreExecute(){
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        originalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        Mat imageMatRGBA = new Mat();
        Utils.bitmapToMat(originalImage, imageMatRGBA, false);

        // Creating new Mat for binary image
        Mat binaryChannel = new Mat(imageMatRGBA.size(), CvType.CV_8SC1);

        // Creating a binary image
        Imgproc.cvtColor(imageMatRGBA, binaryChannel, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(binaryChannel, binaryChannel, threshold, 255, Imgproc.THRESH_BINARY);

        Bitmap bitmap = originalImage;
        Utils.matToBitmap(binaryChannel,bitmap);

        return bitmap;
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
}
