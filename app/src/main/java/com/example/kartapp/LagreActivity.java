package com.example.kartapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class LagreActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MyDialog.DialogClickListener, LocationListener, OnMapReadyCallback {

    public static final String TAG = LagreActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView textView;

    String lat;
    String lng;
    String gateaddresse;
    EditText beskrivelseInp;
    TextView responsTekst;
    LatLng tmpPoint;
    EditText gateAddresseTxtEdit;
    Button leggtilBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lagre);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        lat = getIntent().getStringExtra("lat");
        lng = getIntent().getStringExtra("lng");
        gateaddresse = getIntent().getStringExtra("gateadresse");
        gateAddresseTxtEdit = (EditText) findViewById(R.id.gateAddresse);
        beskrivelseInp = (EditText) findViewById(R.id.beskrivelseInp);
        responsTekst = (TextView) findViewById(R.id.responsTekst);
        leggtilBtn = (Button) findViewById(R.id.lagreBtn);

        leggtilBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String beskrivelse = String.valueOf(beskrivelseInp.getText());
                createJSON task = new createJSON(lat,lng,gateaddresse,beskrivelse);
                task.execute();

                responsTekst.setText("Severdigheten er lagret");
            }
        });
        gateAddresseTxtEdit.setText(gateaddresse);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this )
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());


    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent backIntent= new Intent(LagreActivity.this, AddresseListeActivity.class);
                backIntent.putExtra("lokasjon",lat + ":" + lng);
                startActivity(backIntent);
                return true;


        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onYesClick() {
        MarkerOptions marker = new MarkerOptions().position(new LatLng(tmpPoint.latitude, tmpPoint.longitude)).title("Beskrivelse:ffoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqoqfq");
        mMap.addMarker(marker);
}
    @Override
    public void onNoClick() {return; }




    private class createJSON extends AsyncTask<String, Void,String> {
        JSONObject jsonObject;
        String lat;
        String lng;
        String gateaddresse;
        String beskrivelse;

        public createJSON(String lat, String lng, String gateaddresse,String beskrivelse) {

            this.lat = lat;
            this.lng = lng;
            this.gateaddresse = gateaddresse;
            this.beskrivelse = beskrivelse;
        }


        @Override
        protected String doInBackground(String... urls) {
            String retur = "";
            String s = "";
            String output = "";

                try{
                    URL urlen= new URL("http://data1500.cs.oslomet.no/~s349967/jsonin.php?lat="+lat+"&lng="+lng+"&gateadresse="+gateaddresse+"&beskrivelse="+beskrivelse);
                    HttpURLConnection conn= (HttpURLConnection)
                            urlen.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                    if (conn.getResponseCode() != 200) {

                        throw new RuntimeException("Failed : HTTP errorcode: "
                                + conn.getResponseCode());
                    }


                    BufferedReader br= new BufferedReader(new InputStreamReader((conn.getInputStream())));

                    while ((s = br.readLine()) != null) {
                        output = output + s;
                    }
                    conn.disconnect();
                    try{
                        JSONArray mat = new JSONArray(output);


                        return mat.toString();
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                    return retur;
                } catch(Exception e) {
                    return e.getLocalizedMessage();
                }


        }
        @Override
        protected void onPostExecute(String ss) {

        }
    }


    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            handleNewLocation(mLastLocation);
        }
    };



    public void handleNewLocation(Location location) {

        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location == null) {
                    requestNewLocationData();
                } else {
                    handleNewLocation(location);
                }
            }
        });


    }
    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        handleNewLocation(location);



    }




    @Override
    public void onMapReady(GoogleMap googleMap) {


    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    }
