package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Measurement extends AppCompatActivity implements Scene.OnUpdateListener{

    private double MIN_OPENGL_VERSION = 3.0;
    private String TAG = Measurement.class.getSimpleName();

    private ArFragment arFragment = null;

    private TextView pointTextView;
    private TextView distanceModeTextView;

    private LinearLayout arrow1UpLinearLayout;
    private LinearLayout arrow1DownLinearLayout;
    private ImageView arrow1UpView;
    private ImageView arrow1DownView;
    private Renderable arrow1UpRenderable;
    private Renderable arrow1DownRenderable;


    private LinearLayout arrow10UpLinearLayout;
    private LinearLayout arrow10DownLinearLayout;
    private ImageView arrow10UpView;
    private ImageView arrow10DownView;
    private Renderable arrow10UpRenderable;
    private Renderable arrow10DownRenderable;


    private TableLayout multipleDistanceTableLayout;

    private ModelRenderable cubeRenderable;
    private ViewRenderable distanceCardViewRenderable;

    private Spinner distanceModeSpinner;
    private String distanceMode = "";
    private List<String> distanceModeArrayList = new ArrayList<String>();

    private ArrayList<Anchor> placedAnchors = new ArrayList<Anchor>();
    private ArrayList<AnchorNode> placedAnchorNodes = new ArrayList<AnchorNode>();
    private HashMap<String,Anchor> midAnchors = new HashMap<String,Anchor>();
    private HashMap<String, AnchorNode> midAnchorNodes = new HashMap<String,AnchorNode>();
    private ArrayList<List<Node>> fromGroundNodes = new ArrayList<List<Node>>();

    private String initCM;
    private Button clearButton;
    private Button confirmButton;

    Constants c = new Constants();
    int mmpp = c.getMaxMultiplePoints();

    private TextView [][] multipleDistances = new TextView[mmpp][mmpp];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Called");
        if(!checkIsSupportedDeviceOrFinish(this)){
            Toast.makeText(this, "Device not supported", Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "onCreate: check finished");
        setContentView(R.layout.activity_measurement);

        String [] distanceModeArray = getResources().getStringArray(R.array.distance_mode);
        distanceModeArrayList = Arrays.asList(distanceModeArray);

        arFragment =(ArFragment)getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        distanceModeTextView = findViewById(R.id.distance_view);
        multipleDistanceTableLayout = findViewById(R.id.multiple_distance_table);

        initCM = getResources().getString(R.string.initCM);
        Log.d(TAG, "onCreate: Initialize done");
        configureSpinner();
        initArrowView();
        initRenderable();
        clearButtonSetup();
        confirmButtonSetup();
        Log.d(TAG, "onCreate: functions called");
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) ->{
            Log.d(TAG, "onCreate: tapped on screen");
            if(cubeRenderable==null || distanceCardViewRenderable ==null){
                Log.d(TAG, "onCreate: returning early");
                return ;
            }
            Log.d(TAG, "onCreate: "+ distanceMode);
            if(distanceMode.equals(distanceModeArrayList.get(0))){
                clearAllAnchors();
                placeAnchor(hitResult,distanceCardViewRenderable);
            }
            else if(distanceMode.equals(distanceModeArrayList.get(1))){
                tapDistanceOf2Points(hitResult);
            }
            else if(distanceMode.equals(distanceModeArrayList.get(2))){
                tapDistanceOfMultiplePoints(hitResult);
            }
            else if(distanceMode.equals(distanceModeArrayList.get(3))){
                tapDistanceFromGround(hitResult);
            }
            else{
                clearAllAnchors();
                placeAnchor(hitResult,distanceCardViewRenderable);
            }

        } );

        Log.d(TAG, "onCreate: arfragment setup");
    
    }

    private void confirmButtonSetup() {
        confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Results.class);
                ArrayList<Float> coordinates3 = new ArrayList<Float>();
                for(int i=0;i<placedAnchorNodes.size();i++){
                    coordinates3.add(placedAnchorNodes.get(i).getWorldPosition().x);
                    coordinates3.add(placedAnchorNodes.get(i).getWorldPosition().y);
                    coordinates3.add(placedAnchorNodes.get(i).getWorldPosition().z);
                }
                intent.putExtra("coordinates",coordinates3);
                startActivity(intent);
            }
        });
    }

    private void tapDistanceFromGround(HitResult hitResult) {

        Log.d(TAG, "tapDistanceFromGround: in this method");
        clearAllAnchors();
        Anchor anchor = hitResult.createAnchor();
        placedAnchors.add(anchor);
        Log.d(TAG, "tapDistanceFromGround: anchor placed");
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setSmoothed(true);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        placedAnchorNodes.add(anchorNode);
        Log.d(TAG, "tapDistanceFromGround: anchor node placed");
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.getRotationController().setEnabled(false);
        transformableNode.getScaleController().setEnabled(false);
        transformableNode.getTranslationController().setEnabled(false);
        //possible
        transformableNode.setParent(anchorNode);

        Node node = new Node();
        node.setParent(transformableNode);
        node.setWorldPosition(new Vector3(anchorNode.getWorldPosition().x,
                anchorNode.getWorldPosition().y,
                anchorNode.getWorldPosition().z));
        node.setRenderable(distanceCardViewRenderable);

        Node arrow1UpNode = new Node();
        arrow1UpNode.setParent(node);
        arrow1UpNode.setWorldPosition(new Vector3(node.getWorldPosition().x,
                node.getWorldPosition().y+0.1f,
                node.getWorldPosition().z));
        arrow1UpNode.setRenderable(arrow1UpRenderable);
        arrow1UpNode.setOnTapListener((hitTestResult, motionEvent) -> {
            node.setWorldPosition(new Vector3(node.getWorldPosition().x,
                    node.getWorldPosition().y + 0.01f,
                    node.getWorldPosition().z));
        });

        Node arrow1DownNode = new Node();
        arrow1DownNode.setParent(node);
        arrow1DownNode.setWorldPosition(new Vector3(node.getWorldPosition().x,
                node.getWorldPosition().y-0.08f,
                node.getWorldPosition().z));
        arrow1DownNode.setRenderable(arrow1DownRenderable);
        arrow1DownNode.setOnTapListener((hitTestResult, motionEvent) -> {
            node.setWorldPosition(new Vector3(node.getWorldPosition().x,
                    node.getWorldPosition().y - 0.01f,
                    node.getWorldPosition().z));
        });

        Node arrow10UpNode = new Node();
        arrow10UpNode.setParent(node);
        arrow10UpNode.setWorldPosition(new Vector3(node.getWorldPosition().x,
                node.getWorldPosition().y+0.18f,
                node.getWorldPosition().z));
        arrow10UpNode.setRenderable(arrow10UpRenderable);
        arrow10UpNode.setOnTapListener((hitTestResult, motionEvent) -> {
            node.setWorldPosition(new Vector3(node.getWorldPosition().x,
                    node.getWorldPosition().y + 0.1f,
                    node.getWorldPosition().z));
        });

        Node arrow10DownNode = new Node();
        arrow10DownNode.setParent(node);
        arrow10DownNode.setWorldPosition(new Vector3(node.getWorldPosition().x,
                node.getWorldPosition().y-0.167f,
                node.getWorldPosition().z));
        arrow10DownNode.setRenderable(arrow10DownRenderable);
        arrow10DownNode.setOnTapListener((hitTestResult, motionEvent) -> {
            node.setWorldPosition(new Vector3(node.getWorldPosition().x,
                    node.getWorldPosition().y - 0.01f,
                    node.getWorldPosition().z));
        });

        List<Node> adding = Arrays.asList(node,arrow1UpNode,arrow1DownNode,arrow10UpNode,arrow10DownNode);

        fromGroundNodes.add(adding);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();

    }

    private void tapDistanceOfMultiplePoints(HitResult hitResult) {
        if(placedAnchorNodes.size()==mmpp-1){
            clearAllAnchors();
        }
        ViewRenderable.builder()
                .setView(this,R.layout.point_text_layout)
                .build()
                .thenAccept(viewRenderable -> {
                    viewRenderable.setShadowReceiver(false);
                    viewRenderable.setShadowCaster(false);
                    pointTextView = (TextView) viewRenderable.getView();
                    pointTextView.setText(Integer.toString(placedAnchors.size()));
                    placeAnchor(hitResult,viewRenderable);
                })
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage()).setTitle("Error");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return null;
                });
        Log.i(TAG,"Number of anchors: "+ placedAnchors.size());
    }

    private void tapDistanceOf2Points(HitResult hitResult) {
        if(placedAnchorNodes.size()==0) {

            placeAnchor(hitResult,cubeRenderable);

        }
        else if(placedAnchorNodes.size()==1){
            placeAnchor(hitResult,cubeRenderable);

            float [] midPosition = {(placedAnchorNodes.get(0).getWorldPosition().x +placedAnchorNodes.get(1).getWorldPosition().x)/2,
                    (placedAnchorNodes.get(0).getWorldPosition().y +placedAnchorNodes.get(1).getWorldPosition().y)/2,
                    (placedAnchorNodes.get(0).getWorldPosition().z +placedAnchorNodes.get(1).getWorldPosition().z)/2};

            float [] quaternion = {0.0f,0.0f,0.0f,0.0f};

            Pose pose = new Pose(midPosition,quaternion);

            placeMidAnchor(pose,distanceCardViewRenderable);

        }
        else{
            clearAllAnchors();;
            placeAnchor(hitResult,cubeRenderable);
        }
    }

    private void placeMidAnchor(Pose pose, Renderable renderable ) {
        int [] between = {0,1};

        String midKey = Integer.toString(between[0]) + "_"+ Integer.toString(between[1]);

        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pose);
        midAnchors.put(midKey,anchor);

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setSmoothed(true);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        midAnchorNodes.put(midKey,anchorNode);

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.getRotationController().setEnabled(false);
        node.getScaleController().setEnabled(false);
        node.getTranslationController().setEnabled(true);
        node.setRenderable(renderable);
        node.setParent(anchorNode);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        arFragment.getArSceneView().getScene().addChild(anchorNode);


    }

    private void placeMidAnchor(Pose pose, Renderable renderable,int [] between ) {
        String midKey = Integer.toString(between[0]) + "_"+ Integer.toString(between[1]);

        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pose);
        midAnchors.put(midKey,anchor);

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setSmoothed(true);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        midAnchorNodes.put(midKey,anchorNode);

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.getRotationController().setEnabled(false);
        node.getScaleController().setEnabled(false);
        node.getTranslationController().setEnabled(true);
        node.setRenderable(renderable);
        node.setParent(anchorNode);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
    }
    private void placeAnchor(HitResult hitResult, Renderable distanceCardViewRenderable) {


        Anchor anchor = hitResult.createAnchor();
        placedAnchors.add(anchor);

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setSmoothed(true);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        placedAnchorNodes.add(anchorNode);

        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.getRotationController().setEnabled(false);
        transformableNode.getScaleController().setEnabled(false);
        transformableNode.getTranslationController().setEnabled(true);
        transformableNode.setRenderable(distanceCardViewRenderable);
        transformableNode.setParent(anchorNode);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();


    }

    private void clearButtonSetup() {
        Log.d(TAG, "clearButtonSetup: Setting up clear button");
        clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllAnchors();
            }
        });
        Log.d(TAG, "clearButtonSetup: Setup done");
    }


    private void initRenderable() {

        Log.d(TAG, "initRenderable: Initializing renderable");
        
        MaterialFactory.makeTransparentWithColor(getApplicationContext(),
                new Color(android.graphics.Color.RED))
                .thenAccept(material -> {
                    cubeRenderable = ShapeFactory.makeSphere(
                            0.02f,
                            Vector3.zero(),
                            material
                    );
                    cubeRenderable.setShadowReceiver(false);
                    cubeRenderable.setShadowCaster(false);

                }).exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage()).setTitle("Error");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return null;
        });

        ViewRenderable
                .builder()
                .setView(this,R.layout.distance_text_layout)
                .build()
                .thenAccept(viewRenderable -> {
                    distanceCardViewRenderable = viewRenderable;
                    distanceCardViewRenderable.setShadowCaster(false);
                    distanceCardViewRenderable.setShadowReceiver(false);
                }).exceptionally(throwable -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(throwable.getMessage()).setTitle("Error");
            AlertDialog dialog = builder.create();
            dialog.show();
            return null;
        });

        ViewRenderable
                .builder()
                .setView(this,R.layout.distance_text_layout)
                .build()
                .thenAccept(viewRenderable -> {
                    arrow1DownRenderable = viewRenderable;
                    arrow1DownRenderable.setShadowCaster(false);
                    arrow1DownRenderable.setShadowReceiver(false);
                }).exceptionally(throwable -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(throwable.getMessage()).setTitle("Error");
            AlertDialog dialog = builder.create();
            dialog.show();
            return null;
        });


        ViewRenderable
                .builder()
                .setView(this,R.layout.distance_text_layout)
                .build()
                .thenAccept(viewRenderable -> {
                    arrow1UpRenderable = viewRenderable;
                    arrow1UpRenderable.setShadowCaster(false);
                    arrow1UpRenderable.setShadowReceiver(false);
                }).exceptionally(throwable -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(throwable.getMessage()).setTitle("Error");
            AlertDialog dialog = builder.create();
            dialog.show();
            return null;
        });

        ViewRenderable
                .builder()
                .setView(this,R.layout.distance_text_layout)
                .build()
                .thenAccept(viewRenderable -> {
                    arrow10UpRenderable = viewRenderable;
                    arrow10UpRenderable.setShadowCaster(false);
                    arrow10UpRenderable.setShadowReceiver(false);
                }).exceptionally(throwable -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(throwable.getMessage()).setTitle("Error");
            AlertDialog dialog = builder.create();
            dialog.show();
            return null;
        });

        ViewRenderable
                .builder()
                .setView(this,R.layout.distance_text_layout)
                .build()
                .thenAccept(viewRenderable -> {
                    arrow10DownRenderable = viewRenderable;
                    arrow10DownRenderable.setShadowCaster(false);
                    arrow10DownRenderable.setShadowReceiver(false);
                }).exceptionally(throwable -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(throwable.getMessage()).setTitle("Error");
            AlertDialog dialog = builder.create();
            dialog.show();
            return null;
        });

        Log.d(TAG, "initRenderable: Initialization done");
    }


    private void initArrowView() {

        Log.d(TAG, "initArrowView: configuring arrow view");
        Constants c = new Constants();

        arrow1UpLinearLayout = new LinearLayout(this);
        arrow1UpLinearLayout.setOrientation(LinearLayout.VERTICAL);
        arrow1UpLinearLayout.setGravity(Gravity.CENTER);
        arrow1UpView = new ImageView(this);
        arrow1UpView.setImageResource(R.drawable.arrow_1up);
        arrow1UpLinearLayout.addView(arrow1UpView,c.getArrowViewSize(),c.getArrowViewSize());

        arrow10UpLinearLayout = new LinearLayout(this);
        arrow10UpLinearLayout.setOrientation(LinearLayout.VERTICAL);
        arrow10UpLinearLayout.setGravity(Gravity.CENTER);
        arrow10UpView = new ImageView(this);
        arrow10UpView.setImageResource(R.drawable.arrow_10up);
        arrow10UpLinearLayout.addView(arrow10UpView,c.getArrowViewSize(),c.getArrowViewSize());

        arrow1DownLinearLayout = new LinearLayout(this);
        arrow1DownLinearLayout.setOrientation(LinearLayout.VERTICAL);
        arrow1DownLinearLayout.setGravity(Gravity.CENTER);
        arrow1DownView = new ImageView(this);
        arrow1DownView.setImageResource(R.drawable.arrow_1down);
        arrow1DownLinearLayout.addView(arrow1DownView,c.getArrowViewSize(),c.getArrowViewSize());

        arrow10DownLinearLayout = new LinearLayout(this);
        arrow10DownLinearLayout.setOrientation(LinearLayout.VERTICAL);
        arrow10DownLinearLayout.setGravity(Gravity.CENTER);
        arrow10DownView = new ImageView(this);
        arrow10DownView.setImageResource(R.drawable.arrow_10down);
        arrow10DownLinearLayout.addView(arrow10DownView,c.getArrowViewSize(),c.getArrowViewSize());

        Log.d(TAG, "initArrowView: configured arrow view");

    }

    private void configureSpinner() {

        Log.d(TAG, "configureSpinner: configuring spinner");
        distanceMode = distanceModeArrayList.get(0);
        distanceModeSpinner = findViewById(R.id.distance_mode_spinner);

        ArrayAdapter distanceModeAdapter = new ArrayAdapter(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item,distanceModeArrayList);

        distanceModeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        distanceModeSpinner.setAdapter(distanceModeAdapter);

        distanceModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinnerParent = (Spinner)parent;
                distanceMode = (String)spinnerParent.getSelectedItem();
                clearAllAnchors();
                setMode();
                toastMode();

                Constants c = new Constants();

                ViewGroup.LayoutParams layoutParams = multipleDistanceTableLayout.getLayoutParams();
                if(distanceMode==distanceModeArrayList.get(2)){
                    layoutParams.height = c.getMultipleDistanceTableHeight();
                    multipleDistanceTableLayout.setLayoutParams(layoutParams);

                    initDistanceTable();
                }
                else{
                    layoutParams.height = 0;
                    multipleDistanceTableLayout.setLayoutParams(layoutParams);
                }
                Log.i(TAG,"Distance mode: "+ distanceMode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                clearAllAnchors();
                setMode();
                toastMode();
            }


        });
        Log.d(TAG, "configureSpinner: configure spinner done");
    }

    private void initDistanceTable() {
        Constants c = new Constants();
        int mmp = c.getMaxMultiplePoints();

        for(int i=0;i<mmp;i++){
            TableRow tableRow = new TableRow(this);
            multipleDistanceTableLayout.addView(tableRow,
                    multipleDistanceTableLayout.getWidth(),
                    c.getMultipleDistanceTableHeight()/(mmp+1));


            for(int j = 0;j<mmp;j++){
                TextView textView = new TextView(this);
                textView.setTextColor(android.graphics.Color.WHITE);
                if(i==0){
                    if(j==0){
                        textView.setText("cm");
                    }
                    else{
                        textView.setText(Integer.toString(j - 1));
                    }
                }
                else{
                    if(j==0){
                        textView.setText(Integer.toString(i-1));
                    }
                    else if(i==j){
                        textView.setText("-");
                        multipleDistances[i-1][j-1] = textView;
                    }
                    else{
                        textView.setText(initCM);
                        multipleDistances[i-1][j-1] = textView;
                    }
                }

                tableRow.addView(textView,
                        tableRow.getLayoutParams().width/(mmp+1),
                        tableRow.getLayoutParams().height);

            }

        }
    }

    private void toastMode() {
        if(distanceMode.equals(distanceModeArrayList.get(0))){
            Toast.makeText(this, "Find plane and tap somewhere", Toast.LENGTH_SHORT).show();
        }
        else if(distanceMode.equals(distanceModeArrayList.get(1))){
            Toast.makeText(this, "Find plane and tap two points", Toast.LENGTH_SHORT).show();
        }
        else if(distanceMode.equals(distanceModeArrayList.get(2))){
            Toast.makeText(this, "Find plane and tap multiple points", Toast.LENGTH_SHORT).show();
        }
        else if(distanceMode.equals(distanceModeArrayList.get(3))){
            Toast.makeText(this, "Find plane and tap a point", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "???", Toast.LENGTH_SHORT).show();
        }
    }

    private void setMode() {
        distanceModeTextView.setText(distanceMode);
    }

    private void clearAllAnchors() {
        placedAnchors.clear();
        for(AnchorNode anchorNode : placedAnchorNodes){

            arFragment.getArSceneView().getScene().removeChild(anchorNode);
            anchorNode.setEnabled(false);
            anchorNode.getAnchor().detach();
            anchorNode.setParent(null);

        }

        placedAnchorNodes.clear();
        midAnchors.clear();

        for(Map.Entry<String,AnchorNode> entry:midAnchorNodes.entrySet()){
            arFragment.getArSceneView().getScene().removeChild(entry.getValue());
            entry.getValue().setEnabled(false);
            entry.getValue().getAnchor().detach();
            entry.getValue().setParent(null);
        }

        midAnchorNodes.clear();

        for(int i=0;i<mmpp;i++){
            for(int j=0;j<mmpp;j++){
                if(multipleDistances[i][j]!=null){
                    if(i==j){
                        multipleDistances[i][j].setText("-");
                    }
                    else{
                        multipleDistances[i][j].setText(initCM);
                    }
                }
            }
        }

        fromGroundNodes.clear();
    }


    private boolean checkIsSupportedDeviceOrFinish(Activity activity) {
        String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)))
                .getDeviceConfigurationInfo()
                .getGlEsVersion();
        Log.d(TAG, "checkIsSupportedDeviceOrFinish: "+ openGlVersion);
        if(Double.parseDouble(openGlVersion) <MIN_OPENGL_VERSION){
            Log.e(TAG,"Sceneform requires OpenGl version: "+MIN_OPENGL_VERSION+" or later");
            Toast.makeText(activity, "Sceneform requires OpenGl version: "+MIN_OPENGL_VERSION+" or later", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
        Log.d(TAG, "checkIsSupportedDeviceOrFinish: check performed");
        return true;
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        Log.d(TAG, "onUpdate: "+ frameTime.toString());
        if(distanceMode.equals(distanceModeArrayList.get(0))){
            measureDistanceFromCamera();
        }
        else if(distanceMode.equals(distanceModeArrayList.get(1))){
            measureDistanceOf2Points();
        }
        else if(distanceMode.equals(distanceModeArrayList.get(2))){
            measureMultipleDistances();
        }
        else if(distanceMode.equals(distanceModeArrayList.get(3))){
            measureDistanceFromGround();
        }
        else{
            measureDistanceFromCamera();
        }
    }

    private void measureDistanceFromGround() {
        if(fromGroundNodes.size()==0)return ;
        for(List<Node> node : fromGroundNodes){
            TextView tv = (TextView) distanceCardViewRenderable.getView()
                    .findViewById(R.id.distanceCard);
            float distanceCM = changeUnit(node.get(0).getWorldPosition().y + 1.0f,"cm");
            String text = distanceCM + " cm";
            tv.setText(text);

        }
    }

    private float changeUnit(float distanceMeter,String unit){
        if(unit.equals("cm")){
            return distanceMeter*100;
        }
        else if(unit.equals("mm")){
            return distanceMeter*1000;
        }
        else{
            return distanceMeter;
        }
    }

    private void measureMultipleDistances() {
        if(placedAnchorNodes.size()>1){
            for(int i =0;i<placedAnchorNodes.size();i++){
                for(int j=0;j<placedAnchorNodes.size();j++){
                    float distMeter = calculateDistance(
                            placedAnchorNodes.get(i).getWorldPosition(),
                            placedAnchorNodes.get(j).getWorldPosition()
                    );
                    float distanceCM = changeUnit(distMeter,"cm");
                    String s = String.format("%.2f", distanceCM) + " cm";
                    multipleDistances[i][j].setText(s);
                    multipleDistances[j][i].setText(s);
                }
            }
        }
    }

    private void measureDistanceOf2Points() {
        if(placedAnchorNodes.size()==2){
            float distanceMeter = calculateDistance(
                    placedAnchorNodes.get(0).getWorldPosition(),
                    placedAnchorNodes.get(1).getWorldPosition()
            );
            measureDistanceOf2Points(distanceMeter);
        }
    }

    private void measureDistanceOf2Points(float distanceMeter) {
        String distTextCm = makeDistanceTextWithCM(distanceMeter);
        TextView tv = distanceCardViewRenderable.getView().findViewById(R.id.distanceCard);
        tv.setText(distTextCm);
    }

    private String makeDistanceTextWithCM(float distanceMeter){
        float distanceCm = changeUnit(distanceMeter,"cm");
        String s = String.format("%.2f", distanceCm);
        return s + " cm";
    }

    private void measureDistanceFromCamera() {
        Frame arframe = arFragment.getArSceneView().getArFrame();
        Log.d(TAG, "measureDistanceFromCamera: placedAnchorNodes.size() = "+ placedAnchorNodes.size());
        if(placedAnchorNodes.size()>=1){
            float distanceMeter = calculateDistance(
                    placedAnchorNodes.get(0).getWorldPosition(),
                    arframe.getCamera().getPose()
            )   ;
            measureDistanceOf2Points(distanceMeter);
        }
    }

    private float calculateDistance(float x,float y,float z){
        return (float)Math.sqrt(x*x+y*y+z*z);
    }

    private float calculateDistance(Pose objectPose0,Pose objectPose1){
        return calculateDistance(
                objectPose0.tx() - objectPose1.tx(),
                objectPose0.ty() - objectPose1.ty(),
                objectPose0.tz() - objectPose1.tz()
        );
    }

    private float calculateDistance(Vector3 objectPose0,Vector3 objectPose1){
        return calculateDistance(
                objectPose0.x - objectPose1.x,
                objectPose0.y - objectPose1.y,
                objectPose0.z - objectPose1.z
        );
    }

    private float calculateDistance(Vector3 objectPose0,Pose objectPose1){
        return calculateDistance(
                objectPose0.x - objectPose1.tx(),
                objectPose0.y - objectPose1.ty(),
                objectPose0.z - objectPose1.tz()
        );
    }
}