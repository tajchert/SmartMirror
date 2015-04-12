package pl.tajchert.smartmirror;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import pl.tajchert.smartmirror.events.MotionCustomEvent;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class CameraWatcherService extends Service {

    private static final String TAG = "CameraWatcherService";

    private Camera camera;
    private Camera.Size size;
    private int prevBrightness;
    private byte[] buffer;
    private SurfaceTexture texture;

    private static SurfaceTexture getTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0]);
        return new SurfaceTexture(textures[0]);
    }

    @Override
    public void onCreate() {
        
        try{
            super.onCreate();
            startRecording();
        } catch(Exception ex){
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "============Destroying CameraWatcherService");
        stopRecording();
        if(camera != null){
            camera.release();
        }
        super.onDestroy();
    }

    public void startRecording() {
        if (camera == null) {
            try {
                camera = openFrontFacingCameraGingerbread();
                Camera.Parameters parameters = camera.getParameters();
                //parameters.setPreviewFpsRange(0, 10);
                //parameters.setPreviewFormat(ImageFormat.RGB_565);
                camera.setParameters(parameters);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                ex.printStackTrace();
                return;
            }
            if( camera == null ){
                Log.e(TAG, "Camera is null despite trying to allocate it. Stopping Service");
                throw new IllegalStateException("DeltaMonitor was unable to allocate the camera.");
            }
        }

        try {
            Log.i(TAG, "==================Beginning to Record");

            if (buffer == null) {
                Camera.Parameters parameters = CameraSizer.sizeUp(camera);
                size = parameters.getPreviewSize();
                buffer = new byte[size.height * size.width * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
            }
            camera.addCallbackBuffer(buffer);

            if (texture == null) {
                texture = getTexture();
            }
            camera.setPreviewTexture(texture);
            camera.setPreviewCallbackWithBuffer(previewCallback);
            camera.startPreview();

        } catch (IOException ex) {
            Log.e(TAG, "IOException during recording setup " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            if( camera != null){
                camera.stopPreview();
                camera.setPreviewCallbackWithBuffer(null);
                camera.setPreviewCallback(null);
                camera.setPreviewTexture(null);
            }
            texture = null; //TODO: This is a patch for a bug (SurfaceTexture has been abandoned)
        } catch (IOException ex) {
            Log.e(TAG, "IOException during recording setdown " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {

            if (data == null) {
                return;
            }
            if (size == null) {
                return;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 50, out);
            byte[] imageBytes = out.toByteArray();
            Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            int currentBrigthness = calculateBrightness(image);
            Log.d(TAG, "onPreviewFrame current: " + currentBrigthness);
            Log.d(TAG, "onPreviewFrame prev: " + prevBrightness);
            if(Math.abs(prevBrightness - currentBrigthness) >  5) {
                if(SmartMirrorApplication.isActivityVisible()){
                    EventBus.getDefault().post(new MotionCustomEvent());
                } else {
                    Intent intentRun = new Intent(CameraWatcherService.this, MainActivity.class);
                    intentRun.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentRun);
                }
            }

            prevBrightness = currentBrigthness;
            final Handler h = new Handler();
            final Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    camera.addCallbackBuffer(buffer);
                }
            };
            h.postDelayed(r2, 2000);
        }
    };


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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }

}
