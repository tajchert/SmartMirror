package pl.tajchert.smartmirror.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by tajchert on 13.04.15.
 */
@JsonObject
public class WeatherWeather {
    @JsonField
    public int id;
    @JsonField
    public String main;
    @JsonField
    public String description;
    @JsonField
    public String icon;
}
