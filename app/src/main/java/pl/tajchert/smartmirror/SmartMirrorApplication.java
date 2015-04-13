package pl.tajchert.smartmirror;

import android.app.Application;

/**
 * Created by tajchert on 09.04.15.
 */
public class SmartMirrorApplication extends Application {

    private static boolean activityVisible;
    private static long timeLastHackerNewsUpdate;
    private static long timeLastDateFactUpdate;
    private static long timeRefreshPrevious;



    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static long getTimeLastHackerNewsUpdate() {
        return timeLastHackerNewsUpdate;
    }

    public static void setTimeLastHackerNewsUpdate(long timeLastHackerNewsUpdate) {
        SmartMirrorApplication.timeLastHackerNewsUpdate = timeLastHackerNewsUpdate;
    }

    public static long getTimeLastDateFactUpdate() {
        return timeLastDateFactUpdate;
    }

    public static void setTimeLastDateFactUpdate(long timeLastDateFactUpdate) {
        SmartMirrorApplication.timeLastDateFactUpdate = timeLastDateFactUpdate;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static long getTimeRefreshPrevious() {
        return timeRefreshPrevious;
    }

    public static void setTimeRefreshPrevious(long timeRefreshPrevious) {
        SmartMirrorApplication.timeRefreshPrevious = timeRefreshPrevious;
    }

    public static void activityPaused() {
        activityVisible = false;
    }
}
