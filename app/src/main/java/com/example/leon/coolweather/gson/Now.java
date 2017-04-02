package com.example.leon.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Leon on 2017/4/1.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public Condition condition;

    public class Condition{
        @SerializedName("txt")
        public String info;
    }
}
