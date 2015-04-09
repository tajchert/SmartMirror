package pl.tajchert.smartmirror;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(MainActivity.this, CameraWatcherService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SmartMirrorApplication.activityPaused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SmartMirrorApplication.activityResumed();
    }
}
