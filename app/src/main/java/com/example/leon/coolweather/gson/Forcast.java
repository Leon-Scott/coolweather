package com.example.leon.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Leon on 2017/4/1.
 */

public class Forcast {

    public String date;

    public Condition cond;
    @SerializedName("tmp")
    public TemperatureRange temperatureRange;

    public class Condition{
        @SerializedName("txt_d")
        public String info;
    }

    public class TemperatureRange{
        public String max;
        public String min;
    }
}
