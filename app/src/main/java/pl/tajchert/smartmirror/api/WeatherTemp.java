package pl.tajchert.smartmirror.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by tajchert on 13.04.15.
 */
@JsonObject
public class WeatherTemp {
    @JsonField
    public int day;
    @JsonField
    public int min;
    @JsonField
    public int max;
    @JsonField
    public int night;
    @JsonField
    public int eve;
    @JsonField
    public int morn;
}
