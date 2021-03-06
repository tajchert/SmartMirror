package pl.tajchert.smartmirror.api;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import de.greenrobot.event.EventBus;
import pl.tajchert.smartmirror.SmartMirrorApplication;
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
    private static final String API_URL_FACT = "https://numbersapi.p.mashape.com/";
    private static final String API_URL_WEATHER = "http://api.openweathermap.org/data/2.5";
    //http://api.openweathermap.org/data/2.5/forecast/daily?lat=52.252252252252255&lon=20.985195166412463l&units=metric&cnt=3
    private static final int NEWS_NUMBER_HACKER_NEWS = 20;
    private static final long milisecondsTimeHackerNewsUpdate = 600000;//10min
    private static final long milisecondsTimeDateFactUpdate = 10000;//10sec
    private static final long milisecondsTimeWeatherUpdate = 600000;//10min
    private static final long milisecondsTimeRefresh = 1000;//1sec
    public ArrayList<StoryHackerNews> storiesHackerNews;

    public void refresh(Context context) {
        long currentTime= Calendar.getInstance().getTimeInMillis();
        if(currentTime - SmartMirrorApplication.getTimeRefreshPrevious() > milisecondsTimeRefresh) {
            if (currentTime - SmartMirrorApplication.getTimeLastHackerNewsUpdate() > milisecondsTimeHackerNewsUpdate) {
                refreshHackerNews();
            }

            if (currentTime - SmartMirrorApplication.getTimeLastDateFactUpdate() > milisecondsTimeDateFactUpdate) {
                Calendar cal = Calendar.getInstance();
                getFact((cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH));
            }

            if (currentTime - SmartMirrorApplication.getTimeLastWeatherUpdate() > milisecondsTimeWeatherUpdate) {
                Location location = SmartMirrorApplication.getLastLocation(context);
                getWeather(location);
            }

            SmartMirrorApplication.setTimeRefreshPrevious(currentTime);
        }
    }

    public void getWeather(Location location) {
        if(location == null) {
           return;
        }
        IWeatherApi weatherGetter = getHostAdapter(API_URL_WEATHER, false).create(IWeatherApi.class);
        weatherGetter.getForecastDayily(location.getLatitude(), location.getLongitude(), "metric", 3, new Callback<WeatherCity>() {
            @Override
            public void success(WeatherCity weatherWeather, Response response) {
                Log.d(TAG, "success :" + weatherWeather);
                SmartMirrorApplication.setTimeLastWeatherUpdate(Calendar.getInstance().getTimeInMillis());
                EventBus.getDefault().post(weatherWeather);
                EventBus.getDefault().postSticky(new ConnectionEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure : " + error.getLocalizedMessage());
                EventBus.getDefault().postSticky(new ConnectionEvent(true, error.getUrl()));
            }
        });
    }

    public void refreshHackerNews() {
        storiesHackerNews = new ArrayList<>();
        IHackerNewsApi hackerNewsGetter = getHostAdapter(API_URL_HACKER_NEWS, true).create(IHackerNewsApi.class);
        hackerNewsGetter.getNewsStories(new Callback<ResponseHackerNews>() {
            @Override
            public void success(ResponseHackerNews responseHackerNews, Response response) {
                int count = 0;
                for (Integer storyId : responseHackerNews.newsListIds) {
                    if (count < NEWS_NUMBER_HACKER_NEWS) {
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

    private void getStoryDetails(Integer id) {
        IHackerNewsDetailApi storyDetailsGetter = getHostAdapter(API_URL_HACKER_NEWS + "/v0/item/" + id + ".json", false).create(IHackerNewsDetailApi.class);
        storyDetailsGetter.getStoryDetails(new Callback<StoryHackerNews>() {
            @Override
            public void success(StoryHackerNews storyHackerNews, Response response) {
                storiesHackerNews.add(storyHackerNews);
                if (storiesHackerNews.size() >= NEWS_NUMBER_HACKER_NEWS) {
                    SmartMirrorApplication.setTimeLastHackerNewsUpdate(Calendar.getInstance().getTimeInMillis());
                    EventBus.getDefault().postSticky(storiesHackerNews);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure ");
                //TODO handle it somehow
            }
        });
    }

    private static void getFact(String date) {
        date = API_URL_FACT + date;
        IDateApi factGetter = getHostAdapter(date, false).create(IDateApi.class);
        factGetter.getFactForDate("false", "true", new Callback<DateApi>() {
            @Override
            public void success(DateApi dateApi, Response response) {
                SmartMirrorApplication.setTimeLastDateFactUpdate(Calendar.getInstance().getTimeInMillis());
                EventBus.getDefault().postSticky(dateApi);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure :" + error.getResponse());
            }
        });
    }

    public static RestAdapter getHostAdapter(String baseHost, boolean modifyOutput) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(baseHost)
                .setConverter(new LoganSquareConverter(modifyOutput))
                .build();
        return restAdapter;
    }
}
