package pl.tajchert.smartmirror.api;


import retrofit.Callback;
import retrofit.http.GET;

public interface IHackerNewsDetailApi {
    @GET("/")
    void getStoryDetails(Callback<StoryHackerNews> callback);
}
