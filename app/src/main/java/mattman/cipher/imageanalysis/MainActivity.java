package mattman.cipher.imageanalysis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    // Few constants, used to make code more readable
    // "STATE MACHINE"
    public static final int TOOLBAR_HIDDEN = 0;
    public static final int TOOLBAR_SHOWED = 1;
    public static final int BOTTOMBAR_HIDDEN = 10;
    public static final int BOTTOMBAR_SHOWED = 11;

    public static final int STATE_ORIGINAL = 0;
    public static final int STATE_WATERSHED = 1;
    public static final int STATE_GRABCUT = 2;
    public static final int STATE_BINARY = 3;
    public static final int STATE_MEANSHIFT = 4;
    public static final int STATE_CANNY = 5;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 2;
    static int TOOLBAR_STATE = 1;
    static final String TAG = "MainActivity";

    // App state, means which option (watershed? grabcut?) is being used right now
    // Used in logic to deny menus
    int STATE = 0;

    // Binding all important things using the ButterKnife
    //@Bind(R.id.mTextViewThreshold) TextView mTextViewThreshold;
    //@Bind(R.id.cameraImageButton) ImageButton cameraImageButton;
    //@Bind(R.id.galleryImageButton) ImageButton galleryImageButton;
    @Bind(R.id.mImageView) ImageView mImageView;
    @Bind(R.id.seekBar) SeekBar seekBar;
    @Bind(R.id.fabPhoto) FloatingActionButton fabPhoto;
    @Bind(R.id.fabFolder) FloatingActionButton fabFolder;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.bottombar) Toolbar bottombar;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        // If resuming, refresh the states of images
        if(imageModified!=null) mImageView.setImageBitmap(imageModified);
        else if(imageOriginal!=null) mImageView.setImageBitmap(imageOriginal);

        super.onResume();
        // Also check OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    // Creating classes which are used to modify image
    WatershedClass watershedFunction = new WatershedClass();
    GrabCutClass grabcutFunction = new GrabCutClass();
    BinarizationClass binarizationFunction = new BinarizationClass();
    MeanShiftClass meanShiftFunction = new MeanShiftClass();
    CannyClass cannyFunction = new CannyClass();

    // For file saving so each is unique, date and time
    Calendar calendar = Calendar.getInstance();

    // Two important bitmaps, used to always hold 1 step back
    Bitmap imageOriginal, imageModified;
    // Standard threshold for binarization/canny/others
    int threshold = 128;
    // Special values for grabCut
    boolean grabCutBadSelection = false;
    int positionXStart = 0;
    int positionYStart = 0;
    int positionXEnd = 0;
    int positionYEnd = 0;

    @Override
    protected void onCreate(Bundle savedState) {
        // Load savestate, if rotated screen or something happened
        super.onCreate(savedState);
        if (savedState != null) {
            imageModified = savedState.getParcelable("modifiedBitmap");
            imageOriginal = savedState.getParcelable("originalBitmap");
        }

        setContentView(R.layout.activity_main);

        // Butterknife for easier View binding
        ButterKnife.bind(this);

        // Set toolbar
        setSupportActionBar(toolbar);

        seekBar.setEnabled(false);
        seekBar.setVisibility(View.VISIBLE);

        // Seekbar, to change threshold value
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // When user stops touching, the value is being used for current option
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (STATE == 1) {
                    mImageView.setImageBitmap(watershedFunction.ImageSegmentation(imageOriginal, threshold));
                } else if (STATE == 3) {
                    mImageView.setImageBitmap(binarizationFunction.ImageSegmentation(imageOriginal, threshold));
                } else if (STATE == 5) {
                    mImageView.setImageBitmap(cannyFunction.ImageSegmentation(imageOriginal, threshold));
                }
            }
        });

        // FAB button take photo
        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        // FAB button use selected image
        fabFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakeFromFileIntent();
            }
        });

