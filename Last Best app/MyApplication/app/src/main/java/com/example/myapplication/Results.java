package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.sceneform.AnchorNode;

import java.util.ArrayList;

public class Results extends AppCompatActivity {

    private ArrayList<Float> retrievedCoordinates;
    private float [][] coordinates;

    private int numPoints;
    private TextView numTv;
    private EditText quantityEditText;
    private Button calculateButton;
    private TextView resultsText;


    private float area;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        retrievedCoordinates = (ArrayList<Float>) getIntent().getSerializableExtra("coordinates");
        numPoints = retrievedCoordinates.size()/3;

        quantityEditText = findViewById(R.id.quantity_edit_text);
        calculateButton = findViewById(R.id.calculate_button);
        resultsText = findViewById(R.id.results_text);

        coordinates = new float[numPoints][3];

        for(int i=0;i<numPoints;i++) {
            for (int j = 0; j < 3; j++) {
                coordinates[i][j] = retrievedCoordinates.get(i * 3 + j);
            }

        }
        if(numPoints<=2){
            area = 0;
        }
        else if(numPoints==3){
            float d1 = calculateDistance(coordinates[0][0]-coordinates[1][0],
                    coordinates[0][1]-coordinates[1][1],
                    coordinates[0][2]-coordinates[1][2]);
            float d2 = calculateDistance(coordinates[1][0]-coordinates[2][0],
                    coordinates[1][1]-coordinates[2][1],
                    coordinates[1][2]-coordinates[2][2]);
            float d3=calculateDistance(coordinates[0][0]-coordinates[2][0],
                    coordinates[0][1]-coordinates[2][1],
                    coordinates[0][2]-coordinates[2][2]);
            float s = (d1+d2+d3)/2;
            area = (float)Math.sqrt(s*(s-d1)*(s-d2)*(s-d3));
        }
        else if(numPoints==4){
            float d1 = calculateDistance(coordinates[0][0]-coordinates[1][0],
                    coordinates[0][1]-coordinates[1][1],
                    coordinates[0][2]-coordinates[1][2]);
            float d2 = calculateDistance(coordinates[1][0]-coordinates[2][0],
                    coordinates[1][1]-coordinates[2][1],
                    coordinates[1][2]-coordinates[2][2]);
            area = d1*d2;
        }
        numTv = findViewById(R.id.number);
        numTv.setText(Float.toString(area*10000)+ " square cm");


        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quant = Integer.parseInt(quantityEditText.getText().toString());
                if(quant!=0){
                    float q = (area*10000)/quant;
                    resultsText.setText(Float.toString(q) + " units are required");
                }
                else{
                    Toast.makeText(Results.this, "Please enter valid quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private float calculateDistance(float x,float y,float z){
        return (float)Math.sqrt(x*x+y*y+z*z);
    }

}