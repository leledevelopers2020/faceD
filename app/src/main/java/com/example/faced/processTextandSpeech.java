package com.example.faced;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.internal.common.zzh;

import java.util.ArrayList;
import java.util.Locale;

public class processTextandSpeech extends AppCompatActivity {
    processTextandSpeechIMPLMNTATION processTextandSpeechIMPLMNTATIONobj=new processTextandSpeechIMPLMNTATION();
    private static final int FOUND_AS_GUEST=10;
    private static final int WANT_TO_CONTINUE_AS_GUEST=0;
    private static final int WANT_TO_REGISTER=100;
    private static final int ALREADY_REGISTERED=1000;
    int  matched;
    AlertDialog.Builder dialog;
    TextToSpeech mtts;
    int stepByStep;
    static String userReply=null;
    ArrayList<String> userInputsContains;
    SpeechRecognizer speechRecognizer= SpeechRecognizer.createSpeechRecognizer(processTextandSpeech.this);
    Intent speechRecognitionIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    Handler handler=new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_textand_speech);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},1);
        }

        // userInputsContains=new ArrayList<String>(){};

        matched=0;   //// this is for new guest who wants to register     /// this is the value decides we get as intent extras which decides what to do next
        switch(matched)
        {
            case 0:
            {
                stepByStep=1;
                startSpeakAndListening("User Input","Do you want to register","Yes or No");
            }
            break;
            case  1:
            {
                Toast.makeText(processTextandSpeech.this,"taking you to quiz",Toast.LENGTH_LONG).show();
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + matched);
        }

    }

    private void startSpeakAndListening(String title,String msg1,String msg2) {
        speakingBox(title+"",msg1+"",msg2+"");
        startListening();
    }


    private void speakingBox(String title,String msg1,String msg2)
    {
        processTextandSpeechIMPLMNTATIONobj
                .dialogBoxCreation(processTextandSpeech.this ,
                        ""+title,
                        msg1+"\n"+msg2);
        processTextandSpeechIMPLMNTATIONobj
                .startSpeaking(msg1+msg2, getApplicationContext());
    }
    private void startListening()
    {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                speechRecognizer.startListening(speechRecognitionIntent);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        speechRecognizer.stopListening();
                    }
                },10000);
                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {

                    }

                    @Override
                    public void onBeginningOfSpeech() {

                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {

                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {

                    }

                    @Override
                    public void onEndOfSpeech() {

                    }

                    @Override
                    public void onError(int error) {
                        if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                            switch (stepByStep) {
                                case 1: {
                                    startSpeakAndListening("User Input",
                                            "Do you want to register", "Yes or No");
                                }break;
                                case 2:
                                {
                                    startSpeakAndListening("User Name",
                                            "Speak Out Your Name","");
                                }break;
                                case 3:
                                {
                                    startSpeakAndListening("User Phone No.",
                                            "Speak out your Phone No.","");
                                }break;
                            }
                        }

                    }

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> data = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                        StringBuffer stringBuffer = new StringBuffer();
                        for (String s : data) {
                            stringBuffer.append(s);
                        }


                        userReply = stringBuffer.toString();
                        switch (stepByStep) {

                            case 1: {

                                stepByStep = 1;

                                switch (userReply) {
                                    case "no": {

                                        processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                        Toast.makeText(processTextandSpeech.this, "Taking you to quiz", Toast.LENGTH_LONG).show();

                                    }
                                    break;
                                    case "yes": {

                                        processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                        stepByStep = 2;
                                        startSpeakAndListening("User Name", "Speak Out Your Name", "");
                                    }
                                    break;

                                }
                            }break;
                            case 2:
                            {
                                stepByStep =3;
                                processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                startSpeakAndListening("User Phone No.",
                                        "Speak out your Phone No.","");
                            }break;
                            case 3:
                            {
                                processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                Toast.makeText(processTextandSpeech.this,"taking you to quiz",Toast.LENGTH_LONG).show();
                            }break;
                        }
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {

                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {

                    }
                });

            }
        }, 8000);

    }

}