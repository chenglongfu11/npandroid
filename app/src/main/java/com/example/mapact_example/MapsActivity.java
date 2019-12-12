package com.example.mapact_example;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapact_example.models.MapWrapperLayout;
import com.example.mapact_example.models.Message;
import com.example.mapact_example.models.OnInfoWindowElemTouchListener;
import com.example.mapact_example.models.Recycle_adapter;
import com.example.mapact_example.util.ViewWeightAnimationWrapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener, GoogleMap.OnInfoWindowClickListener,
        TextView.OnEditorActionListener, Recycle_adapter.MessageListClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG ="MapsActivity";
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location mLastLocation;
    private ArrayList<Marker> markerList = new ArrayList<>();
    private ArrayList<LatLng> positionList = new ArrayList<>();

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private int mMapLayoutState = 1;

    private Context mContext;
    private LatLng myPlace;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker mCurrLocationMarker;
    private List<Message> messagesList = new ArrayList<>();

    private RelativeLayout mMapContainer;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private EditText mUsername;
    private EditText mShareMsg;
    private Button mResetBtn;

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private Button infoButton;
    private OnInfoWindowElemTouchListener infoButtonListener;

    public MapsActivity() {
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);     //connect to xml
        mContext = this;
        mMapContainer =(RelativeLayout)findViewById(R.id.ly_content) ;
        recyclerView = (RecyclerView) findViewById(R.id.recy_view);
        mUsername =(EditText)findViewById(R.id.txt_username_frag) ;
        mShareMsg = (EditText)findViewById(R.id.txt_input_frag);
        mShareMsg.setOnEditorActionListener(this);         //click return on keyboard to send message
        findViewById(R.id.edit_msg).setOnClickListener(this);           // click listener for post message  right top button
        findViewById(R.id.btn_x).setOnClickListener(this);               //map extand and compact button
        findViewById(R.id.btn_reset).setOnClickListener(this);

        //connect to mapFragment, another option is mapView
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //get location permission from device
        getLocationPermission();

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available.");
        }


        //set location requestion property
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds

//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    if (location != null) {
//                        wayLatitude = location.getLatitude();
//                        wayLongitude = location.getLongitude();
//
//
//                        mFusedLocationClient.removeLocationUpdates(locationCallback);
//                    }
//                }
//            }
//        };

        //get message list from server
        messagesList.add(new Message("Bob","what's the weather",
                new LatLng(59.348720,18.071006),new Date(System.currentTimeMillis())));

        messagesList.add(new Message("Alice","Today is busy",
                new LatLng(59.347724,18.072571),new Date(System.currentTimeMillis())));



//        String bundle = getIntent().getStringExtra("msginfo");
//        if(bundle !=null) {
//
//            try {
//                // get JSONObject from JSON file
//                JSONObject mJsonObject = new JSONObject(bundle);
//                // fetch JSONObject named employee
//                Message newmsg = (Message) mJsonObject.get("message");
//                //Message newMsg= (Message)newmsg;
//                messagesList.add(newmsg);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//
//
//            }
//        }




        //notification
        notifica();


        //recycle view
        recyclerView.setHasFixedSize(true);
        initUserListRecyclerView();


        // specify an adapter (see also next example)





    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        startUserLocationsRunnable();

    }

//    private void getLastKnownLocation() {
//        Log.d(TAG, "getLastKnownLocation: called.");
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//            @Override
//            public void onComplete(@NonNull Task<Location> task) {
//                if (task.isSuccessful()) {
//                    Location location = task.getResult();
//                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
//                    Log.d(TAG, "onComplete: latitude: " + geoPoint.getLatitude());
//                    Log.d(TAG, "onComplete: longitude: " + geoPoint.getLongitude());
//                }
//            }
//        });
//    }

    //check if google play service is available or not
    private boolean CheckGooglePlayServices() {
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(this);
            if(result != ConnectionResult.SUCCESS) {
                if(googleAPI.isUserResolvableError(result)) {
                    googleAPI.getErrorDialog(this, result,
                            0).show();
                }
                return false;
            }
            return true;
        }

        //get location permission, on onMapReady
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

