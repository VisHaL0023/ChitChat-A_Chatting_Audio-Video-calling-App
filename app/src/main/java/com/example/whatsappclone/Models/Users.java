package com.example.whatsappclone.Models;

import java.io.Serializable;

public class Users implements Serializable {
    private String uid, name, phoneNumber, profileImage,FCM;

    //constructor


    public Users(String uid, String name, String phoneNumber, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
    }

    //Empty Constructor is Necessary while dealing with Firebase
    //Firebase cannot figure out on its own what your constructor does, so that's why you need an empty constructor:
    //to allow Firebase to create a new instance of the object, which it then proceeds to fill in using reflection.
    public Users(){}

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFCM() {
        return FCM;
    }

    public void setFCM(String FCM) {
        this.FCM = FCM;
    }
}
