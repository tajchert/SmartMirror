package pl.tajchert.smartmirror;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by tajchert on 09.04.15.
 */
public class ServiceStartRunning extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent intentRun = new Intent(ServiceStartRunning.this, MainActivity.class);
        intentRun.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentRun);
        return super.onStartCommand(intent, flags, startId);

    }
}
