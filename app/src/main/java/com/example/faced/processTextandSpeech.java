package com.example.faced;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class processTextandSpeech extends AppCompatActivity {
    processTextandSpeechIMPLMNTATION processTextandSpeechIMPLMNTATIONobj = new processTextandSpeechIMPLMNTATION();
    private static final int FOUND_AS_GUEST = 10;
    private static final int WANT_TO_CONTINUE_AS_GUEST = 0;
    private static final int WANT_TO_REGISTER = 100;
    private static final int ALREADY_REGISTERED = 1000;
    static String name, phoneNumber;
    static int matched = 0;

    int stepByStep;
    static String userReply = null;

    SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(ModelClass.getActivity().getApplicationContext());
    Intent speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    Handler handler = new Handler(Looper.getMainLooper());
    static Map<String, String> qstnAnswrSet = new HashMap<String, String>();
    static int errCount = 0;
    TextView inputLine;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_textand_speech);
        inputLine = (TextView) findViewById(R.id.speechToText);
        imageView = findViewById(R.id.userImage);
        //Bitmap bitmap = (Bitmap) new Intent().getParcelableExtra("capturedImage");
        Bitmap bitmap = ModelClass.capturedBitmap;
        imageView.setImageBitmap(bitmap);
        startAssistant();
        SliderView sliderView = findViewById(R.id.slideshow);
        ArrayList<SliderData> sliderDataArrayList = new ArrayList<>();

        File path = getApplicationContext().getApplicationContext().getExternalFilesDir("Images/Ads");
        if (path.exists()) {
            List<File> files = Arrays.asList(path.listFiles());
            // adding the urls inside array list
            for (int i = 0; i < files.size(); i++) {
                sliderDataArrayList.add(new SliderData(files.get(i).getAbsolutePath()));
            }
        }

        // passing this array list inside our adapter class.
        SliderAdapter adapter = new SliderAdapter(this, sliderDataArrayList);

        // below method is used to set auto cycle direction in left to
        // right direction you can change according to requirement.
        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);

        // below method is used to
        // setadapter to sliderview.
        sliderView.setSliderAdapter(adapter);

        // below method is use to set
        // scroll time in seconds.
        sliderView.setScrollTimeInSec(3);

        // to set it scrollable automatically
        // we use below method.
        sliderView.setAutoCycle(true);

        // to start autocycle below method is used.
        sliderView.startAutoCycle();

    }

    public void startAssistant() {
        //matched=0;   //// this is for new guest who wants to register     /// this is the value decides we get as intent extras which decides what to do next
        switch (matched) {
            case 0: {
                stepByStep = 1;
                startSpeakAndListening("User Input", "Do you want to register", "Yes or No");
            }
            break;
            case 1: {
                Toast.makeText(ModelClass.getActivity().getApplicationContext(), "taking you to quiz", Toast.LENGTH_LONG).show();
                stepByStep = 4;
                getExcel();
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + matched);
        }
    }

    private void startSpeakAndListening(String title, String msg1, String msg2) {
        speakingBox(title + "", msg1 + "", msg2 + "");
        startListening();
    }

    private void speakingBox(String title, String msg1, String msg2) {
        Log.v("info ", "speaking");
        if (title != null) {
            processTextandSpeechIMPLMNTATIONobj
                    .dialogBoxCreation(ModelClass.getActivity(),
                            "" + title,
                            msg1 + "\n" + msg2);
        }
        processTextandSpeechIMPLMNTATIONobj
                .startSpeaking(msg1 + msg2, ModelClass.getActivity().getApplicationContext());

    }

    private void speakingOnly(String msg1, String msg2) {
        processTextandSpeechIMPLMNTATIONobj
                .startSpeaking(msg1 + msg2, ModelClass.getActivity().getApplicationContext());
        startListening();
    }

    private void startListening() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                speechRecognizer.startListening(speechRecognitionIntent);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        speechRecognizer.stopListening();
                    }
                }, 10000);
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
                                    if (errCount == 1) {
                                        // exit the loop and finish
                                    } else {
                                        errCount++;
                                        startSpeakAndListening("User Input",
                                                "Do you want to register", "Yes or No");
                                    }

                                }
                                break;
                                case 2: {
                                    if (errCount == 1) {
                                        // exit the loop and finish
                                    } else {
                                        errCount++;
                                        startSpeakAndListening("User Name",
                                                "Speak Out Your Name", "");
                                    }
                                }
                                break;
                                case 3: {
                                    if (errCount == 1) {
                                        // exit the loop and finish
                                    } else {
                                        errCount++;
                                        startSpeakAndListening("User Phone No.",
                                                "Speak out your Phone Number", "");
                                    }
                                }
                                break;
                                case 4: {
                                    if (errCount == 1) {
                                        // exit the loop and finish
                                    } else {
                                        errCount++;
                                        speakingOnly("Do you have any further questions?", "Yes or No");
                                    }
                                }
                                break;
                                case 5: {
                                    if (errCount == 1) {
                                        // exit the loop and finish
                                    } else {
                                        errCount++;
                                        speakingOnly("I am glad to hear that!!", "How may i help you?");
                                    }
                                }
                                break;
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

                            /*
                            Takes input of yes or no from user to
                             register new user
                             */
                            case 1: {
                                try {
                                    //if user replies yes
                                    if (stringBuffer.toString().contains("yes")) {
                                        //userReply = "yes";
                                        errCount = 0;

                                        processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                        stepByStep = 2;
                                        speechToText(0, "Yes");
                                        startSpeakAndListening("User Name", "Speak Out Your Name", "");

                                    }
                                    //if user replies no
                                    else if (stringBuffer.toString().contains("no")) {
                                        //userReply = "no";
                                        errCount = 0;

                                        speechToText(0, "No");
                                        processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                        getExcel();
                                        stepByStep = 4;
                                    }
                                    //if user replied other than yes or no
                                    else {
                                        //throw new NullPointerException();

                                        speakingOnly("Please provide the input as yes or no", "");
                                        //call on error method
                                        // not a valid input
                                        //input doesn't matches plz provide correct input
                                    }
                                    // stepByStep = 1;

                                } catch (NullPointerException e) {
                                    //code to trigger for other than yes or no
                                }
                            }
                            break;


                            /*
                                Takes the name as input and ask user for phone number as input
                             */
                            case 2: {
                                errCount = 0;
                                name = stringBuffer.toString();
                                stepByStep = 3;
                                processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                speechToText(1, name);
                                startSpeakAndListening("User Phone No.",
                                        "Speak out your Phone Number", "");
                            }
                            break;

                             /*
                                Takes the phone number as input and starts quiz by asking further
                             */
                            case 3: {
                                phoneNumber = stringBuffer.toString();
                                if (phoneNumber.length() == 10) {
                                    errCount = 0;
                                    speechToText(1, phoneNumber);
                                    processTextandSpeechIMPLMNTATIONobj.alertDismiss();
                                    boolean status = true;
                                    if (status) {
                                        FaceReconAPI faceReconAPI = FaceReconAPI.getFaceReconAPI();
                                        FaceReconAPI.convertBitmapToByteArray(CameraAPI.getCameraAPI().getCurrentBitmap());
                                        faceReconAPI.saveJSONFile(name, phoneNumber);
                                        getExcel();
                                        stepByStep = 4;
                                    } else {
                                        // input not valid enter a valid input
                                    }
                                }

                            }
                            break;

                            /*
                                Starts quiz here.
                             */
                            case 4: {
                                String question = stringBuffer.toString().toLowerCase();
                                speechToText(1, question);
                                Log.v("info ", question);
                                Set<String> keys = processTextandSpeech.qstnAnswrSet.keySet();
                                for (String key : keys) {
                                    if (question.contains(key.toLowerCase())) {
                                        Log.v("info", " ans: " + processTextandSpeech.qstnAnswrSet.get(key));
                                        speakingOnly(Assistant.qstnAnswrSet.get(key), "");
                                        speechToText(0, Assistant.qstnAnswrSet.get(key));
                                        break;
                                    }
                                }
                                stepByStep = 5;

                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        speakingOnly("Do you have any further questions?", "Yes or No");
                                        speechToText(0, "Do you have any further questions?Yes or No");
                                    }
                                }, 10000);
                            }
                            case 5://exit code
                            {
                                if (stringBuffer.toString().contains("yes")) {
                                    userReply = "yes";
                                    errCount = 0;
                                } else if (stringBuffer.toString().contains("no")) {
                                    userReply = "no";
                                    errCount = 0;
                                } else {
                                    // not a valid input
                                }
                                try {
                                    switch (userReply) {
                                        case "no": {
                                            speechToText(1, userReply);
                                            stepByStep = 1;
                                            finish();
                                        }
                                        break;
                                        case "yes": {
                                            speechToText(1, userReply);
                                            stepByStep = 4;
                                            speakingOnly("", "How may i help you?");
                                            speechToText(0, "How may i help you?");
                                        }
                                        break;

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
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
        Log.v("info ", "getExcel");
        DataInputStream in = null;
        FileInputStream fis = null;
        File file = new File(ModelClass.getActivity().getExternalFilesDir(null) + "/Excel/Book.xls");
        Log.v("info ", file.exists() + " " + file.getAbsolutePath());
        if (file.exists()) {
            Log.v("info ", file.toString());
            if (file != null) {
                try {
                    fis = new FileInputStream(file);
                    Log.v("info ", "1");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (fis != null) {
                    in = new DataInputStream(fis);
                    Log.v("info ", "2");
                }

            }
        }
        try {
            qstnAnswrSet.clear();
            Log.v("info ", "3");
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
                            processTextandSpeech.qstnAnswrSet.put(question, ans);
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
            Log.v("info ", "error " + e.getMessage());
        }
        Log.v("info ", "set size " + Assistant.qstnAnswrSet.size());
        Set<String> keys = Assistant.qstnAnswrSet.keySet();
        for (String key : keys) {
            Log.v("info ", "question: " + key + " & Answer: " + Assistant.qstnAnswrSet.get(key));
        }
        speakingOnly("Welcome to quiz!!", "How may i help you?");
        speechToText(0, "Welcome to quiz!!How may i help you?");
    }


    private void speechToText(int response, String output) {
        if (response == 0) //computer speaking data
        {
            inputLine.setText("Assistant Speaking: " + output);
        } else if (response == 1) /// human input
        {
            inputLine.setText("User Spoke: " + output);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}