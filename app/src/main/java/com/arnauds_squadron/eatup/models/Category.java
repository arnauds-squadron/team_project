package com.arnauds_squadron.eatup.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Category {

    public Category () {
    }
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("alias")
    @Expose
    public String alias;
}

