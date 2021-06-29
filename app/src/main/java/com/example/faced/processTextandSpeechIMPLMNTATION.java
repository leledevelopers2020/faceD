package com.example.faced;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Locale;

public class processTextandSpeechIMPLMNTATION implements processTextandSpeechINTRFCE
{
    Context context;
    TextToSpeech mtts;
    AlertDialog.Builder dialog;
    AlertDialog alertDialog;
    SpeechRecognizer speechRecognizer;
    String userReply=null;

    @Override
    public void startSpeaking(String data,Context context) {
        this.context=context;
        String receivedString=data;
        mtts=new TextToSpeech( this.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mtts.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        speak(receivedString);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    @Override
    public void dialogBoxCreation(Context context,String title, String message) {
        this.context=context;
        dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message);
        alertDialog = dialog.create();
        alertDialog.show();

    }

    private void speak(String dataString )
    {
        String data=dataString;
        float pitch = 1.0f;
        float speed =0.5f;
        mtts.setPitch(pitch);
        mtts.setSpeechRate(speed);
        mtts.speak(data, TextToSpeech.QUEUE_FLUSH, null);
    }
    public void alertDismiss()
    {
        if ( alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}