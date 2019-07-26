package com.arnauds_squadron.eatup.yelp_api;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YelpService {
    //the endpoint businesses/search? and you must query all of the necessary attributes for the call to work
    @GET("businesses/search?")
    Call<YelpApiResponse> getLocation(@Query("terms") String food, @Query("categories") String cuisine, @Query("latitude") Double latitude, @Query("longitude") Double longitude);

}
