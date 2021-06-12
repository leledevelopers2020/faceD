package com.example.faced;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FaceReconAPI {

    private static FaceReconAPI faceReconAPI=null;
    FaceReconImp faceReconImp = new FaceReconImp();
    ArrayList<Bitmap> allImagesBitmaps;
    public Bitmap oriBitmap;
    byte[] convertedByteArray;

    private FaceReconAPI() { }

    public static FaceReconAPI getFaceReconAPI() {
        if (faceReconAPI == null)
            faceReconAPI = new FaceReconAPI();
        return faceReconAPI;
    }

    public void setOriBitmap(Bitmap oriBitmap) {
        this.oriBitmap = oriBitmap;
    }

    public void loadFaceReconSetup() {
        faceReconImp.loadtflite(ModelClass.getActivity().getApplicationContext());
    }

    public void recongize(Bitmap bitmap) {
        allImagesBitmaps = new ArrayList<Bitmap>();
        faceReconImp.face_detector(bitmap, "original");
        convertedByteArray = convertBitmapToByteArray(oriBitmap);
        arrayToFile(convertedByteArray);
        if (ModelClass.getListFile().size() != 0) {
            loadAllImages();
        }
    }


    synchronized public static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {

                }
            }
        }
    }



    synchronized private File arrayToFile(byte[] convertedByteArray) {
        File file = null;
        FileOutputStream fos = null;
        try {
            file = createImageFile();
            if (file != null) {
                fos = new FileOutputStream(file);
                fos.write(convertedByteArray);
            } else {
                Log.v("info ", "unable to create file");
            }

            Log.d("file path ab", "above file path" + file.getAbsolutePath());

        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while writing file " + ioe);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ioe) {
                Log.v("info"," Failed to close outputstream object");
            }

        }
        return file;
    }


    synchronized private void loadAllImages() {
        allImagesBitmaps.clear();
        List<File> files = ModelClass.getListFile();
        if (files != null && files.size() > 0) {
            for (int i = 0; i < files.size(); i++) {
                String imageFile = files.get(i).getAbsolutePath();
                Log.v("val iteration = ", (i + 1) + " = " + imageFile);
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile);
                allImagesBitmaps.add(bitmap);
                if (i == files.size() - 1) {
                    verifyImage();
                }
            }
        }

    }

    synchronized private void verifyImage() {
        Log.v("info ","verifyImage");
        if (oriBitmap != null) {
            faceReconImp.setPreviousDistance(0.0);
            faceReconImp.setNoOfMatchImage(0);
            if (allImagesBitmaps.size() != 0) {
                Log.v("info ","into verifyImage");
                compareWithActualImage();
            } else {
                Toast.makeText(ModelClass.getActivity().getApplicationContext(), "No images are found to compare, Please capture " +
                        "om", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ModelClass.getActivity().getApplicationContext(), "Plase select the original image to compare", Toast.LENGTH_LONG).show();
        }
    }

    synchronized private void compareWithActualImage() {
        Log.v("info ","compareWithActualImage");
        for (int j = 0; j < allImagesBitmaps.size(); j++) {
            faceReconImp.face_detector(allImagesBitmaps.get(j), "test");
        }
    }


    synchronized private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = ModelClass.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }
}
