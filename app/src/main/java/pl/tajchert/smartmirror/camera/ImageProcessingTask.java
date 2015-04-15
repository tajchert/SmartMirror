package pl.tajchert.smartmirror.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import de.greenrobot.event.EventBus;
import pl.tajchert.smartmirror.SmartMirrorApplication;
import pl.tajchert.smartmirror.events.MotionCustomEvent;
import pl.tajchert.smartmirror.ui.MainActivity;

/**
 * Created by tajchert on 12.04.15.
 */
public class ImageProcessingTask extends AsyncTask<ImageCaptureObject, ImageCaptureObject, ImageCaptureObject> {
    private static final String TAG = "ImageProcessingTask";
    private Context context;
    private static final int COLOR_THREADSHOLD = 5;
    private static final int COLOR_SIGNIFICANT_THREADSHOLD = 25;

    public ImageProcessingTask(Context context) {
        this.context = context;
    }

    @Override
    protected ImageCaptureObject doInBackground(ImageCaptureObject... params) {
        if(params == null || params.length == 0 || params[0] == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageCaptureObject capture = params[0];

        YuvImage yuvImage = new YuvImage(capture.imageByteArray, ImageFormat.NV21, capture.width, capture.height, null);
        yuvImage.compressToJpeg(new Rect(0, 0, capture.width, capture.height), 50, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        calculateBrightness(image, capture);
        capture.colorRedChangeVal = Math.abs(CameraWatcherService.colorValuePrevRed - capture.colorValueRed);
        capture.colorGreenChangeVal = Math.abs(CameraWatcherService.colorValuePrevGreen - capture.colorValueGreen);
        capture.colorBlueChangeVal = Math.abs(CameraWatcherService.colorValuePrevBlue - capture.colorValueBlue);

        capture.colorRedChangeValLongTerm = Math.abs(CameraWatcherService.colorValuePrevRedLongTerm - capture.colorValueRed);
        capture.colorGreenChangeValLongTerm = Math.abs(CameraWatcherService.colorValuePrevGreenLongTerm - capture.colorValueGreen);
        capture.colorBlueChangeValLongTerm = Math.abs(CameraWatcherService.colorValuePrevBlueLongTerm - capture.colorValueBlue);

        CameraWatcherService.setBrightnessPrev(capture.colorValueRed, capture.colorValueGreen, capture.colorValueBlue);
        return capture;
    }

    @Override
    protected void onPostExecute(ImageCaptureObject capture) {
        if(capture == null) {
            return;
        }

        if(capture.colorRedChangeVal >  COLOR_SIGNIFICANT_THREADSHOLD ||capture.colorGreenChangeVal >  COLOR_SIGNIFICANT_THREADSHOLD || capture.colorBlueChangeVal >  COLOR_SIGNIFICANT_THREADSHOLD) {

            Log.d(TAG, "onPostExecute r:" + capture.colorRedChangeVal);
            Log.d(TAG, "onPostExecute g:" + capture.colorGreenChangeVal);
            Log.d(TAG, "onPostExecute b:" + capture.colorBlueChangeVal);
            activateApp(true);
        } else if(capture.colorRedChangeValLongTerm >  COLOR_SIGNIFICANT_THREADSHOLD ||capture.colorGreenChangeValLongTerm >  COLOR_SIGNIFICANT_THREADSHOLD || capture.colorBlueChangeValLongTerm >  COLOR_SIGNIFICANT_THREADSHOLD) {

            CameraWatcherService.setBrightnessLongTerm(capture.colorValueRed, capture.colorValueGreen, capture.colorValueBlue);
            Log.d(TAG, "onPostExecute long r:" + capture.colorRedChangeVal);
            Log.d(TAG, "onPostExecute long g:" + capture.colorGreenChangeVal);
            Log.d(TAG, "onPostExecute long b:" + capture.colorBlueChangeVal);
            activateApp(true);
        } else if(capture.colorRedChangeVal >  COLOR_THREADSHOLD ||capture.colorGreenChangeVal >  COLOR_THREADSHOLD || capture.colorBlueChangeVal >  COLOR_THREADSHOLD) {

            Log.d(TAG, "onPostExecute r:" + capture.colorRedChangeVal);
            Log.d(TAG, "onPostExecute g:" + capture.colorGreenChangeVal);
            Log.d(TAG, "onPostExecute b:" + capture.colorBlueChangeVal);
            activateApp(false);
        } else if(capture.colorRedChangeValLongTerm >  COLOR_THREADSHOLD ||capture.colorGreenChangeValLongTerm >  COLOR_THREADSHOLD || capture.colorBlueChangeValLongTerm >  COLOR_THREADSHOLD) {

            CameraWatcherService.setBrightnessLongTerm(capture.colorValueRed, capture.colorValueGreen, capture.colorValueBlue);
            Log.d(TAG, "onPostExecute long r:" + capture.colorRedChangeVal);
            Log.d(TAG, "onPostExecute long g:" + capture.colorGreenChangeVal);
            Log.d(TAG, "onPostExecute long b:" + capture.colorBlueChangeVal);
            activateApp(false);
        }
        super.onPostExecute(capture);
    }

    private void activateApp(boolean isSignificant) {
        if(SmartMirrorApplication.isActivityVisible()){
            EventBus.getDefault().post(new MotionCustomEvent(isSignificant));
        } else {
            Intent intentRun = new Intent(context, MainActivity.class);
            intentRun.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentRun);
        }
    }


    public void calculateBrightness(Bitmap bitmap, ImageCaptureObject capture) {
        int redColors = 0;
        int greenColors = 0;
        int blueColors = 0;
        int pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                redColors += Color.red(c);
                greenColors += Color.green(c);
                blueColors += Color.blue(c);
            }
        }
        capture.colorValueRed = redColors/pixelCount;
        capture.colorValueGreen = greenColors/pixelCount;
        capture.colorValueBlue = blueColors/pixelCount;
        //Log.d(TAG, "calculateBrightness r:" + capture.colorValueRed + ",g:" + capture.colorValueGreen +",b:" + capture.colorValueBlue);
    }
}
