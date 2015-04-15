package pl.tajchert.smartmirror.api.jokes;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by tajchert on 14.04.15.
 */
@JsonObject
public class ChuckNorrisJoke {
    @JsonField(name="id")
    public int id;
    @JsonField(name="joke")
    public String joke;
}
