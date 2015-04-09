package pl.tajchert.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by tajchert on 09.04.15.
 */
public class ReceiverBootCustom extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, ServiceStartRunning.class);
            context.startService(serviceIntent);
        }
    }
}