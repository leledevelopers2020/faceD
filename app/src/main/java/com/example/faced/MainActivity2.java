package com.example.faced;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;

import static android.Manifest.permission.CAMERA;

public class MainActivity2 extends AppCompatActivity {
    ImageView imageView;
    private String[] neededPermissions = new String[]{CAMERA};
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private SurfaceHolder surfaceHolder;
    private FaceDetector detector;
    CameraAPI cameraAPI;
    FaceReconAPI faceReconAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        surfaceView = findViewById(R.id.surfaceView);
        imageView = findViewById(R.id.iv_picture);
        cameraAPI = new CameraAPI(MainActivity2.this);
        faceReconAPI = FaceReconAPI.getFaceReconAPI();
        cameraAPI.setSurfaceView(surfaceView);
        ModelClass.imageView = imageView;
        ModelClass.activity = MainActivity2.this;
        cameraAPI.loadDetector(getApplicationContext());
        detector = cameraAPI.getDetector();
        faceReconAPI.loadFaceReconSetup();
        Log.v("info ","faceReconAPI in MainActivity---> "+faceReconAPI.toString());
    }


    @Override
    protected void onStart() {
        super.onStart();
        startMainProcess();
    }

    private void startMainProcess() {
        if (!detector.isOperational()) {
            Log.w("MainActivity", "Detector Dependencies are not yet available");
        } else {
            Log.w("MainActivity", "Detector Dependencies are available");
            if (surfaceView != null) {
                //loadPermissions();
                boolean result = cameraAPI.checkPermission();
                if (result) {
                    Log.v("info ", "loadPermissions success");
                    findViewById(R.id.surfaceView).setVisibility(View.VISIBLE);
                    cameraAPI.setupSurfaceHolder();
                    cameraAPI.captureImage();
                } else {
                    Log.v("info ", "loadPermissions failed");
                    //loadPermissions();
                }
            }
        }
        //cameraAPI.captureImage();
    }

}