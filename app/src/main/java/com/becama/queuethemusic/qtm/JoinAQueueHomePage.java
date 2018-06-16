package com.becama.queuethemusic.qtm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

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

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.becama.queuethemusic.qtm.CreateAQueueHomePage.PREFS_NAME;

public class JoinAQueueHomePage extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private static final String CLIENT_ID = "9c7162b0f0d7414983f3fca6df606f14";
    private static final String REDIRECT_URI = "queuethemusicbecama://callback";

    private static final int REQUEST_CODE = 1437;
    public Player mPlayer;

    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify;
    TrackInfoAdapter searchAdapter;
    TrackInfoAdapter queueAdapter;
    ArrayList<TrackInfo> trackInfoArrayList;
    ArrayList<TrackInfo> musicQueue;
    String[] queueArrayUri;
    int queueCounter;
    ListView searchListview;
    ListView currentQueueListview;
    private String queueName;
    private String userName;
    private String product;

    private Handler mHandler = new Handler();
    SeekBar seekBar;

    ConstraintLayout qhpConstrainLayout;
    ConstraintLayout hssConstrainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_aqueue_home_page);

        Bundle bundle = getIntent().getExtras();
        queueName = bundle.getString("queueName");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        userName = settings.getString("userName", "No info");
        product = settings.getString("product", "No info");
        TextView textView = findViewById(R.id.jqhp_queue_name);
        textView.setText(queueName);

        musicQueue = new ArrayList<>();
        queueCounter = 0;
        trackInfoArrayList = new ArrayList<>();
        searchAdapter = new TrackInfoAdapter(this, trackInfoArrayList);
        queueAdapter = new TrackInfoAdapter(this, musicQueue);
        currentQueueListview = findViewById(R.id.jqhp_current_queue_listview);
        searchListview = findViewById(R.id.jqhp_search_song_listview);

        searchListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrackInfo trackInfo = trackInfoArrayList.get(position);
                musicQueue.add(trackInfo);

                //ListView listView = findViewById(R.id.qhp_listview);


                new AsyncAddSong().execute(trackInfo.uri);

                /*queueAdapter.getCount();
                ArrayList<TrackInfo> test = new ArrayList<>();
                int j = musicQueue.size() - queueAdapter.getCount();
                for (int i = 0; i < j; i++) {
                    test.add(musicQueue.get(queueAdapter.getCount() + i));
                    queueAdapter.add(test.get(i));
                }
                currentQueueListview.setAdapter(queueAdapter);*/
            }
        });


        authenticate();
    }


    public void SearchSong(View view) {
        searchAdapter.clear();
        EditText editText = findViewById(R.id.jqhp_song_search_edit);
        final ListView listView = findViewById(R.id.jqhp_search_song_listview);
        final String track = editText.getText().toString();

        boolean a;
        String b;
        if (spotify == null)
            a = false;
        else
            a = true;
        b = String.valueOf(a);
        Toast.makeText(JoinAQueueHomePage.this, b, Toast.LENGTH_SHORT).show();
        Toast.makeText(JoinAQueueHomePage.this, track, Toast.LENGTH_SHORT).show();

        Log.d("SPOTIFY", b);
        Log.d("SPOTIFY", track);


        spotify.searchTracks(track, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                for (int i = 0; i < 20; i++) {
                    trackInfoArrayList.add(new TrackInfo());
                    trackInfoArrayList.get(i).trackName = tracksPager.tracks.items.get(i).name;
                    trackInfoArrayList.get(i).artistName = tracksPager.tracks.items.get(i).artists.get(0).name;
                    trackInfoArrayList.get(i).length = (int) (tracksPager.tracks.items.get(i).duration_ms);
                    trackInfoArrayList.get(i).uri = tracksPager.tracks.items.get(i).id;

                    try {
                        trackInfoArrayList.get(i).imageURL = tracksPager.tracks.items.get(i).album.images.get(0).url;
                    } catch (Exception e) {
                        try {
                            trackInfoArrayList.get(i).imageURL = tracksPager.tracks.items.get(i).album.images.get(1).url;
                        } catch (Exception o) {
                            trackInfoArrayList.get(i).imageURL = "";
                            Log.d("FAILED", tracksPager.tracks.items.get(i).name);
                        }
                    }
                }
                searchAdapter.addAll(trackInfoArrayList);
                listView.setAdapter(searchAdapter);
                searchListview = listView;
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("FAILURE", "You failed...");
                Toast.makeText(JoinAQueueHomePage.this, "Could not retrieve any songs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, QueueSearch.class);
        intent.putExtra("userName", userName);
        intent.putExtra("productType", product);
        startActivity(intent);
        finish();
    }

    private void authenticate() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-read-email", "user-read-birthdate", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            api.setAccessToken(response.getAccessToken());
            spotify = api.getService();
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                api.setAccessToken(response.getAccessToken());
                new AsyncGetQueueSongs().execute();
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(JoinAQueueHomePage.this);
                        mPlayer.addNotificationCallback(JoinAQueueHomePage.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                        Toast.makeText(JoinAQueueHomePage.this, "Error could not log in", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        new AsyncGetQueueSongs().execute();
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error i) {
        Toast toast = Toast.makeText(this, "Login failed...", Toast.LENGTH_SHORT);
        toast.show();
        Log.d("MainActivity", "Login failed");
        finish();
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    private class AsyncGetQueueSongs extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(JoinAQueueHomePage.this);
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
                url = new URL("https://queuethemusic.000webhostapp.com/get_queue_songs.php");

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
                        .appendQueryParameter("queue_name", queueName);
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
                Toast.makeText(JoinAQueueHomePage.this, "Could not get queue name", Toast.LENGTH_LONG).show();
            }

            if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                Toast.makeText(JoinAQueueHomePage.this, "Oops! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }else if(result.isEmpty()){
                Toast.makeText(JoinAQueueHomePage.this, "No songs in the queue yet", Toast.LENGTH_LONG).show();
            }
            else{
                result = result.substring(0, result.length() - 1);
                queueArrayUri = result.split("¦");

                for (int i = 0; i < queueArrayUri.length; i++) {

                    spotify.getTrack(queueArrayUri[i], new Callback<Track>() {
                        @Override
                        public void success(Track track1, Response response) {
                            Log.d("Track success", track1.name);
                            final Track track;
                            track = track1;
                            musicQueue.add(new TrackInfo());
                            musicQueue.get(musicQueue.size()-1).trackName = track.name;
                            musicQueue.get(musicQueue.size()-1).artistName = track.artists.get(0).name;
                            musicQueue.get(musicQueue.size()-1).uri = track.id;
                            musicQueue.get(musicQueue.size()-1).length = (int)(track.duration_ms);
                            try {
                                musicQueue.get(musicQueue.size()-1).imageURL = track.album.images.get(0).url;
                            } catch (Exception e) {
                                try {
                                    musicQueue.get(musicQueue.size()-1).imageURL = track.album.images.get(1).url;
                                } catch (Exception o) {
                                    musicQueue.get(musicQueue.size()-1).imageURL = "";
                                }
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d("Album failure", error.toString());
                        }
                    });/*
                    final Track track;
                    track = spotify.getTrack(queueArrayUri[i]);
                    musicQueue.add(new TrackInfo());
                    musicQueue.get(i).trackName = track.name;
                    musicQueue.get(i).artistName = track.artists.get(0).toString();
                    musicQueue.get(i).uri = queueArrayUri[i];
                    musicQueue.get(i).length = (int)(track.duration_ms);
                    try {
                        musicQueue.get(i).imageURL = track.album.images.get(0).url;
                    } catch (Exception e) {
                        try {
                            musicQueue.get(i).imageURL = track.album.images.get(1).url;
                        } catch (Exception o) {
                            musicQueue.get(i).imageURL = "";
                        }
                    }*/
                }
            }
            TrackInfoAdapter trackInfoAdapter = new TrackInfoAdapter(JoinAQueueHomePage.this, musicQueue);
            currentQueueListview.setAdapter(trackInfoAdapter);
        }

    }

    private class AsyncAddSong extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(JoinAQueueHomePage.this);
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
                url = new URL("https://queuethemusic.000webhostapp.com/add_song_to_queue.php");

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
                        .appendQueryParameter("queue_name", queueName)
                        .appendQueryParameter("uri", params[0]);
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
                Toast.makeText(JoinAQueueHomePage.this, "Could not get queue name", Toast.LENGTH_LONG).show();
            }else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                Toast.makeText(JoinAQueueHomePage.this, "Oops! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }
            new AsyncGetQueueSongs().execute();
        }
    }
}
