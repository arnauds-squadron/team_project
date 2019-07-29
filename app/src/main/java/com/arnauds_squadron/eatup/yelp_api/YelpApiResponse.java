package com.arnauds_squadron.eatup.yelp_api;

import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Category;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

//todo understand how to user retrofit
public class YelpApiResponse {

    //This is the list of businesses the YelpService class will get back
    @SerializedName("businesses")
    @Expose
    public List<Business> businessList;

    @SerializedName("categories")
    @Expose
    public List<Category> categoryList;
}
