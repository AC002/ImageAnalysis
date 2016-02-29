package com.ac002.imageanalysis;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
<<<<<<< HEAD:app/src/main/java/com/ac002/imageanalysis/GrabCutClass.java
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
=======
>>>>>>> parent of c595adf... AsyncTask Test:app/src/main/java/mattman/cipher/imageanalysis/GrabCutClass.java

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Rect;
import org.opencv.core.Point;

<<<<<<< HEAD:app/src/main/java/com/ac002/imageanalysis/GrabCutClass.java
import mattman.ac002.imageanalysis.R;

=======
>>>>>>> parent of c595adf... AsyncTask Test:app/src/main/java/mattman/cipher/imageanalysis/GrabCutClass.java
/**
 * Created by TranCipher on 2015-06-21.
 */
public class GrabCutClass {

    static final String TAG = "OpenCV GRABCUT";

<<<<<<< HEAD:app/src/main/java/com/ac002/imageanalysis/GrabCutClass.java
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
=======
    public Bitmap ImageSegmentation(Bitmap originalImage,int positionXStart,int positionYStart,int positionXEnd,int positionYEnd) {
>>>>>>> parent of c595adf... AsyncTask Test:app/src/main/java/mattman/cipher/imageanalysis/GrabCutClass.java

        originalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

        //GrabCut part
        Mat img = new Mat();
        Utils.bitmapToMat(originalImage, img);
        boolean debug = false;

        Rect rect;
        if(debug == false){
            int r = img.rows();
            int c = img.cols();
            Point p1 = new Point(c/10, r/10);
            Point p2 = new Point(c-c/10, r-r/10);
            rect = new Rect(p1, p2);
        }
        else{

            Point p1 = new Point (positionXStart, positionYStart);
            Point p2 = new Point (positionXEnd, positionYEnd);
            rect = new Rect(p1, p2);

        }
        Log.d(TAG, "rect: " + rect);

        Mat mask = new Mat();
        Mat fgdModel = new Mat();
        Mat bgdModel = new Mat();

        Mat imgC3 = new Mat();
        Imgproc.cvtColor(img, imgC3, Imgproc.COLOR_RGBA2RGB);

        Log.d(TAG, "Grabcut begins");
        Imgproc.grabCut(imgC3, mask, rect, bgdModel, fgdModel, 2, Imgproc.GC_INIT_WITH_RECT);
        Log.d(TAG, "Grabcut ends");

        Core.convertScaleAbs(mask, mask, 100, 0);
        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2RGBA);

        Log.d(TAG, "Convert to Bitmap");

        Utils.matToBitmap(mask, originalImage);
        Bitmap bitmap;
        bitmap = originalImage;

        img.release();
        imgC3.release();
        mask.release();
        fgdModel.release();
        bgdModel.release();

        return bitmap;
    }
}
