package com.example.faced;


import android.content.Context;
import android.graphics.Bitmap;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public interface FaceRecon {
    public void loadtflite(Context context);

    public MappedByteBuffer loadmodelFile(Context context) throws IOException;

    public void face_detector(final Bitmap bitmap, final String imagetype);

    public void get_embaddings(Bitmap bitmap, String imagetype);
}