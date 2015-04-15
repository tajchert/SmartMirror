package pl.tajchert.smartmirror.api.jokes;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by tajchert on 14.04.15.
 */

@JsonObject
public class ResponseChuckNorris {
    @JsonField(name="type")
    public String status;
    @JsonField(name="value")
    public ChuckNorrisJoke chuckNorrisJoke;
}
