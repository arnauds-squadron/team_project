package com.arnauds_squadron.eatup.YelpApi;

import com.arnauds_squadron.eatup.Business;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

//todo understand how to user retrofit
public class YelpApiResponse {

    @SerializedName("businesses")
    @Expose
    public List<Business> businessList;

}
