package com.example.vpoorn001c.ble;

/**
 * Created by vpoorn001c on 5/3/15.
 */
public class Data {
    private   int id  ;
    private  String  deviceAddress ;
    private  String couponName ;
    private  String  couponDesc ;
    private  String discountPerc;
    private  String  couponExpiry;

    public Data(int id, String deviceAddress, String couponName, String couponDesc, String discountPerc, String couponExpiry) {
        this.id = id;
        this.deviceAddress = deviceAddress;
        this.couponName = couponName;
        this.couponDesc = couponDesc;
        this.discountPerc = discountPerc;
        this.couponExpiry = couponExpiry;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getCouponExpiry() {
        return couponExpiry;
    }

    public void setCouponExpiry(String couponExpiry) {
        this.couponExpiry = couponExpiry;
    }

    public String getCouponDesc() {
        return couponDesc;
    }

    public void setCouponDesc(String couponDesc) {
        this.couponDesc = couponDesc;
    }

    public String getDiscountPerc() {
        return discountPerc;
    }

    public void setDiscountPerc(String discountPerc) {
        this.discountPerc = discountPerc;
    }


}
