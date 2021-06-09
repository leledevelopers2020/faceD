package com.example.faced;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;

public interface FaceRecon {


    public void loadtflite(Context context);

    public MappedByteBuffer loadmodelFile(Context context) throws IOException;

    public void face_detector(final Bitmap bitmap, final String imagetype);

    public void get_embaddings(Bitmap bitmap, String imagetype);

    public void setResult(TextView textView);

    public void loadFile(File photoFile);

    void loadCompents(ImageView originalImage, ImageView randomImage, TextView resultText);

    void displayInt(int i);

    void displayVal(int i);
}