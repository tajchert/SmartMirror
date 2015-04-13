package pl.tajchert.smartmirror.events;

import java.util.Calendar;

/**
 * Created by tajchert on 12.04.15.
 */
public class ConnectionEvent {
    public boolean isError;
    public int errorCode;
    public Calendar date;
    public String url;

    public ConnectionEvent(boolean isError, String url) {
        this.isError = isError;
        this.url = url;
        this.date = Calendar.getInstance();
    }

    public ConnectionEvent(boolean isError, int errorCode, Calendar date) {
        this.isError = isError;
        this.errorCode = errorCode;
        this.date = date;
    }

    public ConnectionEvent(Calendar date) {
        this.isError = false;
        this.date = date;
    }

    public ConnectionEvent() {
        this.date = Calendar.getInstance();
        this.isError = false;
    }
}
