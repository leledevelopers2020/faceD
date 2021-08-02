package com.example.faced;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class FaceReconImp implements FaceRecon {
    static Interpreter tflite;
    protected int imageSizeX;
    protected int imageSizeY;
    private Context context;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f;
    public Bitmap cropped,originalBitmap;

    static float[][] ori_embedding = null;
    float[][] test_embedding = null;
    private double previousDistance = 0.0;
    private int matchingIndex;
    //private Bitmap previousBitmap;

    public FaceReconImp() {
        ori_embedding = new float[1][128];
    }

    @Override
    public void loadtflite(Context context) {
        this.context = context;
        try {
            tflite = new Interpreter(loadmodelFile(this.context));
            Log.v("tflite:", tflite.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public MappedByteBuffer loadmodelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("Qfacenet.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength);
    }

    @Override
    synchronized public void face_detector(Bitmap bitmap, String imagetype,int imageIndex) {
        Log.v("val type = ", "image type bit" + bitmap);
        if(imagetype.equals("original"))
        {
            originalBitmap = bitmap;
        }

        InputImage image = null;
        try {
            image = InputImage.fromBitmap(bitmap, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.v("val imagebitmap ", "=" + bitmap);
        FaceDetector detector = FaceDetection.getClient();
        detector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                // Task completed successfully
                                Log.v("val no of faces ", "=" + faces.size());
                               /* if(faces.size() == 0) {
                                    oriBitmap = null;

                                    new File(photoFile.getAbsoluteFile().toString()).delete();
                                    originalImage.setImageBitmap(oriBitmap);
                                    Toast.makeText(context, "Can not able to detect the face" +
                                                    ",Please try again",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }*/
                                Log.v("val faces ", "Proceed futher ");
                                for (Face face : faces) {
                                    Rect bounds = face.getBoundingBox();
                                    Log.v("val l: ", bounds.left + "");
                                    Log.v("val t: ", bounds.top + "");
                                    Log.v("bound w: ", bounds.width() + "");
                                    Log.v("bound h: ", bounds.height() + "");
                                    cropped = Bitmap.createBitmap(bitmap, bounds.left, bounds.top,
                                            bounds.width(), bounds.height());
                                    get_embaddings(cropped, imagetype,imageIndex);
                                }
                            }

                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
    }

    @Override
    synchronized public void get_embaddings(Bitmap bitmap, String imagetype,int imageIndex) {
        test_embedding = new float[1][128];
        TensorImage inputImageBuffer;
        float[][] embedding = new float[1][128];

        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        Log.v("val = imageSizeX = ", imageSizeX + "");
        Log.v("val = imageSizeY = ", imageSizeY + "");
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        inputImageBuffer = new TensorImage(imageDataType);

        inputImageBuffer = loadImage(bitmap, inputImageBuffer);

        tflite.run(inputImageBuffer.getBuffer(), embedding);

        if (imagetype.equals("original"))
        {    ori_embedding = embedding;
            Log.v("val-imageIndex = ", imageIndex + "");
        }
        else if (imagetype.equals("test")) {
            test_embedding = embedding;
            double val = calculate_distance(ori_embedding, test_embedding);
            Log.v("val-value = ", val + "");
            Log.v("val-imageIndex = ", imageIndex + "");
            if (previousDistance == 0.0) {
                previousDistance = val;
                //previousBitmap = bitmap;
                matchingIndex = imageIndex;
            }
            if (previousDistance >= val) {
                previousDistance = val;
                matchingIndex = imageIndex;
               /* if (previousDistance < 6.0) {

                   // ModelClass.imageView.setImageBitmap(bitmap);
                    ///noOfMatchImage++;
                } else {
                    //ModelClass.imageView.setImageBitmap(null);
                    if (noOfMatchImage == 0) {
                       // Toast.makeText(ModelClass.getActivity().getApplicationContext(),
                                //"No matches found!!", Toast.LENGTH_LONG).show();
                    }
                }*/
            }

            if(imageIndex == FaceReconAPI.userDetails.size()-1){
                Log.v("val- = ", "Found Match with " +matchingIndex);
                if(previousDistance < 6.0){
                    ModelClass.imageView.setImageBitmap(FaceReconAPI.userDetails.get(matchingIndex).getUserImage());
                    processTextandSpeech.matched = 1;
                    Log.v("val- ", "Welcome "+FaceReconAPI.userDetails.get(matchingIndex).getName());
                } else {
                    processTextandSpeech.matched = 0;
                    ModelClass.imageView.setImageBitmap(null);
                    Toast.makeText(ModelClass.getActivity().getApplicationContext(),
                            "No matches found!!", Toast.LENGTH_LONG).show();
                }
                //new Assistant().startAssistant();
                Context context = ModelClass.getActivity().getApplicationContext();
                Intent intent = new Intent(context,processTextandSpeech.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bitmap sndBit = Bitmap.createScaledBitmap(originalBitmap,200,400,false);
                ModelClass.capturedBitmap = sndBit;
                //intent.putExtra("capturedImage",sndBit);
                context.startActivity(intent);
                //start camera again
                //CameraAPI.getCameraAPI().captureImage(5000);
            }
            Log.v("val-preDistInit = ", previousDistance + "");
        }
    }

    private TensorImage loadImage(final Bitmap bitmap, TensorImage inputImageBuffer) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(getPreprocessNormalizeOp())
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }

    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    synchronized public double calculate_distance(float[][] ori_embedding, float[][] test_embedding) {
        double sum = 0.0;
        for (int i = 0; i < 128; i++) {
            sum = sum + Math.pow((ori_embedding[0][i] - test_embedding[0][i]), 2.0);
        }
        return Math.sqrt(sum);
    }

    public void setPreviousDistance(double previousDistance) {
        this.previousDistance = previousDistance;
    }

    public void setMatchingIndex(int matchingIndex) {
        this.matchingIndex = matchingIndex;
    }
}