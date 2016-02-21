package mattman.cipher.imageanalysis;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by TranCipher on 2015-06-20.
 */
public class WatershedClass {

    static final String TAG = "OpenCV WATERSHED";

    public Bitmap ImageSegmentation(Bitmap originalImage, int threshold){


        originalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        //!
        Mat markers2 = new Mat();
        Bitmap safeOriginalImage = originalImage;

        // Creating two Mats, for RGBA and RGB
        Mat imageMatRGBA = new Mat();
        Utils.bitmapToMat(safeOriginalImage, imageMatRGBA, false);
        Mat imageMatRGB = new Mat();
        Imgproc.cvtColor(imageMatRGBA, imageMatRGB, Imgproc.COLOR_RGBA2RGB);

        // Creating new Mat for binary image
        Mat binaryChannel = new Mat(imageMatRGB.size(), CvType.CV_8SC1);

        // Creating a binary image
        Imgproc.cvtColor(imageMatRGB, binaryChannel, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(binaryChannel, binaryChannel, threshold, 255, Imgproc.THRESH_BINARY);

        // Finding foreground
        Mat fg = new Mat(binaryChannel.size(), CvType.CV_8SC1);
        Imgproc.erode(binaryChannel,fg,new Mat(),new Point(-1,-1),2);

        // Finding background
        Mat bg = new Mat(binaryChannel.size(),CvType.CV_8SC1);
        Imgproc.dilate(binaryChannel,bg,new Mat(),new Point(-1,-1),3);
        Imgproc.threshold(bg,bg,1, 128,Imgproc.THRESH_BINARY_INV);

        Mat markers = new Mat(imageMatRGB.size(),CvType.CV_8SC1, new Scalar(0));
        Core.add(fg, bg, markers);

        Log.i(TAG, "Setting markers...");

        //!
        markers.convertTo(markers2, CvType.CV_32SC1);

        Imgproc.watershed(imageMatRGB, markers2);
        markers2.convertTo(markers2, CvType.CV_8U);

        Utils.matToBitmap(markers2,originalImage);
        Bitmap bitmap;
        bitmap = originalImage;

        binaryChannel.release();
        markers.release();
        bg.release();
        fg.release();
        imageMatRGBA.release();
        imageMatRGB.release();
        markers2.release();

        return bitmap;
    }
}
