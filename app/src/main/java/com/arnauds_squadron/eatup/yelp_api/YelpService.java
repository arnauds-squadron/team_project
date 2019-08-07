package com.arnauds_squadron.eatup.yelp_api;

import com.arnauds_squadron.eatup.models.Business;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface YelpService {
    //the endpoint businesses/search? and you must query all of the necessary attributes for the call to work
    @GET("businesses/search?")
    Call<YelpApiResponse> getLocation(@Query("latitude") double latitude, @Query("longitude") double longitude, @Query("term") String cuisine, @Query("radius") int radius);

    @GET("businesses/{id}")
    Call<Business> getDetails(@Path("id") String id);

    // TODO remove at end if unused
    // categories autocomplete endpoint
    @GET("autocomplete")
    Call<YelpApiResponse> getSuggestion(@Query("text") String query);
}
