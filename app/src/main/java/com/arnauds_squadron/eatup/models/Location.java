package com.arnauds_squadron.eatup.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Location {

    public Location () {
    }

    @SerializedName("address1")
    @Expose
    public String address1;

    public String getAddress1() {
        return address1;
    }

    @SerializedName("address2")
    @Expose
    public String address2;

    @SerializedName("address3")
    @Expose
    public String address3;

    @SerializedName("city")
    @Expose
    public String city;

    public String getCity() {
        return city;
    }

    @SerializedName("zip_code")
    @Expose
    public String zipCode;

    public String getZipCode() {
        return zipCode;
    }
    @SerializedName("country")
    @Expose
    public String country;

    @SerializedName("state")
    @Expose
    public String state;

    public String getState() {
        return state;
    }
}
