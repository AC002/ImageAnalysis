package mattman.cipher.imageanalysis;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by AlfaSqD on 2015-06-22.
 */
public class CannyClass {
    static final String TAG = "OpenCV CANNY";

    public Bitmap ImageSegmentation(Bitmap originalImage, int threshold) {

        originalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        //!
        Mat markers2 = new Mat();
        Mat imageMatRGBA = new Mat();
        Utils.bitmapToMat(originalImage, imageMatRGBA, false);
        Mat imageMatGrey = new Mat();
        Mat imageMatEdge = new Mat();

        Imgproc.cvtColor(imageMatRGBA, imageMatGrey, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.Canny(imageMatGrey,imageMatEdge,80,threshold);

        Imgproc.dilate(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
        Imgproc.erode(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));

        //Imgproc.floodFill(imageMatEdge, imageMatEdge,new Point(0,0),new Scalar(255,255,255));
        //Mat mask = new Mat( imageMatEdge.rows()+2, imageMatEdge.cols()+2, CvType.CV_8U, Scalar.all(0));
        //Imgproc.floodFill(imageMatEdge, imageMatEdge,new Point(0,0),new Scalar(255));

        //Imgproc.dilate(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
        //Imgproc.erode(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));
        //Imgproc.dilate(imageMatEdge, imageMatEdge, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

        // Finding background
       /* Mat bg = new Mat(imageMatEdge.size(),CvType.CV_8SC1);
        Imgproc.dilate(imageMatEdge,bg,new Mat(),new Point(-1,-1),3);
        Imgproc.threshold(bg,bg,1, 128,Imgproc.THRESH_BINARY_INV);
        */



        //Scalar colorDiff = Scalar.all(0);
        //imageMatRGBA.copyTo(colorDiff, imageMatEdge);

        Utils.matToBitmap(imageMatEdge,originalImage);
        return originalImage;
    }
}
