package com.example.faced;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FaceReconAPI {

    private static FaceReconAPI faceReconAPI=null;
    private static String result;
    FaceReconImp faceReconImp = new FaceReconImp();
    ArrayList<Details> allImagesBitmaps;
    public Bitmap oriBitmap;
    byte[] convertedByteArray;
    static List<UserDetails> userDetails = new ArrayList<>();

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
        allImagesBitmaps = new ArrayList<Details>();
        faceReconImp.face_detector(bitmap, "original",-1);
        //saving image
        //convertedByteArray = convertBitmapToByteArray(oriBitmap);
        //arrayToFile(convertedByteArray);
        //saveJSONFile(null,null);
        if (ModelClass.getListFile().size() != 0) {
            loadAllImages();
        }
    }

    public void saveJSONFile(String name,String phoneNumber) {
        FileOutputStream stream = null;
        File jsonFile;
        //String jsonFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String jsonFileName = name;
        File path = ModelClass.getActivity().getApplicationContext().getExternalFilesDir("text/plain");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonFile = File.createTempFile(
                    jsonFileName,  /* prefix */
                    ".json",         /* suffix */
                    path      /* directory */
            );
            jsonObject.put("imageBitmap",result);
            jsonObject.put("name",name);
            jsonObject.put("phoneNumber",phoneNumber);
            stream = new FileOutputStream(jsonFile);
            String data = "Data " + jsonObject.toString();
            stream.write(data.getBytes());
            stream.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }


    synchronized public static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            //convert the bitmap into string
            result=Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

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
        userDetails.clear();
        FileReader fileReader =null;
        BufferedReader bufferedReader = null;
        //getFiles();
        List<File> files = ModelClass.getListFile();
        if (files != null && files.size() > 0) {
            for (int i = 0; i < files.size(); i++) {
                String imageFile = files.get(i).getAbsolutePath();
                Log.v("val iteration = ", (i + 1) + " = " + imageFile);
                StringBuffer stringBuffer = new StringBuffer();
                String line = "";
                try {
                    fileReader = new FileReader(files.get(i).getAbsolutePath());
                    bufferedReader = new BufferedReader(fileReader);
                    while ((line=bufferedReader.readLine()) != null){
                        stringBuffer.append(line);
                    }
                    String data = stringBuffer.toString().replace("Data ","");
                    bufferedReader.close();
                    allImagesBitmaps.add(new Details(new JSONObject(data)));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
                //Bitmap bitmap = BitmapFactory.decodeFile(imageFile);
                //allImagesBitmaps.add(bitmap);
                userDetails.add(new UserDetails(
                        i,
                        imageFile,
                        getBitMapFromString(allImagesBitmaps.get(i).getImageBitmap()),
                        allImagesBitmaps.get(i).getName(),
                        allImagesBitmaps.get(i).getPhoneNumber()
                ));
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
            //faceReconImp.setNoOfMatchImage(0);
            faceReconImp.setMatchingIndex(0);
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
            faceReconImp.face_detector(getBitMapFromString(allImagesBitmaps.get(j).getImageBitmap()), "test",j);
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

    private void getFiles() {

        File path = ModelClass.getActivity().getApplicationContext().getExternalFilesDir("text/plain");
        if (path.exists()) {
            FileReader fileReader =null;
            BufferedReader bufferedReader = null;
            List<Details> details = new ArrayList<Details>();
            List<File> files = Arrays.asList(path.listFiles());
            for(int i=0;i<files.size();i++){
                StringBuffer stringBuffer = new StringBuffer();
                String line = "";
                try {
                    fileReader = new FileReader(files.get(i).getAbsolutePath());
                    bufferedReader = new BufferedReader(fileReader);
                    while ((line=bufferedReader.readLine()) != null){
                        stringBuffer.append(line);
                    }
                    String data = stringBuffer.toString().replace("Data ","");
                    bufferedReader.close();
                    details.add(new Details(new JSONObject(data)));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            }

        }else {
            Log.v("info ", path.getAbsolutePath()+ " not found");
        }
    }

    public Bitmap getBitMapFromString(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",") + 1),
                Base64.DEFAULT        );
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void doYouWantToRegister(Bitmap bitmap){
        //new Assistant().startAssistant();
        Log.v("info","Start doYouWantToRegister");
        processTextandSpeech textandSpeech = new processTextandSpeech();
        textandSpeech.stepByStep = 1;
        Context context = ModelClass.getActivity().getApplicationContext();
        Intent intent = new Intent(context,processTextandSpeech.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ModelClass.capturedBitmap = bitmap;
        context.startActivity(intent);
        Log.v("info","end doYouWantToRegister");

    }

}