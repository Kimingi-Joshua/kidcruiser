package com.creativeapps.schoolbustracker.data.network.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class School implements Parcelable{
    @Expose
    @SerializedName("id")
    private Integer id;

    @Expose
    @SerializedName("name")
    private String name;


    @Expose
    @SerializedName("channel")
    private String channel;


    @Expose
    @SerializedName("address")
    private String address;
    
    @Expose
    @SerializedName("latitude")
    private Double latitude;

    @Expose
    @SerializedName("longitude")
    private Double longitude;

    @Expose
    @SerializedName("isPayAsYouGo")
    private Byte is_pay_as_you_go;

    protected School(Parcel in) {
        id = in.readInt();
        name = in.readString();
        channel = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        is_pay_as_you_go = in.readByte();
    }

    public static final Creator<School> CREATOR = new Creator<School>() {
        @Override
        public School createFromParcel(Parcel in) {

            return new School(in);
        }

        @Override
        public School[] newArray(int size) {
            return new School[size];
        }
    };

    public School(Integer id, String name, Double latitude, Double longitude, String address, Byte is_pay_as_you_go) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.is_pay_as_you_go = is_pay_as_you_go;
    }


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getChannel() {
        return channel;
    }

    public Double getLast_longitude() {
        return longitude;
    }

    public Double getLast_latitude() {
        return latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setAddress_latitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setAddress_longitude(Double longitude) {
        this.longitude = longitude;
    }
    //Getter for is_pay_as_you_go
    public Byte getIsPayAsYouGo() {
        return is_pay_as_you_go;
    }
    //Setter for is_pay_as_you_go
    public void setIsPayAsYouGo(Byte is_pay_as_you_go) {
        this.is_pay_as_you_go = is_pay_as_you_go;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(channel);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(address);
        parcel.writeByte(is_pay_as_you_go);
    }
}
