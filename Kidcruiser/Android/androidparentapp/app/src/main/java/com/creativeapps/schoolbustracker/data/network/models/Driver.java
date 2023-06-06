package com.creativeapps.schoolbustracker.data.network.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Driver implements Parcelable{
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
    @SerializedName("last_latitude")
    private Double last_latitude;

    @Expose
    @SerializedName("last_longitude")
    private Double last_longitude;

    @Expose
    @SerializedName("tel_number")
    private String tel_number;

    @Expose
    @SerializedName("country_code")
    private String country_code;

    @Expose
    @SerializedName("secretKey")
    private String secretKey;

    @Expose
    @SerializedName("v_code")
    private String v_code;

    @Expose
    @SerializedName("verified")
    private Byte verified;


    @Expose
    @SerializedName("bus")
    private Bus bus;

    protected Driver(Parcel in) {
        id = in.readInt();
        name = in.readString();
        channel = in.readString();
        last_latitude = in.readDouble();
        last_longitude = in.readDouble();
        tel_number = in.readString();
        country_code = in.readString();
        secretKey = in.readString();
        v_code = in.readString();
        verified = in.readByte();
        bus = in.readParcelable(Bus.class.getClassLoader());
    }

    public static final Creator<Driver> CREATOR = new Creator<Driver>() {
        @Override
        public Driver createFromParcel(Parcel in) {

            return new Driver(in);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };

    public Driver(Integer id, String name, Double last_latitude, Double last_longitude,
                  String tel_number, String country_code, String secretKey, String v_code, Byte verified) {
        this.id = id;
        this.name = name;
        this.last_latitude = last_latitude;
        this.last_longitude = last_longitude;
        this.tel_number = tel_number;
        this.country_code = country_code;
        this.secretKey = secretKey;
        this.v_code = v_code;
        this.verified = verified;
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
        return last_longitude;
    }

    public Double getLast_latitude() {
        return last_latitude;
    }

    public String getTel_number() {
        return tel_number;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getV_code() {
        return v_code;
    }

    public Byte getVerified() {
        return verified;
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

    public void setAddress_latitude(Double last_latitude) {
        this.last_latitude = last_latitude;
    }

    public void setAddress_longitude(Double last_longitude) {
        this.last_longitude = last_longitude;
    }

    public void setTel_number(String tel_number) {
        this.tel_number = tel_number;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setV_code(String v_code) {
        this.v_code = v_code;
    }

    public void setVerified(Byte verified) {
        this.verified = verified;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
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
        parcel.writeDouble(last_latitude);
        parcel.writeDouble(last_longitude);
        parcel.writeString(tel_number);
        parcel.writeString(country_code);
        parcel.writeString(secretKey);
        parcel.writeString(v_code);
        parcel.writeByte(verified);
    }
}
