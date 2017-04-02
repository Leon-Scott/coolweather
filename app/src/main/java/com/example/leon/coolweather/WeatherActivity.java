package com.example.leon.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.leon.coolweather.gson.Forcast;
import com.example.leon.coolweather.gson.Weather;
import com.example.leon.coolweather.util.HttpUtil;
import com.example.leon.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCityTextView;
    private TextView updateTimeTextView;
    private TextView degreeTextView;
    private TextView weatherInfoTextView;
    private LinearLayout forcastLayout;
    private TextView aqiTextView;
    private TextView pm25TextView;
    private TextView comfortTextView;
    private TextView carWashTextView;
    private TextView sportTextView;
    private SharedPreferences sharedPreferences;

    private ImageView bingPicImageView ;

    /*��������ˢ��*/
    public SwipeRefreshLayout swipeRefreshLayout;
    private String weatherId;

    /*�л����а�ť*/
    private Button updateAreaButton;
    /*�����˵�����*/
    public DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*������ʾ��״̬�����棨��״̬���غϣ�������״̬��Ϊ͸��*/
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        /*��ʼ�����ֿؼ�*/
        weatherLayout = (ScrollView) findViewById(R.id.id_weatherLayout);
        titleCityTextView = (TextView) findViewById(R.id.id_titleCity);
        updateTimeTextView = (TextView) findViewById(R.id.id_titleUpdateTime);
        degreeTextView = (TextView) findViewById(R.id.id_degree);
        weatherInfoTextView = (TextView) findViewById(R.id.id_weatherInfo);
        forcastLayout = (LinearLayout) findViewById(R.id.id_forcastLayout);
        aqiTextView = (TextView) findViewById(R.id.id_aqi);
        pm25TextView = (TextView) findViewById(R.id.id_pm25);
        comfortTextView = (TextView) findViewById(R.id.id_comfortInfo);
        carWashTextView = (TextView) findViewById(R.id.id_carWashInfo);
        sportTextView = (TextView) findViewById(R.id.id_sportInfo);

        /*��ȡ����Json���ݲ���ʾ��scroll������*/
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather",null);
        if(weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            weatherId = getIntent().getStringExtra("weatherId");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        /*��������ˢ�¼����¼�*/
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.id_SwipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        /*��bing��ȡͼƬ�����ñ���ɫ*/
        bingPicImageView = (ImageView) findViewById(R.id.id_bingPic);
        String bingPicString = sharedPreferences.getString("bingPic",null);
        if(bingPicString != null){
            Glide.with(this).load(bingPicString).into(bingPicImageView);
        }else{
            requestBingPic();
        }

        /*�л����м����¼�--�򿪻����˵�--����Ƭ���洦������߼�*/
        updateAreaButton = (Button) findViewById(R.id.id_updateArea);
        drawerLayout = (DrawerLayout) findViewById(R.id.id_drawerLayout);
        updateAreaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawerLayout.openDrawer(GravityCompat.START);

            }
        });


    }

    private void requestBingPic(){
        String bingPicUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(bingPicUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this,"��ȡÿ��ͼƬʧ��",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String bingPicString = response.body().string();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("bingPic",bingPicString);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPicString).into(bingPicImageView);
                    }
                });
            }
        });
    }


/*��������ID����������Ϣ*/
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"����������Ϣʧ�ܡ�",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseData);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && weather.status.equals("ok")){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("weather",responseData);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"��ȡ������Ϣʧ�ܣ�����״̬��������",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }
        });
    }

    /*��ʾweatherʵ���е�����*/
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"��";
        String weatherInfo = weather.now.condition.info;


        titleCityTextView.setText(cityName);
        updateTimeTextView.setText(updateTime);
        degreeTextView.setText(degree);
        weatherInfoTextView.setText(weatherInfo);

        forcastLayout.removeAllViews();
        for (Forcast forcast:weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forcast_item,forcastLayout,false);
            TextView forcastdateTextView = (TextView) view.findViewById(R.id.id_forcastDate);
            TextView forcastInfoTextView = (TextView) view.findViewById(R.id.id_forcastInfo);
            TextView forcastMaxTempTextView = (TextView) view.findViewById(R.id.id_forcastMaxTemperature);
            TextView forcastMinTempTextView = (TextView) view.findViewById(R.id.id_forcastMinTemperature);

            forcastdateTextView.setText(forcast.date);
            forcastInfoTextView.setText(forcast.cond.info);
            forcastMaxTempTextView.setText(forcast.temperatureRange.max);
            forcastMinTempTextView.setText(forcast.temperatureRange.min);

            forcastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiTextView.setText(weather.aqi.city.aqi);
            pm25TextView.setText(weather.aqi.city.pm25);
        }

        String comfort = "���ʶȣ�" + weather.suggestion.comfort.info;
        String carWash = "ϴ��ָ����" + weather.suggestion.carWash.info;
        String sport = "�˶����飺" + weather.suggestion.sport.info;

        comfortTextView.setText(comfort);
        carWashTextView.setText(carWash);
        sportTextView.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);
    }
}
