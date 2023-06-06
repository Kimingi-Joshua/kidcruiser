package com.creativeapps.schoolbustracker.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ParentResponse {

    @Expose
    @SerializedName("parent")
    private Parent parent;

    public Parent getParent() {

        return parent;
    }

}
