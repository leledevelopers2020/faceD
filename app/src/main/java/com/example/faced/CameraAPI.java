package com.example.faced;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.CAMERA;

public class CameraAPI {
    private CameraAPIListener listener;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private SurfaceHolder surfaceHolder;
    FaceDetector detector;
    private String[] neededPermissions = new String[]{CAMERA};
    Activity activity;
    ModelClass modelClass;
    FaceReconAPI faceReconAPI = FaceReconAPI.getFaceReconAPI();

    public CameraAPI(Activity activity) {
        this.activity = activity;
    }

    public void setImageView(ImageView imageView) {
        modelClass.setImageView(imageView);
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public void setListener(CameraAPIListener listener) {
        this.listener = listener;
    }

    public FaceDetector getDetector() {
        return detector;
    }

    public void loadDetector(Context context){
        detector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
                .build();
    }


    public boolean checkPermission() {
        ArrayList<String> permissionsNotGranted = new ArrayList<>();
        for (String permission : neededPermissions) {
            if (ContextCompat.checkSelfPermission(this.activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.add(permission);
            }
        }
        if (!permissionsNotGranted.isEmpty()) {
            boolean shouldShowAlert = false;
            for (String permission : permissionsNotGranted) {
                shouldShowAlert = ActivityCompat.shouldShowRequestPermissionRationale(this.activity, permission);
            }
            if (shouldShowAlert) {
                showPermissionAlert(permissionsNotGranted.toArray(new String[0]));
            } else {
                requestPermissions(permissionsNotGranted.toArray(new String[0]));
            }
            return false;
        }
        return true;
    }

    public void showPermissionAlert(final String[] permissions) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.activity);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission Required");
        alertBuilder.setMessage("Camea permission is required to move forward.");
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(permissions);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    public void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this.activity, permissions, 1001);
    }

    public void setupSurfaceHolder() {
        cameraSource = new CameraSource.Builder(this.activity, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();

        surfaceHolder = this.getSurfaceView().getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    cameraSource.start(surfaceHolder);
                    detector.setProcessor(new LargestFaceFocusingProcessor(detector,
                            new Tracker<Face>()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
    }


    public void captureImage() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ModelClass.imageView.setImageBitmap(null);
                ModelClass.imageView.setVisibility(View.GONE);
                clickImage();
            }
        }, 3000);
    }


    private void clickImage() {
        if (cameraSource != null) {
            cameraSource.takePicture(/*shutterCallback*/null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    try {
                        processCameraPicture(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    synchronized private void processCameraPicture(Bitmap bitmap) throws Exception {
        if (detector.isOperational() && bitmap != null) {
            //Bitmap editedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Face> faces = detector.detect(frame);

            if (faces.size() == 0) {
                Log.d("info no faces", "no of faces = " + faces.size());
                Toast.makeText(activity.getApplicationContext(),
                        "Scan Failed: Found nothing to scan",Toast.LENGTH_LONG).show();
                        captureImage();
            } else {
                Log.d("info faces", "no of faces = " + faces.size());
                loadImagesFromDir();
                //ModelClass.imageView.setImageBitmap(bitmap);
                ModelClass.imageView.setVisibility(View.VISIBLE);
                Log.v("info ","faceReconAPI in CameraApi---> "+faceReconAPI.toString());
                faceReconAPI.setOriBitmap(bitmap);
                faceReconAPI.recongize(bitmap);
               }
        } else {
            Log.d("info detector ", "Could not set up the detector!");
        }
    }

    synchronized private void loadImagesFromDir() {
        File path = activity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (path.exists()) {
            ModelClass.setListFile(Arrays.asList(path.listFiles()));
            List<File> files = ModelClass.getListFile();
            //Log.v("info_files_size", " "+ModelClass.getListFile().size());
            if(ModelClass.getListFile().size()==0){
                Toast.makeText(ModelClass.getActivity().getApplicationContext(),"No Images are found" +
                        " to compare. Please take a new picture",Toast.LENGTH_LONG).show();
                captureImage();
            }
        }else {
            Log.v("info ", path.getAbsolutePath()+ " not found");
        }

    }

    public interface  CameraAPIListener{
        public void loadCamera();
    }

    public void startCamera() {
        if (listener != null)
            listener.loadCamera();
    }

}
