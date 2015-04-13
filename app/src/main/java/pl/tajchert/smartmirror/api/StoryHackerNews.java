package pl.tajchert.smartmirror.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by tajchert on 12.04.15.
 */

@JsonObject
public class StoryHackerNews {
    @JsonField
    public int id;

    @JsonField
    public int score;

    @JsonField
    public String text;

    @JsonField
    public String title;

    @JsonField
    public long time;

    @JsonField
    public String url;
}
