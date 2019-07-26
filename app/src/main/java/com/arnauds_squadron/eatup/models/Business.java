package com.arnauds_squadron.eatup.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Business {
    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("image_url")
    @Expose
    public String imageUrl;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("rating")
    @Expose
    public String rating;

}
