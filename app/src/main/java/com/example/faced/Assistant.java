package com.example.faced;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Assistant {
    processTextandSpeechIMPLMNTATION processTextandSpeechIMPLMNTATIONobj=new processTextandSpeechIMPLMNTATION();
    private static final int FOUND_AS_GUEST=10;
    private static final int WANT_TO_CONTINUE_AS_GUEST=0;
    private static final int WANT_TO_REGISTER=100;
    private static final int ALREADY_REGISTERED=1000;
    static String name,phoneNumber;
    static int  matched=0;
    AlertDialog.Builder dialog;
    TextToSpeech mtts;
    int stepByStep;
    static String userReply=null;
    ArrayList<String> userInputsContains;
    SpeechRecognizer speechRecognizer= SpeechRecognizer.createSpeechRecognizer(ModelClass.getActivity().getApplicationContext());
    Intent speechRecognitionIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    Handler handler=new Handler(Looper.getMainLooper());
    static Map<String,String> qstnAnswrSet = new HashMap<String,String>();

    public void startAssistant(){
        //matched=0;   //// this is for new guest who wants to register     /// this is the value decides we get as intent extras which decides what to do next
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
                Toast.makeText(ModelClass.getActivity().getApplicationContext(),"taking you to quiz",Toast.LENGTH_LONG).show();
                stepByStep = 4;
                getExcel();
            }
            break;
            /*case 2:
            {
                stepByStep = 2;
                startSpeakAndListening("User Input","Welcome to quzi!!","How may i help you?");
            }*/
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
        Log.v("info ","speaking");
        if(title != null) {
            processTextandSpeechIMPLMNTATIONobj
                    .dialogBoxCreation(ModelClass.getActivity() ,
                            ""+title,
                            msg1+"\n"+msg2);
        }
        processTextandSpeechIMPLMNTATIONobj
                .startSpeaking(msg1 + msg2, ModelClass.getActivity().getApplicationContext());

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



                        switch (stepByStep) {

                            case 1: {
                                userReply = stringBuffer.toString();
                                stepByStep = 1;

                                switch (userReply) {
                                    case "no": {

                                        processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                        Toast.makeText(ModelClass.getActivity().getApplicationContext(), "Taking you to quiz", Toast.LENGTH_LONG).show();
                                        getExcel();
                                        stepByStep = 4;
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
                                name = stringBuffer.toString();
                                stepByStep =3;
                                processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                startSpeakAndListening("User Phone No.",
                                        "Speak out your Phone No.","");
                            }break;
                            case 3:
                            {
                                phoneNumber= stringBuffer.toString();
                                processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                boolean status = true;
                                if(status){
                                    FaceReconAPI faceReconAPI = FaceReconAPI.getFaceReconAPI();
                                    FaceReconAPI.convertBitmapToByteArray(CameraAPI.getCameraAPI().getCurrentBitmap());
                                    faceReconAPI.saveJSONFile(name,phoneNumber);
                                    Toast.makeText(ModelClass.getActivity().getApplicationContext(),"taking you to quiz",Toast.LENGTH_LONG).show();
                                    getExcel();
                                    stepByStep = 4;
                                }

                            }break;
                            case 4:
                            {
                                String question= stringBuffer.toString().toLowerCase();
                                Log.v("info ",question);
                                Set<String> keys = Assistant.qstnAnswrSet.keySet();
                                for (String key : keys) {
                                    if(question.contains(key.toLowerCase())){
                                        Log.v("info"," ans: "+Assistant.qstnAnswrSet.get(key));
                                        speakingBox(null,Assistant.qstnAnswrSet.get(key),"");
                                        break;
                                    }
                                    //Log.v("info ", "question: "+ key +" & Answer: "+Assistant.qstnAnswrSet.get(key));
                                }
                                stepByStep = 5;

                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startSpeakAndListening(null,"Do you have any further questions?","Yes or No");
                                    }
                                },3000);
                            }
                            case 5://exit code
                            {
                                userReply = stringBuffer.toString();
                                switch (userReply) {
                                    case "no": {
                                        stepByStep = 1;
                                        CameraAPI.getCameraAPI().captureImage(3000);
                                    }
                                    break;
                                    case "yes": {
                                        stepByStep = 4;
                                        startSpeakAndListening(null,"I am glad to hear that!!","How may i help you?");
                                    }
                                    break;

                                }
                            }
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

    private void getExcel() {
        Log.v("info ","getExcel");
        DataInputStream in = null;
        FileInputStream fis = null;
        File file = new File(ModelClass.getActivity().getExternalFilesDir(null)+"/Excel/Book.xls");
        Log.v("info ",file.exists()+" "+file.getAbsolutePath());
        if (file.exists()) {
            Log.v("info ",file.toString());
            if (file != null) {
                try {
                    fis = new FileInputStream(file);
                    Log.v("info ","1");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (fis != null) {
                    in = new DataInputStream(fis);
                    Log.v("info ","2");
                }

            }
        }
        try {
            qstnAnswrSet.clear();
            Log.v("info ","3");
            /*
            //For reading .xlsx file
           /// OPCPackage pkg = new OPCPackage(in);
            XSSFWorkbook myWorkBook = new XSSFWorkbook(in);
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

             */


            //For reading .xls file
            // Create a POI File System object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(in);
            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            // We now need something to iterate through the cells.
            Iterator<Row> rowIter = mySheet.rowIterator();
            int rowno = 0;
            //   textView.append("\n");
            // db.deleteTableData();
            while (rowIter.hasNext()) {
                Log.e("info>>rowno", " row no " + rowno);
                HSSFRow myRow = (HSSFRow) rowIter.next();
                if (rowno != 0) {
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    int colno = 0;
                    String sno = "", question = "", ans = "";
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        if (colno == 0) {
                            sno = myCell.toString();
                        } else if (colno == 1) {
                            question = myCell.toString();
                        } else if (colno == 2) {
                            ans = myCell.toString();
                            Assistant.qstnAnswrSet.put(question,ans);
                        }
                        colno++;
                        //       Log.e(">>excel record", " Index :" + myCell.getColumnIndex() + " -- " + myCell.toString());
                    }

                    /// db.insertQues(question, ans);
                    Log.v("info>>excel record", " Index :" + sno + " " + question + " " + ans + "");

                }
                rowno++;
            }
        } catch (Exception e) {
            Log.v("info ", "error "+ e.getMessage());
        }
        Log.v("info ", "set size "+ Assistant.qstnAnswrSet.size());
       Set<String> keys = Assistant.qstnAnswrSet.keySet();
        for (String key : keys) {
            Log.v("info ", "question: "+ key +" & Answer: "+Assistant.qstnAnswrSet.get(key));
        }
        startSpeakAndListening(null,"Welcome to quiz!!","How may i help you?");
    }

}
