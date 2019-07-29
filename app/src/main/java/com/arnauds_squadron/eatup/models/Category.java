package com.arnauds_squadron.eatup.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("alias")
    @Expose
    public String alias;
}

