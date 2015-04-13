package pl.tajchert.smartmirror.api;


import retrofit.Callback;
import retrofit.http.GET;

public interface IHackerNewsApi {
    @GET("/v0/newstories.json")
    void getNewsStories(Callback<ResponseHackerNews> callback);
}
