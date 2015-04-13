package pl.tajchert.smartmirror.api;

import android.util.Log;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import pl.tajchert.smartmirror.events.ConnectionEvent;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by tajchert on 12.04.15.
 */
public class WebContentManager {
    private static final String TAG = "WebContentManager";
    private static final String API_URL_HACKER_NEWS = "https://hacker-news.firebaseio.com";
    public static ArrayList<StoryHackerNews> storiesHackerNews;

    public static void refresh() {
        refreshHackerNews();
    }

    public static void refreshHackerNews() {
        storiesHackerNews = new ArrayList<>();
        IHackerNewsApi articleGetter = getHostAdapter(API_URL_HACKER_NEWS).create(IHackerNewsApi.class);
        articleGetter.getNewsStories(new Callback<ResponseHackerNews>() {
            @Override
            public void success(ResponseHackerNews responseHackerNews, Response response) {
                int count = 0;
                for(Integer storyId : responseHackerNews.newsListIds) {
                    if(count < 20) {
                        getStoryDetails(storyId);
                    }
                    count++;
                }
                EventBus.getDefault().postSticky(new ConnectionEvent());
            }
            @Override
            public void failure(RetrofitError error) {
                EventBus.getDefault().postSticky(new ConnectionEvent(true, error.getUrl()));
            }
        });
    }

    private static void getStoryDetails(Integer id) {
        IHackerNewsDetailApi articleGetter = getHostAdapter(API_URL_HACKER_NEWS + "/v0/item/" + id + ".json").create(IHackerNewsDetailApi.class);
        articleGetter.getStoryDetails(new Callback<StoryHackerNews>() {
            @Override
            public void success(StoryHackerNews storyHackerNews, Response response) {
                storiesHackerNews.add(storyHackerNews);
                EventBus.getDefault().postSticky(storiesHackerNews);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure ");
                //TODO handle it somehow
            }
        });
    }

    public static RestAdapter getHostAdapter(String baseHost) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(baseHost)
                .setConverter(new LoganSquareConverter())
                .build();
        return restAdapter;
    }
}
