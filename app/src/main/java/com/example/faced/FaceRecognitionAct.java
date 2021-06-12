package com.example.faced;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class FaceRecognitionAct extends AppCompatActivity {

    public Bitmap oriBitmap = null;

    private ImageView originalImage, testImage, randomImage;
    private Button verify, next;
    private TextView resultText;

    ArrayList<String> imageFiles;// list of file paths
    ArrayList<Double> listMeasures;
    ArrayList<Bitmap> allImagesBitmaps;
    String currentPhotoPath;
    byte[] convertedByteArray;
    File file;

    FaceReconImp faceReconImp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        faceReconImp = new FaceReconImp();
        imageFiles = new ArrayList<String>();
        listMeasures = new ArrayList<Double>();
        allImagesBitmaps = new ArrayList<Bitmap>();

        Intent intent = getIntent();
        oriBitmap = (Bitmap) intent.getParcelableExtra("Bitmap");


        initComponents();

    }

    private void initComponents() {
        originalImage = (ImageView) findViewById(R.id.originalImage);
        testImage = (ImageView) findViewById(R.id.testImage);
        verify = (Button) findViewById(R.id.verify);
        next = (Button) findViewById(R.id.nextPaga);
        resultText = (TextView) findViewById(R.id.result);
        randomImage = (ImageView) findViewById(R.id.randomImg);

        faceReconImp.loadCompents(originalImage, randomImage, resultText);

        faceReconImp.loadtflite(getApplicationContext());

        faceReconImp.face_detector(oriBitmap, "original");
        convertedByteArray = convertBitmapToByteArray(oriBitmap);
        file = arrayToFile(convertedByteArray);

        if(ModelClass.getListFile().size()==0){
            resultText.setText("No Images found to compare");
        } else {
            loadAllImages();
        }

    }
 /*   private void saveImage(Bitmap bitmap) {
        OutputStream outputStream = null;
        //Creates Pictures folder in the apk files
        File path = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.v("path ", path.getAbsolutePath());
        //Creates SavedImages folder in Pictures folder of the apk files
        File dir = new File(path.getAbsoluteFile() + "/SavedImages/");
        //Creates the image file in the SavedImages folder
        File file = new File(dir, System.currentTimeMillis() + ".jpg");
        try {
            outputStream = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 300, outputStream);
        Toast.makeText(getApplicationContext(), "Image saved", Toast.LENGTH_LONG).show();
        try {
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
*/

    synchronized private File arrayToFile(byte[] convertedByteArray) {
        File file = null;
        FileOutputStream fos = null;

        try {
            file = createImageFile();
            if (file != null) {
                fos = new FileOutputStream(file);
                // Writes bytes from the specified byte array to this file output stream
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

    synchronized private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    synchronized private void loadAllImages() {
        allImagesBitmaps.clear();
        List<File> files = ModelClass.getListFile();
        if (files != null && files.size()>0) {
            imageFiles.clear();
            for (int i = 0; i < files.size(); i++) {
                //imageFiles.add(listFile[i].getAbsolutePath());
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
        if (oriBitmap != null) {
            faceReconImp.setPreviousDistance(0.0);
            faceReconImp.setResult(resultText);
            faceReconImp.reSetRandomImage();
            if (allImagesBitmaps.size() != 0) {
                compareWithActualImage();
                resultText.setText("Result:");
            } else {
                resultText.setText("No images to compare");
                Toast.makeText(getApplicationContext(), "No images are found to compare, Please capture " +
                        "om", Toast.LENGTH_LONG).show();
            }


        } else {
            Toast.makeText(getApplicationContext(), "Plase select the original image to compare", Toast.LENGTH_LONG).show();
        }
    }

    synchronized private void compareWithActualImage() {
        // FaceReconImp reconImp = new FaceReconImp(0.0);
        // Log.v("val albitmap size ",allImagesBitmaps.size()+"");
        for (int j = 0; j < allImagesBitmaps.size(); j++) {
            faceReconImp.face_detector(allImagesBitmaps.get(j), "test");
            // Bitmap decodeFile = BitmapFactory.decodeFile(imageFile);
            // new Thread(new FaceReconImp(allImagesBitmaps.get(j),"test",j)).start();
            //  new Thread(new FaceReconImp(j)).start();


        }
    }


}