package pl.tajchert.smartmirror.events;

/**
 * Created by tajchert on 09.04.15.
 */
public class MotionCustomEvent {
    public boolean isSignificationChange;

    public MotionCustomEvent(boolean isSignificationChange) {
        this.isSignificationChange = isSignificationChange;
    }
}
