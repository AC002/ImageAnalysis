package com.ac002.imageanalysis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import mattman.ac002.imageanalysis.R;

public class CannyClass extends AsyncTask<Void, Void, Bitmap> {

    Activity mainActivity;
    Bitmap originalImage;
    int threshold;

    public CannyClass(Activity main, Bitmap bitmap, int thresholdDesired) {
        this.mainActivity = main;
        this.originalImage = bitmap;
        this.threshold = thresholdDesired;
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
    
        Mat markers2 = new Mat();
        Mat imageMatRGBA = new Mat();
        Utils.bitmapToMat(originalImage, imageMatRGBA, false);
        Mat imageMatGrey = new Mat();
        Mat imageMatEdge = new Mat();

        Imgproc.cvtColor(imageMatRGBA, imageMatGrey, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.Canny(imageMatGrey, imageMatEdge, 80, threshold);

        Imgproc.dilate(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
        Imgproc.erode(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));


        /* Here is the segmentation part
        //Imgproc.floodFill(imageMatEdge, imageMatEdge,new Point(0,0),new Scalar(255,255,255));
        //Mat mask = new Mat( imageMatEdge.rows()+2, imageMatEdge.cols()+2, CvType.CV_8U, Scalar.all(0));
        //Imgproc.floodFill(imageMatEdge, imageMatEdge,new Point(0,0),new Scalar(255));

        //Imgproc.dilate(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
        //Imgproc.erode(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));
        //Imgproc.dilate(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

        // Finding background
        Mat bg = new Mat(imageMatEdge.size(),CvType.CV_8SC1);
        Imgproc.dilate(imageMatEdge,bg,new Mat(),new Point(-1,-1),3);
        Imgproc.threshold(bg,bg,1, 128,Imgproc.THRESH_BINARY_INV);
        */

        //Scalar colorDiff = Scalar.all(0);
        //imageMatRGBA.copyTo(colorDiff, imageMatEdge);

        Utils.matToBitmap(imageMatEdge, originalImage);
        return originalImage;
    }
}
