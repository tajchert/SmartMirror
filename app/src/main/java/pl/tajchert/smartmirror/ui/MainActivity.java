package pl.tajchert.smartmirror.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import pl.tajchert.smartmirror.R;
import pl.tajchert.smartmirror.SmartMirrorApplication;
import pl.tajchert.smartmirror.api.DateApi;
import pl.tajchert.smartmirror.api.StoryHackerNews;
import pl.tajchert.smartmirror.api.WeatherCity;
import pl.tajchert.smartmirror.api.WebContentManager;
import pl.tajchert.smartmirror.camera.CameraWatcherService;
import pl.tajchert.smartmirror.events.MotionCustomEvent;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";
    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    final Runnable runnableTurnOff = new Runnable() {
        @Override
        public void run() {
            //gifDecoderView.setVisibility(View.INVISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
            WindowManager.LayoutParams params = getWindow().getAttributes();
            /** Turn off: */
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            params.screenBrightness = 0f;
            getWindow().setAttributes(params);
            //textView.setText("BYE!");
        }
    };
    @InjectView(R.id.textMain)
    TextView textView;

    //@InjectView(R.id.gifView)
    //GifImageView gifDecoderView;

    @InjectView(R.id.listHackerNews)
    ListView newsList;

    @InjectView(R.id.mainLayout)
    LinearLayout linearLayout;

    final Handler handler = new Handler();
    private static WebContentManager webContentManager;
    private ArrayList<String> newsListContent = new ArrayList<String>();
    private ArrayList<StoryHackerNews> storiesHackerNews;
    private ArrayAdapter<String> listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBars();

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        listAdapter = new ArrayAdapter<>(this, R.layout.news_row, newsListContent);
        newsList.setAdapter(listAdapter);
        webContentManager = new WebContentManager();

        Intent intent = new Intent(MainActivity.this, CameraWatcherService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);

        handler.removeCallbacks(runnableTurnOff);
        handler.postDelayed(runnableTurnOff, 10000);
    }

    public void onEvent(MotionCustomEvent motionCustomEvent) {
        //Motion detected!
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        //TODO restoring from original value
        params.screenBrightness = 1f;
        this.getWindow().setAttributes(params);
        //gifDecoderView.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        webContentManager.refresh(MainActivity.this);

        handler.removeCallbacks(runnableTurnOff);
        handler.postDelayed(runnableTurnOff, 10000);
    }

    public void onEvent(ArrayList<StoryHackerNews> storiesHackerNews) {
        if(storiesHackerNews != null && storiesHackerNews.size() > 0) {
            this.storiesHackerNews = storiesHackerNews;
            newsListContent = new ArrayList<>();
            updateNewsList();
        }
    }

    public void onEvent(DateApi dateApi) {
        if(dateApi !=null && dateApi.text != null && dateApi.text.length() > 0) {
            dateApi.text = dateApi.text.substring(0,1).toUpperCase() + dateApi.text.substring(1);
            textView.setVisibility(View.VISIBLE);
            textView.setText(dateApi.text);
        }
    }

    public void onEvent(WeatherCity weatherCity) {
        Log.d(TAG, "onEvent weather: " + weatherCity);
    }

    private void updateNewsList(){
        for(StoryHackerNews story : storiesHackerNews) {
            if(story.title != null && story.title.length() > 0) {
                newsListContent.add(story.title);
            }
        }
        listAdapter = new ArrayAdapter<>(this, R.layout.news_row, newsListContent);
        newsList.setAdapter(listAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        SmartMirrorApplication.activityPaused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        SmartMirrorApplication.activityResumed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void hideBars() {
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }
    }
}
