package com.example.faced;

import android.graphics.Bitmap;

public class UserDetails {
    private String filename;
    private Bitmap userImage;
    private int index;
    private String name;
    private String phoneNumber;


    public UserDetails(int index,String filename, Bitmap userImage,String name,String phoneNumber) {
        this.index = index;
        this.filename = filename;
        this.userImage = userImage;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Bitmap getUserImage() {
        return userImage;
    }

    public void setUserImage(Bitmap userImage) {
        this.userImage = userImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
