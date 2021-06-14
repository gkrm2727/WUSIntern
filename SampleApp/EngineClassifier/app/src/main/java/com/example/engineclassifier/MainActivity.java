package com.example.engineclassifier;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button cameraButton;
    Button galleryButton;
    Button predictButton;
    ImageView imView;
    TextView infoTextView;
    Button moreInfoButton;



    String ipv4Address;
    String portNumber;

    String selectedImagePath;
    String detectedPart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraButton = findViewById(R.id.camera_button);
        galleryButton = findViewById(R.id.gallery_button);
        imView = findViewById(R.id.selected_image);
        predictButton = findViewById(R.id.predict_button);
        infoTextView = findViewById(R.id.prediction_text);
        moreInfoButton = findViewById(R.id.info_button);



        ipv4Address = getResources().getString(R.string.ipv4Address);
        portNumber = getResources().getString(R.string.portNumber);

    }


    public void cameraButtonListener(View view) {
        ImagePicker.Companion.with(this).cameraOnly().start();
    }

    public void galleryButtonListener(View view) {
        ImagePicker.Companion.with(this).galleryOnly().start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Uri uri = data.getData();
            selectedImagePath = uri.getPath();
            Toast.makeText(this, "Selected: "+ selectedImagePath, Toast.LENGTH_SHORT).show();
            imView.setVisibility(View.VISIBLE);
            predictButton.setVisibility(View.VISIBLE);
            imView.setImageURI(uri);
        }
        else{
            Toast.makeText(this, "Failed!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void predictButtonListener(View view) {
        String postUrl= "http://"+ipv4Address+":"+portNumber+"/";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Read BitMap by file path
        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        String name = Calendar.getInstance().getTime().toString();
        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "androidFlask "+name+".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        postRequest(postUrl, postBodyImage);
    }
    public void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed to connect to Server!!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            detectedPart = response.body().string();
                            infoTextView.setText("This is predicted to be "+ detectedPart);
                            infoTextView.setVisibility(View.VISIBLE);
                            moreInfoButton.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            detectedPart = null;
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void infoButtonListener(View view) {
        String url;
        Log.e("Main Activity ", detectedPart);

        if(detectedPart.equals("Engine Block")){
            url = getResources().getString(R.string.engineBlockLink);
        }
        else if (detectedPart.equals("Cylinder Head")){
            url = getResources().getString(R.string.cylinderHeadLink);
        }

        else if (detectedPart.equals("Timing Belt")){
            url = getResources().getString(R.string.timingBeltLink);
        }

        else if (detectedPart.equals("Crank Shaft")){
            url = getResources().getString(R.string.crankShaftLink);
        }

        else if (detectedPart.equals("Piston")){
            url = getResources().getString(R.string.pistonLink);
        }
        else{
            url = "---";
        }
        Intent i = new Intent(getApplicationContext(),Information.class);
        i.putExtra("url",url);
        reset();
        startActivity(i);
    }


    public void reset(){
        imView.setVisibility(View.GONE);
        predictButton.setVisibility(View.GONE);
        infoTextView.setVisibility(View.GONE);
        moreInfoButton.setVisibility(View.GONE);
    }

}