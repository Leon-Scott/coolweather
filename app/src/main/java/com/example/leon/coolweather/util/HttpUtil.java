package com.example.leon.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Leon on 2017/3/30.
 */

public class HttpUtil {
    //此处使用了OKhttp.callback (自带的)回调函数：callback.onResponse();callback.onFailure();
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = okHttpClient.newCall(request).execute();
                     String s = response.body().string();
                    Log.d("HttpUtil22:", s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/

        okHttpClient.newCall(request).enqueue(callback);
    }
}
