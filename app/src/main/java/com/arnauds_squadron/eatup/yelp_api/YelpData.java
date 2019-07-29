package com.arnauds_squadron.eatup.yelp_api;

import android.content.Context;

import com.arnauds_squadron.eatup.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class YelpData {
public static YelpService retrofit(Context context) {
final String secretKey = context.getResources().getString(R.string.yelp_api_key);

        //Getting Authentication through OkHttpClient
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
        .addInterceptor(new Interceptor() {
@NotNull
@Override
public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder requestBuilder = request.newBuilder()
        .header("Authorization", "Bearer " + secretKey)
        .method(request.method(), request.body());
        return chain.proceed(requestBuilder.build());
        }
        });
        //Using retrofit to call the YelpApiRepsonse on  the events Cuisine and geopoint location then checking for a response

        Retrofit retrofit = new Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl("https://api.yelp.com/v3/")
        .client(okHttpClient.build())
        .addConverterFactory(GsonConverterFactory.create())
        .build();
        YelpService service = retrofit.create(YelpService.class);
        return service;
        }
}
