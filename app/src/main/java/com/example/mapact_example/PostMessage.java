package com.example.mapact_example;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mapact_example.models.Message;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class PostMessage extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener{

    private static final String TAG = "PostMessage";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    //widgets
    private ProgressBar mProgressBar;

    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private EditText mUsername;
    private EditText mInput;
    private Button btn_share;
    private LatLng myPlace = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postmessage);
        //mProgressBar = findViewById(R.id.progressBar);
        getLocationPermission();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();
        initView();
    }

    private void initView() {


        findViewById(R.id.btn_share).setOnClickListener(this);
        mUsername = findViewById(R.id.txt_username);
        mInput = findViewById(R.id.txt_msg);
        Log.i(TAG, "intiView finished");


    }


    @Override
    public void onClick(View v) {
        String JSON_STRING = "{\"message\":{\"username\":\"\",\"salary\":65000}}";


        switch (v.getId()) {

            case R.id.btn_share:

                Intent it = new Intent().setClass(getApplicationContext(),MapsActivity.class);
                String musername = mUsername.getText().toString();
                String minput = mInput.getText().toString();

                Date date = new Date(System.currentTimeMillis());



                Message newMsg = new Message(musername, minput, myPlace, date);

                JSONObject obj=new JSONObject();
                try {
                    obj.put("message",newMsg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, newMsg.toString());
                //port to websocket
                Log.i(TAG,"message is succefully saved with "+newMsg.getUsername()+
                        " msg is "+newMsg.getMsg()+" location is "
                        +newMsg.getLocation()+" at time "+newMsg.getDate());


                it.putExtra("msginfo",obj.toString());
                startActivity(it);
                break;
        }
    }




    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    //get location permission
    private void getLocationPermission(){

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    //For each get location: get latitude, longitute, makeroptions, move camera
    private void getLocation() {
        //LatLng postPostion;
        Log.i(TAG, "get location is working");

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    double wayLatitude = location.getLatitude();
                    double wayLongitude = location.getLongitude();
                    //  GeoPoint geoPoint =new GeoPoint()
                    myPlace = new LatLng(wayLatitude, wayLongitude);
                    Log.d(TAG, "onComplete: latitude: " + myPlace.latitude);
                    Log.d(TAG, "onComplete: longitude: " + myPlace.longitude);
                }
            }
        });
    }



    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }
}
