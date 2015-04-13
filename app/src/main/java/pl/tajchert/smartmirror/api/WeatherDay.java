package pl.tajchert.smartmirror.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by tajchert on 13.04.15.
 */
@JsonObject
public class WeatherDay {
    @JsonField
    public WeatherTemp temp;
    @JsonField
    public long dt;
    @JsonField
    public float pressure;
    @JsonField
    public int humidity;
    @JsonField
    public WeatherWeather weather;
    @JsonField
    public float speed;
    @JsonField
    public int deg;
    @JsonField
    public int clouds;

}
