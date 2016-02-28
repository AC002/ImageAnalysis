package mattman.cipher.imageanalysis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Rect;
import org.opencv.core.Point;

import butterknife.Bind;

/**
 * Created by TranCipher on 2015-06-21.
 */
public class GrabCutClass extends AsyncTask<Void, Void, Bitmap>{

    static final String TAG = "OpenCV GRABCUT";
    Activity mainActivity;
    Bitmap originalImage;
    int startXPos, startYPos, endXPos, endYPos;

    public GrabCutClass(Activity mainActivity, Bitmap bitmapOriginal, int positionXStart, int positionYStart, int positionXEnd, int positionYEnd){
        this.mainActivity = mainActivity;
        this.originalImage = bitmapOriginal;
        startXPos = positionXStart;
        startYPos = positionYStart;
        endXPos = positionXEnd;
        endYPos = positionYEnd;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.showDialog(1);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        mainActivity.removeDialog(1);
        Toast.makeText(mainActivity, "Obliczono!", Toast.LENGTH_SHORT).show();
        ImageView imageView;
        imageView = (ImageView) mainActivity.findViewById(R.id.mImageView);
        imageView.setImageBitmap(result);
    }

    @Override
    protected Bitmap doInBackground(Void... arg0) {
        try {
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

                Point p1 = new Point (startXPos, startYPos);
                Point p2 = new Point (endXPos, endYPos);
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
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
