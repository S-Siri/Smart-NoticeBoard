package com.example.myfirebaseapp;

public class ReadWriteUserDetails {
    public String  fullName,doB, gender, mobile;

    //Constructor
    public ReadWriteUserDetails(){};
    public ReadWriteUserDetails(String fullName,String textDoB, String textGender, String textMobile) {
        this.fullName=fullName;
        this.doB = textDoB;
        this.gender = textGender;
        this.mobile = textMobile;
    }
}