//above are things for onCreate
// below is onMapReady


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.setOnInfoWindowClickListener(this);

       /* //Add a marker in New York
        LatLng myPlace = new LatLng(40.73, -73.99);  // this is New York
        mMap.addMarker(new MarkerOptions().position(myPlace).title("My Favorite City"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 12));    //0 代表世界地图， 20 最小， 12 正好

        // Add a marker in Sydney and move the camera
       /* LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */


        //infoWindow
        MapWrapperLayout mapWrapperLayout = (MapWrapperLayout)findViewById(R.id.map_relative_layout);
        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));

        // We want to reuse the info window for all the markers,
        // so let's create only one class member instance
        this.infoWindow = (ViewGroup)getLayoutInflater().inflate(R.layout.infowindow, null);
        this.infoTitle = (TextView)infoWindow.findViewById(R.id.title);
        this.infoSnippet = (TextView)infoWindow.findViewById(R.id.snippet);
        this.infoButton = (Button)infoWindow.findViewById(R.id.button);
        // Setting custom OnTouchListener which deals with the pressed state
        // so it shows up
        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton,
                getResources().getDrawable(R.drawable.love_filler), //btn_default_normal
                getResources().getDrawable(R.drawable.love_filler)) //btn_default_pressed
        {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
                Toast.makeText(getApplicationContext(),"You liked WeShare of "+marker.getTitle(), Toast.LENGTH_SHORT).show();
                //infoButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.love_filler));
                Message message=messagesList.get(markerList.indexOf(marker));
                if(message != null) {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(renewMarker(message.getUsername(), message.getMsg())));
                }
            }
        };
        this.infoButton.setOnTouchListener(infoButtonListener);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                infoTitle.setText(marker.getTitle());
                infoSnippet.setText(marker.getSnippet());
                infoButtonListener.setMarker(marker);

                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });





        mMap.getUiSettings().setZoomControlsEnabled(true);       //缩放控制并回调
        //mMap.setOnMarkerClickListener(this);

        /*// 单击
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });

         */

        getLocation();
        Log.d(TAG,"on MapReady, my location is got");
        showMessageLocations();




    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    private void showMessageLocations(){
        if(mMap != null) {
            for (Message message : messagesList) {

                MarkerOptions options = new MarkerOptions()
                        .position(message.getLocation())
                        .draggable(false)
                        .flat(false)
                        .title(message.getDate().toString())
                        .snippet(message.getMsg())
                        .icon(BitmapDescriptorFactory.fromBitmap(createMarker(message.getUsername(), message.getMsg())));
                Marker marker = mMap.addMarker(options);
                markerList.add(marker);
                Log.d(TAG, "Message List locations are set");
                positionList.add(message.getLocation());

            }
        }
    }


    private void initUserListRecyclerView() {
        mAdapter = new Recycle_adapter(messagesList,this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    //setting for google api
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                         .addConnectionCallbacks(this)
                         .addOnConnectionFailedListener(this)
                         .addApi(LocationServices.API)
                         .build();
        mGoogleApiClient.connect();
             }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();

    }


    @Override
    public void onLocationChanged(Location location){
        getLocation();

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
    }



