package pl.tajchert.smartmirror;

import android.app.Service;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class CameraWatcherService extends Service {

    private static final String TAG = "CameraWatcherService";

    private Camera camera;
    private Camera.Size size;
    private static int prevBrightness;
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

            ImageProcessingTask imageProcessingTask = new ImageProcessingTask(CameraWatcherService.this);
            imageProcessingTask.execute(new ImageCapturObject(data, size.width, size.height, CameraWatcherService.prevBrightness));

            final Handler h = new Handler();
            final Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    camera.addCallbackBuffer(buffer);
                }
            };
            h.postDelayed(r2, 500);
        }
    };

    public static void setPrevBrightness(Integer value) {
        CameraWatcherService.prevBrightness = value;
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
