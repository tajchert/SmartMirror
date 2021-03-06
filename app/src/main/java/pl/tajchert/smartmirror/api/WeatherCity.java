package pl.tajchert.smartmirror.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

/**
 * Created by tajchert on 13.04.15.
 */
@JsonObject
public class WeatherCity {
    @JsonField
    public List<WeatherDay> list;
    @JsonField
    public String cod;
    @JsonField
    public String cnt;
}
