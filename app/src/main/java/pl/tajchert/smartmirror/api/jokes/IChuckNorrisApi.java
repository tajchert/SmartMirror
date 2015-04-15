package pl.tajchert.smartmirror.api.jokes;


import retrofit.Callback;
import retrofit.http.GET;

public interface IChuckNorrisApi {
    @GET("/jokes/random")
    void getRandomJoke(Callback<ResponseChuckNorris> callback);
}
