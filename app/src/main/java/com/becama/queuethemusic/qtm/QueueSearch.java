package com.becama.queuethemusic.qtm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.becama.queuethemusic.qtm.CreateAQueueHomePage.PREFS_NAME;
import static com.becama.queuethemusic.qtm.CreateAQueueOptions.MY_PERMISSIONS_REQUEST_FINE_LOCATION;

public class QueueSearch extends AppCompatActivity {
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private LocationManager locationManager;
    private Location currentLocation;
    private LocationListener locationListener;
    private String[] queueArray;
    private ListView listView;
    private String userName;
    private String product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_search);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        userName = settings.getString("userName", "No info");
        product = settings.getString("product", "No info");

        listView = findViewById(R.id.qs_nearby_list);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);



        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                currentLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }


        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "We need to access your location so we can find queues near you", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }else{
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            //Toast.makeText(this, currentLocation.toString(), Toast.LENGTH_LONG).show();
        }

        new AsyncGetNearbyQueues().execute();
    }

    public void GoToQueue(int pos) {
        Intent intent = new Intent(this, JoinAQueueHomePage.class);
        intent.putExtra("queueName", queueArray[pos]);
        intent.putExtra("userName", userName);
        intent.putExtra("productType", product);
        startActivity(intent);
    }

    public void UpdateQueues(View view) {
        new AsyncGetNearbyQueues().execute();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                } else {
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, CreateJoinQueue.class);
        intent.putExtra("userName", userName);
        intent.putExtra("productType", product);
        startActivity(intent);
    }

    private class AsyncGetNearbyQueues extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(QueueSearch.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pdLoading.dismiss();
            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("https://queuethemusic.000webhostapp.com/get_nearby_queues.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("long", String.valueOf(currentLocation.getLongitude()))
                        .appendQueryParameter("lat", String.valueOf(currentLocation.getLatitude()));
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            pdLoading.dismiss();

            if (result.equalsIgnoreCase("Notset")){
                Toast.makeText(QueueSearch.this, "Could not location", Toast.LENGTH_LONG).show();
            }else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                Toast.makeText(QueueSearch.this, "Oops! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            } else{
                if(result.length() > 1) {
                    result = result.substring(0, result.length() - 1);
                    queueArray = result.split("¦");

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(QueueSearch.this, android.R.layout.simple_list_item_1, queueArray);
                    listView.setAdapter(arrayAdapter);
                }
                else{
                    Toast.makeText(QueueSearch.this, "No queues were found near you :(", Toast.LENGTH_SHORT).show();
                }
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GoToQueue(position);
                }
            });
        }

    }
}
