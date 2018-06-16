package com.becama.queuethemusic.qtm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

public class Signup extends AppCompatActivity {
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private EditText firstName;
    private EditText surname;
    private EditText age;
    private EditText email;
    private EditText password1;
    private EditText password2;
    private String[] details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        details = new String[5];
        firstName = findViewById(R.id.signup_first_name);
        surname = findViewById(R.id.signup_surname);
        age = findViewById(R.id.signup_age);
        email = findViewById(R.id.signup_email);
        password1 = findViewById(R.id.signup_password1);
        password2 =findViewById(R.id.signup_password2);
    }

    public void CreateAccount(View view) {
        if(DetailsFilledIn()){
            SetDetailsArray();
            new AsyncSignup().execute(details);
        }
    }

    private void SetDetailsArray(){
        details[0] = firstName.getText().toString();
        details[1] = surname.getText().toString();
        details[2] = age.getText().toString();
        details[3] = email.getText().toString();
        details[4] = password1.getText().toString();
    }

    private boolean DetailsFilledIn(){
        boolean a = firstName.getText().toString().isEmpty();
        boolean b = surname.getText().toString().isEmpty();
        boolean c = age.getText().toString().isEmpty();
        boolean d = email.getText().toString().isEmpty();
        boolean e = password1.getText().toString().isEmpty();
        boolean f = password2.getText().toString().isEmpty();
        String g = password1.getText().toString();
        String h = password2.getText().toString();

        if(!a && !b && !c && !d && !e && !f && g.equals(h)){
            return true;
        }
        else if(!g.equals(h)){
            Toast.makeText(Signup.this, "The passwords you entered are not the same.", Toast.LENGTH_LONG).show();
            return false;
        }
        else{
            Toast.makeText(Signup.this, "Please fill in all the details", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private class AsyncSignup extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(Signup.this);
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
                url = new URL("https://queuethemusic.000webhostapp.com/create_account.php");

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
                        .appendQueryParameter("first_name", params[0])
                        .appendQueryParameter("surname", params[1])
                        .appendQueryParameter("age", params[2])
                        .appendQueryParameter("email", params[3])
                        .appendQueryParameter("password", params[4]);
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
            Toast.makeText(Signup.this, result, Toast.LENGTH_LONG).show();

            if(result.equalsIgnoreCase("Set"))
            {
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */
                Toast.makeText(Signup.this, "Account created", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Signup.this,LaunchScreen.class);
                startActivity(intent);
                Signup.this.finish();

            }else if (result.equalsIgnoreCase("Notset")){

                // If username and password does not match display a error message
                Toast.makeText(Signup.this, "Please fill in all the fields", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(Signup.this, "OoPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();

            }
        }

    }
}
