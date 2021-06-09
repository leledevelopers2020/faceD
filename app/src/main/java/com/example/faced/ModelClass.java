package com.example.faced;

import java.io.File;
import java.util.List;

public class ModelClass {
    static private List<File> listFile;

    public static List<File> getListFile() {
        return listFile;
    }

    public static void setListFile(List<File> listFile) {
        ModelClass.listFile = listFile;
    }

    public static void clearListFile() {
        ModelClass.listFile.clear();
    }
}
