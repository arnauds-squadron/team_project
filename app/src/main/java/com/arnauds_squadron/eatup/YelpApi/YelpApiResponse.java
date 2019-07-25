package com.arnauds_squadron.eatup.YelpApi;

import com.arnauds_squadron.eatup.models.Event;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//todo understand how to user retrofit
public class YelpApiResponse {
    List<Event> events;
    //Event event = Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));


    public YelpApiResponse() {
        events = new ArrayList<Event>();
    }
    GsonBuilder gsonBuilder = new GsonBuilder();
    // register type adapters here, specify field naming policy, etc.
    Gson Gson = gsonBuilder.create();
    public static YelpApiResponse parseJSON(String response) {
        Gson gson = new GsonBuilder().create();
        YelpApiResponse yelpApiResponse = gson.fromJson(response, YelpApiResponse.class);
        return yelpApiResponse;
    }

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("url")
    @Expose
    private String yelpUrl;
    @SerializedName("rating")
    @Expose
    private String rating;
    public static final String BASE_URL = "https://api.yelp.com/v3";
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    YelpService service = retrofit.create(YelpService.class);
    //Call<YelpApiResponse> repos = service.getFood();
}
