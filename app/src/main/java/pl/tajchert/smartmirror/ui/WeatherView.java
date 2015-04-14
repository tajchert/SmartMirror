package pl.tajchert.smartmirror.ui;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.tajchert.smartmirror.R;
import pl.tajchert.smartmirror.api.WeatherCity;
import pl.tajchert.smartmirror.api.WeatherDay;


public class WeatherView extends LinearLayout {

    @InjectView(R.id.weatherIconOne)
    TextView iconOne;
    @InjectView(R.id.weatherIconTwo)
    TextView iconTwo;
    @InjectView(R.id.weatherIconThree)
    TextView iconThree;

    @InjectView(R.id.weatherTextOne)
    TextView textOne;
    @InjectView(R.id.weatherTextTwo)
    TextView textTwo;
    @InjectView(R.id.weatherTextThree)
    TextView textThree;

    public WeatherView(Context context) {
        super(context);
        init(context);
    }

    public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        inflate(getContext(), R.layout.weather_view, this);
        ButterKnife.inject(this);
    }

    public void setWeather(WeatherCity weather, Context context) {
        if(weather == null || weather.list == null || weather.list.size() == 0) {
            return;
        }

        for(int i = 0; i < weather.list.size(); i++) {
            WeatherDay weatherDay = weather.list.get(i);
            if(weatherDay != null && weatherDay.weather != null && weatherDay.weather.get(0) != null) {
                switch(i){
                    case 0:
                        textOne.setText(Html.fromHtml(weatherDay.temp.min + "/" + weatherDay.temp.max + "<sup>o</sup>" + "C"));
                        iconOne.setText(setIcon(weatherDay.weather.get(0).icon, context));
                        break;
                    case 1:
                        textTwo.setText(Html.fromHtml(weatherDay.temp.min + "/" + weatherDay.temp.max + "<sup>o</sup>" + "C"));
                        iconTwo.setText(setIcon(weatherDay.weather.get(0).icon, context));
                        break;
                    case 2:
                        textThree.setText(Html.fromHtml(weatherDay.temp.min + "/" + weatherDay.temp.max + "<sup>o</sup>" + "C"));
                        iconThree.setText(setIcon(weatherDay.weather.get(0).icon, context));
                        break;
                }
            }
        }
        invalidate();
    }

    private String setIcon(String iconName, Context context) {
        String result = "B";
        if("01d".equals(iconName) || "01n".equals(iconName)) {
            result = "B";
        } else if("02d".equals(iconName) || "02n".equals(iconName)) {
            result = "H";
        } else if("03d".equals(iconName) || "03n".equals(iconName)) {
            result = "H";
        } else if("04d".equals(iconName) || "04n".equals(iconName)) {
            result = "N";
        } else if("09d".equals(iconName) || "09n".equals(iconName)) {
            result = "R";
        } else if("10d".equals(iconName) || "10n".equals(iconName)) {
            result = "R";
        } else if("11d".equals(iconName) || "11n".equals(iconName)) {
            result = "O";
        } else if("13d".equals(iconName) || "13n".equals(iconName)) {
            result = "W";
        } else if("50d".equals(iconName) || "50n".equals(iconName)) {
            result = "J";
        }
        //result = context.getResources().getIdentifier("ic_weather_clouds", "drawable", context.getPackageName());
        return result;

    }
}
