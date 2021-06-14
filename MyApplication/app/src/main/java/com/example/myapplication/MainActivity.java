package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private String [] buttonString ;
    private List<String> buttonArrayList = new ArrayList<String>();
    private Button toMeasurement ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"On create");

        buttonString = getResources().getStringArray(R.array.arcore_measurement_buttons);
        buttonArrayList = Arrays.asList(buttonString);

        toMeasurement = findViewById(R.id.to_measurement);
        String text = buttonString[0];
        toMeasurement.setText(text);

        toMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Button clicked");
                Intent intent = new Intent(getApplicationContext(),Measurement.class);
                startActivity(intent);
            }
        });
    }
}