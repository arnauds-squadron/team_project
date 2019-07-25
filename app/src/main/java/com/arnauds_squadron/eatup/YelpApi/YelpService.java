package com.arnauds_squadron.eatup.YelpApi;

import com.arnauds_squadron.eatup.home.HomeDetailsActivity;
import com.arnauds_squadron.eatup.models.Event;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YelpService {
    @GET("/businesses/search?term=food&categories=")
    Call<Event> getFood(@Query("categories") String cuisine);
    @GET("&latitude=")
    Call<Event> getLatitude(@Query("latitude") String latitude);
    @GET("&longitude=")
    Call<Event> getLongitude(@Query("longitude") String longitude);
//
//    RequestInterceptor requestInterceptor = new RequestInterceptor() {
//        @Override
//        public void intercept(RequestFacade request) {
//            request.addQueryParam("apikey", apiKey);
//        }
//    };
//    OkHttpClient client = new OkHttpClient();
//    client.interceptors().add(requestInterceptor);
//    Retrofit retrofit = new Retrofit.Builder()
//            .client(client)
//            .baseUrl("https://api.yelp.com/v3")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
//
//    YelpService service = retrofit.create(YelpService.class);
//
//    Call<YelpApiResponse> call = service.listEvents();
//    call.enqueue(new Callback<YelpApiResponse>() {
//        @Override
//        public void onResponse(Call<YelpApiResponse> call, Response response) {
//            // handle response here
//            YelpApiResponse boxOfficeMovieResponse = response.body();
//        }
//
//        @Override
//        public void onFailure(Throwable t) {
//
//        }
//    });
}
