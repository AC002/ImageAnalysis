package mattman.cipher.imageanalysis;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by AlfaSqD on 2015-06-21.
 */
public class BinarizationClass {

    static final String TAG = "OpenCV BINARIZATION";

    public Bitmap ImageSegmentation(Bitmap originalImage, int threshold) {

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
}
