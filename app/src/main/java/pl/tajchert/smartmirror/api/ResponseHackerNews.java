package pl.tajchert.smartmirror.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;

/**
 * Created by tajchert on 12.04.15.
 */

@JsonObject
public class ResponseHackerNews {
    @JsonField(name="storyIds")
    public ArrayList<Integer> newsListIds;
}
