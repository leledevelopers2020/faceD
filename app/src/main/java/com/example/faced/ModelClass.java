package com.example.faced;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

public class ModelClass {
    static private List<File> listFile;
    static Activity activity;
    static ImageView imageView;

    public static List<File> getListFile() {
        return listFile;
    }

    public static void setListFile(List<File> listFile) {
        ModelClass.listFile = listFile;
    }

    public static void clearListFile() {
        ModelClass.listFile.clear();
    }

    public static Activity getActivity() {
        return ModelClass.activity;
    }

    public static void setActivity(Activity activity) {
        ModelClass.activity = activity;
    }

    public View getImageView() {
        return ModelClass.imageView;
    }

    public void setImageView(ImageView imageView) {
        ModelClass.imageView = imageView;
    }
}
