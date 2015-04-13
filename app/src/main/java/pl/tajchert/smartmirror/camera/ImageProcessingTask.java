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
import pl.tajchert.smartmirror.ui.MainActivity;
import pl.tajchert.smartmirror.SmartMirrorApplication;
import pl.tajchert.smartmirror.events.MotionCustomEvent;

/**
 * Created by tajchert on 12.04.15.
 */
public class ImageProcessingTask extends AsyncTask<ImageCaptureObject, ImageCaptureObject, ImageCaptureObject> {
    private static final String TAG = "ImageProcessingTask";
    private Context context;

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
        int currentBrigthness = calculateBrightness(image);
        capture.brightnessCurrent = currentBrigthness;
        capture.brightnessChangeTemporary = Math.abs(CameraWatcherService.getBrightnessPrev() - currentBrigthness);
        capture.brightnessChangeLongterm = Math.abs(CameraWatcherService.getBrightnessLastChanged() - currentBrigthness);
        CameraWatcherService.setBrightnessPrev(currentBrigthness);
        return capture;
    }

    @Override
    protected void onPostExecute(ImageCaptureObject capture) {
        if(capture == null) {
            return;
        }

        Log.d(TAG, "onPreviewFrame current: " + capture.brightnessCurrent);
        Log.d(TAG, "onPreviewFrame changeTemporary: " + capture.brightnessChangeTemporary);
        Log.d(TAG, "onPreviewFrame changeLongterm: " + capture.brightnessChangeLongterm);

        if(capture.brightnessChangeTemporary >  2 || capture.brightnessChangeLongterm > 2) {
            CameraWatcherService.setBrightnessLastChanged(capture.brightnessCurrent);
            if(SmartMirrorApplication.isActivityVisible()){
                EventBus.getDefault().post(new MotionCustomEvent());
            } else {
                Intent intentRun = new Intent(context, MainActivity.class);
                intentRun.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentRun);
            }
        }
        super.onPostExecute(capture);
    }


    public int calculateBrightness(Bitmap bitmap) {
        int redColors = 0;
        int greenColors = 0;
        int blueColors = 0;
        int pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight(); y++)
        {
            for (int x = 0; x < bitmap.getWidth(); x++)
            {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                redColors += Color.red(c);
                greenColors += Color.green(c);
                blueColors += Color.blue(c);
            }
        }
        // calculate average of bitmap r,g,b values
        int red = (redColors/pixelCount);
        int green = (greenColors/pixelCount);
        int blue = (blueColors/pixelCount);
        return (int) Math.round(0.2126*red + 0.7152*green + 0.0722*blue);
    }
}
