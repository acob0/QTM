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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
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

public class CreateAQueueOptions extends AppCompatActivity {
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1524;
    private String userName;
    private String product;
    private String queueName1;
    private LocationManager locationManager;
    private Location currentLocation;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_aqueue_options);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        userName = settings.getString("userName", "No info");
        product = settings.getString("product", "No info");

        Switch passwordSwitch = findViewById(R.id.cqo_password_switch);

        passwordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView textView = findViewById(R.id.cqo_password_text);
                EditText editText = findViewById(R.id.cqo_password_editText);
                ImageView imageView = findViewById(R.id.cqo_password_image);
                if (!isChecked) {
                    textView.setVisibility(View.GONE);
                    editText.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                }else{
                    textView.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        });

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
                Toast.makeText(this, "We need to access your location so only people near you can access your queue", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
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

    public void CreateQueue(View view) {
        EditText queueNameEdit = findViewById(R.id.cqo_queue_name);
        Switch duplicateSwitchEdit = findViewById(R.id.cqo_duplicate_switch);
        Switch passwordSwitch = findViewById(R.id.cqo_password_switch);
        EditText passwordEdit = findViewById(R.id.cqo_password_editText);
        String queueName = queueNameEdit.getText().toString();
        String duplicateSwitch = String.valueOf(duplicateSwitchEdit.isChecked());
        String password = passwordEdit.getText().toString();

        if(password.equals("null")){
            Toast.makeText(this, "Please choose a different password", Toast.LENGTH_SHORT).show();
        }else if(queueName.contains("¦")){
            Toast.makeText(this, "Please do not use ¦ in your queue name", Toast.LENGTH_SHORT).show();
        }else {
            if (password.isEmpty() && !passwordSwitch.isChecked()) {
                password = "null";
            }
            if(queueName.isEmpty() || (passwordSwitch.isChecked() && password.isEmpty())){
                Toast.makeText(this, "Please fill in all the details", Toast.LENGTH_SHORT).show();
            }else{
                queueName1 = queueName;
0                new AsyncCreateQueue().execute(queueName, duplicateSwitch, password);
            }
        }
        locationManager.removeUpdates(locationListener);
    }


    private class AsyncCreateQueue extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(CreateAQueueOptions.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }
        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("https://queuethemusic.000webhostapp.com/create_queue.php");

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
                        .appendQueryParameter("queue_name", params[0])
                        .appendQueryParameter("duplicate_songs", params[1])
                        .appendQueryParameter("password", params[2])
                        .appendQueryParameter("user", userName)
                        .appendQueryParameter("longitude", String.valueOf(currentLocation.getLongitude()))
                        .appendQueryParameter("latitude", String.valueOf(currentLocation.getLatitude()));
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

            //this method will be running on UI thread

            pdLoading.dismiss();
            Toast.makeText(CreateAQueueOptions.this, result, Toast.LENGTH_LONG).show();

            if(result.equalsIgnoreCase("done"))
            {
                Intent intent = new Intent(CreateAQueueOptions.this, CreateAQueueHomePage.class);
                intent.putExtra("userName", userName);
                intent.putExtra("queueName", queueName1);
                startActivity(intent);
                CreateAQueueOptions.this.finish();

            }else if (result.equalsIgnoreCase("invalid")){

                // If username and password does not match display a error message
                Toast.makeText(CreateAQueueOptions.this, "Queue name already taken", Toast.LENGTH_LONG).show();

            }else if(result.equalsIgnoreCase("notset")) {
                Toast.makeText(CreateAQueueOptions.this, "Please fill in all the fields", Toast.LENGTH_LONG).show();
            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(CreateAQueueOptions.this, "OoPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();

            }
        }

    }
}