//For each get location: get latitude, longitute, makeroptions, move camera
    private void getLocation() {
        Log.i(TAG,"get location is working");

            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                             if (location != null) {
                                 wayLatitude = location.getLatitude();
                                 wayLongitude = location.getLongitude();
                               //  GeoPoint geoPoint =new GeoPoint()
                                 myPlace = new LatLng(wayLatitude, wayLongitude);

                                 //Marker to show the map on the location
                                 MarkerOptions markerOptions = new MarkerOptions();
                                 markerOptions.position(myPlace);
                                 markerOptions.title("THIS IS Current Position");
                                 markerOptions.snippet("This is you");
                                 markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createMarker("Me", "This is you")));

                                 Marker marker = mMap.addMarker(markerOptions);
                                 messagesList.add(new Message("Me","Welcome to WeShare",myPlace,new Date(System.currentTimeMillis())));
                                 //marker.showInfoWindow();
                                 positionList.add(myPlace);
                                 markerList.add(marker);

                                 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 16));
                             } else {
                                 mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                             }
                         });
    }


    @Override
   public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

             }




    //button listener for onClick

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_msg:
                Intent it = new Intent(getApplicationContext(), PostMessage.class);
                startActivity(it);
            case R.id.btn_x: {
                if (mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    expandMapAnimation();
                } else if (mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                    contractMapAnimation();
                }
            }
            case R.id.btn_reset:{
                    resetMap();
                    showMessageLocations();
            }
        }
    }


    //notification function
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifica() {
        String CHANNEL_ID = "my_channel_01";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.triangle)
                .setContentTitle("WeShare")
                .setContentText("WeShare is running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(1, builder.build());

    }

        //below are functions to expand and contract map
    private void expandMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                50,
                100);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(recyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                50,
                0);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void contractMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                50);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(recyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                50);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }


    //click info window of marker to show alertdialog
    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getSnippet().equals("This is you")) {
            marker.hideInfoWindow();
        } else {

            final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage(marker.getSnippet())
                    .setCancelable(true)
                    .setPositiveButton("LIKE", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    //click return button on keyboard to send message
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            String mtxtUsername = mUsername.getText().toString();
            String mtxtShareMsg = mShareMsg.getText().toString();
            if(mtxtShareMsg.length() ==0){
                Toast.makeText(this,"Please write message to share",Toast.LENGTH_SHORT).show();
                Log.d(TAG,"onEditorAction: wrong input for share message");
            }else if(mtxtUsername.length() ==0){
                Toast.makeText(this,"Please specify a username",Toast.LENGTH_SHORT).show();
                Log.d(TAG,"onEditorAction: wrong input for username");
            }

            else {
                Message newMsg = new Message(mtxtUsername,mtxtShareMsg,myPlace,new Date(System.currentTimeMillis()));
                messagesList.add(newMsg);
                showMessageLocations();
                mUsername.setText("");
                mShareMsg.setText("");
                Toast.makeText(this, "Input message is " +newMsg.toString(), Toast.LENGTH_SHORT).show();
                //transfer message to database.
            }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }

        return false;
    }




    //customized marker
    private Bitmap createMarker(String username, String msg) {
        View markerLayout = getLayoutInflater().inflate(R.layout.marketlayout, null);

        //ImageView markerImage = (ImageView) markerLayout.findViewById(R.id.marker_image);
        TextView markerTxtUser = (TextView) markerLayout.findViewById(R.id.marker_text_user);
        TextView markerTxtMsg = (TextView) markerLayout.findViewById(R.id.marker_text_msg);




        //markerImage.setImageResource(R.drawable.ic_home_marker);
        markerTxtUser.setText(username);
        markerTxtMsg.setText(msg);

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }

    //renew marker
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private Bitmap renewMarker(String username, String msg) {
        View markerLayout = getLayoutInflater().inflate(R.layout.marketlayout, null);

        //ImageView markerImage = (ImageView) markerLayout.findViewById(R.id.marker_image);
        TextView markerTxtUser = (TextView) markerLayout.findViewById(R.id.marker_text_user);
        TextView markerTxtMsg = (TextView) markerLayout.findViewById(R.id.marker_text_msg);
        markerLayout.setBackground(ContextCompat.getDrawable(this,R.drawable.redbubble));
        Log.d(TAG,"renewMaker to " );


        //markerImage.setImageResource(R.drawable.ic_home_marker);
        markerTxtUser.setText(username);
        markerTxtMsg.setText(msg);

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }


    private void resetMap(){
        if(mMap != null) {
            mMap.clear();

            if (markerList.size() > 0) {
                markerList.clear();
                markerList = new ArrayList<>();
            }

        }
    }

    @Override
    public void onClicked(int position) {
        Log.d(TAG,"onCliced:  + message is "+messagesList.get(position));
        Marker makkk = markerList.get(position);
        if(makkk != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(makkk.getPosition().latitude,
                    makkk.getPosition().longitude)), 600, null);
        }

    }


//    //update messges and locations every 3 seconds
//    private Handler mHandler = new Handler();
//    private Runnable mRunnable;
//    private static final int LOCATION_UPDATE_INTERVAL = 3000;
//
//    private void startUserLocationsRunnable(){
//        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
//        mHandler.postDelayed(mRunnable = new Runnable() {
//            @Override
//            public void run() {
//                retriveMessageLocations();
//                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
//            }
//        }, LOCATION_UPDATE_INTERVAL);
//    }
//
//    private void stopLocationUpdates(){
//        mHandler.removeCallbacks(mRunnable);
//    }
//
//    private void retriveMessageLocations(){
//
//        for(Message mssg:messagesList) {
//            if (markerList.size() != 0) {
//                for (int i=0;i<markerList.size();i++) {
//                    Marker makk=markerList.get(i);
//                    //replace duplicated message
//                    if (makk.getPosition().equals(mssg.getLocation()) && makk.getTitle() != mssg.getDate().toString()) {
//                        makk.remove();
//                        markerList.remove(i);
//                        MarkerOptions options = new MarkerOptions()
//                                .position(mssg.getLocation())
//                                .draggable(false)
//                                .flat(false)
//                                .title(mssg.getDate().toString())
//                                .snippet(mssg.getMsg())
//                                .icon(BitmapDescriptorFactory.fromBitmap(createMarker(mssg.getUsername(), mssg.getMsg())));
//                        Marker marker = mMap.addMarker(options);
//                        markerList.add(marker);
//                        mListView.append(mssg.toString());
//                        positionList.remove(i);
//                        positionList.add(mssg.getLocation());
//
//                    }
//
//
////                    MarkerOptions options = new MarkerOptions()
////                            .position(message.getLocation())
////                            .draggable(true)
////                            .flat(false)
////                            .title(message.getDate().toString())
////                            .snippet(message.getMsg())
////                            .icon(BitmapDescriptorFactory.fromBitmap(createMarker(message.getUsername(), message.getMsg())));
////                    Marker marker = mMap.addMarker(options);
////                    markerList.add(marker);
////                    Log.d(TAG, "Message List locations are set");
////                    mListView.append(message.toString());
//                }
//            }
//        }
//
//    }




}




