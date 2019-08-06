package com.arnauds_squadron.eatup.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.parse.ParseFile;

import org.json.JSONObject;
import org.parceler.Parcel;

import java.net.URL;
import java.util.List;

@Parcel
public class Business {

    public Business() {
    }

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("image_url")
    @Expose
    public String imageUrl;

    @SerializedName("url")
    @Expose
    public String url;

    @SerializedName("rating")
    @Expose
    public Float rating;

    @SerializedName("review_count")
    @Expose
    public int reviewCount;

    @SerializedName("display_phone")
    @Expose
    public String displayPhone;

    @SerializedName("phone")
    @Expose
    public String phone;

    @SerializedName("price")
    @Expose
    public String price;

    @SerializedName("location")
    @Expose
    public Location location;

}
