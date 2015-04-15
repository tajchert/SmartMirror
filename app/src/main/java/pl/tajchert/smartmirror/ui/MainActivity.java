package pl.tajchert.smartmirror.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import pl.tajchert.smartmirror.R;
import pl.tajchert.smartmirror.SmartMirrorApplication;
import pl.tajchert.smartmirror.api.DateApi;
import pl.tajchert.smartmirror.api.StoryHackerNews;
import pl.tajchert.smartmirror.api.WeatherCity;
import pl.tajchert.smartmirror.api.WebContentManager;
import pl.tajchert.smartmirror.api.jokes.IChuckNorrisApi;
import pl.tajchert.smartmirror.api.jokes.ResponseChuckNorris;
import pl.tajchert.smartmirror.camera.CameraWatcherService;
import pl.tajchert.smartmirror.events.MotionCustomEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends ActionBarActivity implements RecognitionListener {
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
            if(speech != null) {
                speech.stopListening();
            }
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
    final Runnable runnableStopListening = new Runnable() {
        @Override
        public void run() {
            if(speech != null) {
                speech.stopListening();
            }
        }
    };
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private TextToSpeech tts;

    @InjectView(R.id.textMain)
    TextView textView;

    @InjectView(R.id.weatherView)
    WeatherView weatherView;

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

    int delay = 500; // delay for 1 sec.
    int period = 1750; // repeat every 1 sec.
    int currentListTopItem = 0;

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

        initSpeechRecognition();
        initTts();
        startService(intent);
        setRunningTasks();

    }

    private void initSpeechRecognition() {
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1800);
    }

    private void initTts() {
        tts=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    } else{
                        sayText("");
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });
    }

    private void sayText(String text) {
        if (text != null && !"".equals(text)) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }


    private void setRunningTasks() {
        handler.removeCallbacks(runnableTurnOff);
        handler.postDelayed(runnableTurnOff, 10000);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                //move news list
                if (linearLayout.getVisibility() == View.INVISIBLE) {
                    return;
                }
                if (newsList == null || newsList.getAdapter() == null || newsList.getAdapter().isEmpty() || newsListContent == null || newsListContent.size() < 4) {
                    return;
                }
                if (currentListTopItem >= newsListContent.size()) {
                    currentListTopItem = 0;
                } else {
                    currentListTopItem++;
                }
                newsList.smoothScrollToPosition(currentListTopItem);
            }
        }, delay, period);
    }

    public void onEvent(MotionCustomEvent motionCustomEvent) {
        //Motion detected!
        if(motionCustomEvent.isSignificationChange) {
            //speech.startListening(recognizerIntent);//TODO tablet with better mic
            sayJoke();
        }
        //sayText("Welcome");
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
        handler.removeCallbacks(runnableStopListening);
        handler.postDelayed(runnableStopListening, 2000);

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
        weatherView.setWeather(weatherCity, MainActivity.this);
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
        if (speech != null) {
            speech.destroy();
        }
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
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
        }
    }

    private void sayJoke() {
        IChuckNorrisApi norrisGetter = WebContentManager.getHostAdapter("http://api.icndb.com/", false).create(IChuckNorrisApi.class);
        norrisGetter.getRandomJoke(new Callback<ResponseChuckNorris>() {
            @Override
            public void success(ResponseChuckNorris responseChuckNorris, Response response) {
                Log.d(TAG, "success Chuck" + responseChuckNorris.chuckNorrisJoke.joke);
                sayText(responseChuckNorris.chuckNorrisJoke.joke);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure while getting Chuck joke.");
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onReadyForSpeech(Bundle params) {}

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onRmsChanged(float rmsdB) {}

    @Override
    public void onBufferReceived(byte[] buffer) {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(int error) {}

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) {
            text += result + "\n";
        }
        Log.d(TAG, "onResults speech: " + text);
        if(text != null && text.contains("joke")) {
            sayJoke();
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {}

    @Override
    public void onEvent(int eventType, Bundle params) {}
}
