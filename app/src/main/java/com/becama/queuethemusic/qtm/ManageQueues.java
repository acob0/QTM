package com.becama.queuethemusic.qtm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
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
import java.util.ArrayList;

import static com.becama.queuethemusic.qtm.CreateAQueueHomePage.PREFS_NAME;

public class ManageQueues extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private ListView listView;
    private String userName;
    private String product;
    private String[] queueArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_queues);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        userName = settings.getString("userName", "No info");
        product = settings.getString("product", "No info");

        listView = findViewById(R.id.mq_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GoToQueue(position);
            }
        });

        new AsyncGetQueues().execute();
    }

    public void GoToQueue(int pos) {
        Intent intent = new Intent(this, CreateAQueueHomePage.class);
        intent.putExtra("queueName", queueArray[pos]);
        intent.putExtra("userName", userName);
        intent.putExtra("productType", product);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, CreateJoinQueue.class);
        intent.putExtra("userName", userName);
        intent.putExtra("productType", product);
        startActivity(intent);
    }

    private class AsyncGetQueues extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(ManageQueues.this);
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
                url = new URL("https://queuethemusic.000webhostapp.com/get_your_queues.php");

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
                        .appendQueryParameter("user", userName);
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
                Toast.makeText(ManageQueues.this, "Could not get username", Toast.LENGTH_LONG).show();
            }else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                Toast.makeText(ManageQueues.this, "Oops! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
                finish();
            } else{
                if(result.length() > 1) {
                    result = result.substring(0, result.length() - 1);
                    queueArray = result.split("¦");

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ManageQueues.this, android.R.layout.simple_list_item_1, queueArray);
                    listView.setAdapter(arrayAdapter);
                }
                else{
                    Toast.makeText(ManageQueues.this, "You have no queues active", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
