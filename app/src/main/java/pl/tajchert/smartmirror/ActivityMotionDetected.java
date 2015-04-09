package pl.tajchert.smartmirror;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;


public class ActivityMotionDetected extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.textMain);
        textView.setText("Motion Detected!");

        Handler handler = new Handler(Looper.getMainLooper());
        final Runnable r = new Runnable() {
            public void run() {
                ActivityMotionDetected.this.finish();
            }
        };
        handler.postDelayed(r, 3000);
    }
}