/* //For future
        cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        galleryImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dispatchTakeFromFileIntent();
            }

        });*/

        // If image is pressed anywhere, use animation to hide toolbar and FAB buttons
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mImageView.getDrawable() && TOOLBAR_STATE == TOOLBAR_SHOWED){
                    animateToolbar(TOOLBAR_HIDDEN);
                    TOOLBAR_STATE = TOOLBAR_HIDDEN;
                    animateToolbar(BOTTOMBAR_HIDDEN);
                }
                else if (null!=mImageView.getDrawable() && TOOLBAR_STATE == 0){
                    animateToolbar(TOOLBAR_SHOWED);
                    TOOLBAR_STATE = TOOLBAR_SHOWED;
                    animateToolbar(BOTTOMBAR_SHOWED);
                }

            }
        });
    }

    // Function for animating toolbar based on state
    public void animateToolbar(int state){
        switch(state){

            case TOOLBAR_HIDDEN:
                toolbar.animate()
                        .translationY(-toolbar.getHeight())
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
                break;

            case TOOLBAR_SHOWED:
                toolbar.animate()
                        .translationY(0)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
                break;

            case BOTTOMBAR_HIDDEN:
                bottombar.animate()
                        .translationY(bottombar.getHeight() - 10)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
                fabPhoto.animate()
                        .translationY(bottombar.getHeight()+fabPhoto.getHeight()*2)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(250);
                fabFolder.animate()
                        .translationY(bottombar.getHeight()+fabPhoto.getHeight())
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(250);
                break;

            case BOTTOMBAR_SHOWED:
                bottombar.animate()
                        .translationY(0)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
                fabPhoto.animate()
                        .translationY(0)
                        .setInterpolator(new OvershootInterpolator())
                        .setDuration(250);
                fabFolder.animate()
                        .translationY(0)
                        .setInterpolator(new OvershootInterpolator())
                        .setDuration(250);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Part needed for grabcut - for now it is constant - ignore
        // int x = (int) event.getX();
        // int y = (int) event.getY();

        //int[] viewCoords = new int[2];
        //mImageView.getLocationOnScreen(viewCoords);
        //Log.i(TAG, "ViewCoords0: " + viewCoords[0]);
        //Log.i(TAG, "ViewCoords1: " + viewCoords[1]);

        // Additional for hiding bottombar with gesture
        /*int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //positionXStart = x - viewCoords[0];
                //positionYStart = y - viewCoords[1];
                Log.i(TAG, "PositionXS: " + positionXStart);
                Log.i(TAG, "PositionYS: " + positionYStart);
                Log.d(TAG,"Action was DOWN");
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                //positionXEnd = x - viewCoords[0];
                //positionYEnd = y - viewCoords[1];
                Log.i(TAG, "PositionXE: " + positionXEnd);
                Log.i(TAG, "PositionYE: " + positionYEnd);
                break;
        }*/
        /*if (positionXStart < 0 || positionXEnd > viewCoords[0] || positionYStart < 0 || positionYEnd > viewCoords[0]){
            grabCutBadSelection = true;
            Toast.makeText(this, "Bad selection!", Toast.LENGTH_SHORT).show();
        }
        else grabCutBadSelection = false;*/

        return true;
    }

    // Method for using smartphone camera app (selectable by user)
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // Method for using built in gallery to select image
    private void dispatchTakeFromFileIntent() {
        Intent takeFromFileIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        takeFromFileIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(takeFromFileIntent, "Select Picture"), PICK_IMAGE);
    }

    // Saving file to /DCIM/100ANDRO
    private void dispatchSaveFile(){
        mImageView.buildDrawingCache();
        Bitmap bm = mImageView.getDrawingCache();
        OutputStream fOut = null;
        Uri outputFileUri;
        try {
            // Change these lines if you want to change save position
            File root = Environment.getExternalStorageDirectory();
            File cachePath = new File(root.getAbsolutePath() + "/DCIM/100ANDRO/");
            File sdImageMainDirectory = new File(cachePath, "save"+getCurrentDateAndTime()+".jpg");

            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            fOut = new FileOutputStream(sdImageMainDirectory);

        } catch (Exception e) {
            Toast.makeText(this, "Error occured. Please try again later.",
                    Toast.LENGTH_SHORT).show();
        }

        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }
            Toast.makeText(this,"Image saved", Toast.LENGTH_SHORT).show();
    }

    private void showOriginalImage() {
        mImageView.setImageResource(0);
        mImageView.setImageBitmap(imageOriginal);
        seekBar.setEnabled(false);
        seekBar.setVisibility(View.VISIBLE);
    }

    private void useWatershed() {
        imageModified = watershedFunction.ImageSegmentation(imageOriginal, threshold);
        mImageView.setImageBitmap(imageModified);
        seekBar.setEnabled(true);
    }

    private void useGrabCut() {
        imageModified = grabcutFunction.ImageSegmentation(imageOriginal, positionXStart, positionYStart, positionXEnd, positionYEnd);
        mImageView.setImageBitmap(imageModified);
        seekBar.setEnabled(false);
    }

    private void useBinarization() {
        imageModified = binarizationFunction.ImageSegmentation(imageOriginal, threshold);
        mImageView.setImageBitmap(imageModified);
        seekBar.setEnabled(true);
    }

    private void useMeanShift() {
        imageModified = meanShiftFunction.ImageSegmentation(imageOriginal);
        mImageView.setImageBitmap(imageModified);
        seekBar.setEnabled(false);

    }

    private void useCanny() {
        imageModified = cannyFunction.ImageSegmentation(imageOriginal, threshold);
        mImageView.setImageBitmap(imageModified);
        seekBar.setEnabled(true);
    }

    // Method for getting the image made by camera from Intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageOriginal = imageBitmap;
            mImageView.setImageBitmap(imageBitmap);
        }

        // This wont work with too big images!
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                Toast.makeText(this, "Error while loading image!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            imageOriginal = imageBitmap;
            mImageView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Checking which option is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.photofromfile) {
            dispatchTakeFromFileIntent();
            return true;
        }

        if (id == R.id.watershed) {
            useWatershed();
            STATE = STATE_WATERSHED;
            return true;
        }

        if (id == R.id.grabcut) {
            useGrabCut();
            STATE = STATE_GRABCUT;
            return true;
        }

        if (id == R.id.binarization) {
            useBinarization();
            STATE = STATE_BINARY;
            return true;
        }

        if (id == R.id.meanshift) {
            useMeanShift();
            STATE = STATE_MEANSHIFT;
            return true;
        }

        if (id == R.id.canny) {
            useCanny();
            STATE = STATE_CANNY;
            return true;
        }

        if (id == R.id.originalimage) {
            Log.i(TAG, "ShowImage");
            STATE = STATE_ORIGINAL;
            showOriginalImage();
            imageModified=null;
            return true;
        }

        if (id == R.id.saveimage){
            Log.i(TAG,"SaveImage");
            dispatchSaveFile();
            return true;
        }

        if (id == R.id.overwritebase){
            Log.i(TAG,"NewImage");
            if(imageModified!=null){
                imageOriginal = imageModified;
            }
            // Trying to overwrite the original image while not seeing the modified one
            // Disabling it so it wont confuse the user
            else {
                Toast.makeText(this,"Sorry I can't do that Dave",Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Modifing the menus (disable 2 to 8) (should make it dynamic, maybe later)
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean boolEnable;

        if (imageOriginal != null) {
            boolEnable = true;
        } else {
            boolEnable = false;
        }
        for (int i = 2; i <= 8; i++) {
            menu.getItem(i).setEnabled(boolEnable);
        }

        if (grabCutBadSelection) menu.getItem(4).setEnabled(false);
        return true;
    }

    // Function for getting date and time to save files
    public String getCurrentDateAndTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
        String str = dateFormat.format(date);
        return str;
    }

    // Saving state (saving image if device rotated, who wants to load it twice?
    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
        toSave.putParcelable("modifiedBitmap", imageModified);
        toSave.putParcelable("originalBitmap", imageOriginal);
    }
}