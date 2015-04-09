package pl.tajchert.smartmirror;

import android.app.Application;

/**
 * Created by tajchert on 09.04.15.
 */
public class SmartMirrorApplication extends Application {

    private static boolean activityVisible;


    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }
}
