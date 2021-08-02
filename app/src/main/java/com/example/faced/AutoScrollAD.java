package com.example.faced;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.smarteist.autoimageslider.SliderView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoScrollAD extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_scroll_ad);
        SliderView sliderView = findViewById(R.id.slider);
        ArrayList<SliderData> sliderDataArrayList = new ArrayList<>();

        File path = getApplicationContext().getApplicationContext().getExternalFilesDir("Images/Ads");
        if (path.exists()) {
            List<File> files = Arrays.asList(path.listFiles());
            // adding the urls inside array list
            for(int i=0;i<files.size();i++) {
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
}