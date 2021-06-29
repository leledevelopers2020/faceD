package com.example.faced;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.face.FaceDetector;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    private SurfaceView surfaceView;
    private FaceDetector detector;
    CameraAPI cameraAPI;
    FaceReconAPI faceReconAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceView);
        imageView = findViewById(R.id.iv_picture);
        cameraAPI = CameraAPI.getCameraAPI();
        cameraAPI.setActivity(MainActivity.this);
        faceReconAPI = FaceReconAPI.getFaceReconAPI();
        cameraAPI.setSurfaceView(surfaceView);
        ModelClass.imageView = imageView;
        ModelClass.activity = MainActivity.this;
        cameraAPI.loadDetector(getApplicationContext());
        detector = cameraAPI.getDetector();
        faceReconAPI.loadFaceReconSetup();
        Log.v("info ", "faceReconAPI in MainActivity---> " + faceReconAPI.toString());
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
                    cameraAPI.captureImage(3000);
                } else {
                    Log.v("info ", "loadPermissions failed");
                    //loadPermissions();
                }
            }
        }
        //cameraAPI.captureImage();
    }

}