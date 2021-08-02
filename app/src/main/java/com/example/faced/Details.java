package com.example.faced;

import org.json.JSONException;
import org.json.JSONObject;

public class Details {
    private JSONObject jsonObject;
    private String imageBitmap;
    private String name;
    private String phoneNumber;

    public Details(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        try {
            this.imageBitmap = (String) jsonObject.get("imageBitmap");
            this.name = (String) jsonObject.get("name");
            this.phoneNumber = (String) jsonObject.get("phoneNumber");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public String getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(String imageBitmap) {
        this.imageBitmap = imageBitmap;
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