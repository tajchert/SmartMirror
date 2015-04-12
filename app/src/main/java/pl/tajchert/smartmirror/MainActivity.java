package pl.tajchert.smartmirror;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import pl.droidsonroids.gif.GifImageView;
import pl.tajchert.smartmirror.events.MotionCustomEvent;


public class MainActivity extends ActionBarActivity {
    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    TextView textView;
    GifImageView gifDecoderView;
    final Handler h = new Handler();


    final Runnable runnableTurnOff = new Runnable() {
        @Override
        public void run() {
            gifDecoderView.setVisibility(View.INVISIBLE);
            WindowManager.LayoutParams params = getWindow().getAttributes();
            /** Turn off: */
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            params.screenBrightness = 0f;
            getWindow().setAttributes(params);
            //textView.setText("BYE!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBars();

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textMain);
        gifDecoderView = (GifImageView) findViewById(R.id.gifView);

        Intent intent = new Intent(MainActivity.this, CameraWatcherService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
    }

    public void onEvent(MotionCustomEvent motionCustomEvent) {
        //Motion detected!
        //textView.setText("HELLO");
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        //TODO restoring from original value
        params.screenBrightness = 1f;
        this.getWindow().setAttributes(params);
        gifDecoderView.setVisibility(View.VISIBLE);

        h.removeCallbacks(runnableTurnOff);
        h.postDelayed(runnableTurnOff, 10000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        SmartMirrorApplication.activityPaused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        SmartMirrorApplication.activityResumed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void hideBars() {
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }
    }
}
