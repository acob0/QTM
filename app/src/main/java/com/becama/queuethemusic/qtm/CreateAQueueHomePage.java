package com.becama.queuethemusic.qtm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ImageView;
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
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

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
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreateAQueueHomePage extends Activity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private static final String CLIENT_ID = "9c7162b0f0d7414983f3fca6df606f14";
    private static final String REDIRECT_URI = "queuethemusicbecama://callback";

    private static final int REQUEST_CODE = 1437;
    public static final String PREFS_NAME = "MyPrefsFile";

    public Player mPlayer;

    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify;
    TrackInfoAdapter searchAdapter;
    TrackInfoAdapter queueAdapter;
    ArrayList<TrackInfo> nameArtistArrayList;
    ArrayList<TrackInfo> musicQueue;
    String[] queueArrayUri;
    int queueCounter;
    ListView mainListView;
    private String userName;
    private String queueName;
    private String product;

    private Handler mHandler = new Handler();
    SeekBar seekBar;

    ProgressDialog pdLoading;

    ConstraintLayout qhpConstrainLayout;
    ConstraintLayout hssConstrainLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_aqueue_home_page);
        ImageView imageView = findViewById(R.id.qhp_imageView);
        imageView.setAlpha(0.35f);

        Bundle bundle = getIntent().getExtras();
        queueName = bundle.getString("queueName");

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        userName = settings.getString("userName", "No info");
        product = settings.getString("product", "No info");

        authenticate();

        qhpConstrainLayout = findViewById(R.id.qhp_constraint_layout);
        hssConstrainLayout = findViewById(R.id.hss_constraint_layout);
        qhpConstrainLayout.setVisibility(View.VISIBLE);
        hssConstrainLayout.setVisibility(View.GONE);

        musicQueue = new ArrayList<>();
        queueCounter = 0;
        nameArtistArrayList = new ArrayList<>();
        searchAdapter = new TrackInfoAdapter(this, nameArtistArrayList);
        queueAdapter = new TrackInfoAdapter(this, musicQueue);

        mainListView = findViewById(R.id.hss_listview);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrackInfo trackInfo = nameArtistArrayList.get(position);
                musicQueue.add(trackInfo);

                new AsyncAddSong().execute(trackInfo.uri);

                /*ListView listView = findViewById(R.id.qhp_listview);
                queueAdapter.getCount();
                ArrayList<TrackInfo> test = new ArrayList<>();
                int j = musicQueue.size() - queueAdapter.getCount();
                for (int i = 0; i < j; i++) {
                    test.add(musicQueue.get(queueAdapter.getCount() + i));
                    queueAdapter.add(test.get(i));
                }
                listView.setAdapter(queueAdapter);*/
            }
        });

        ListView queueListView = findViewById(R.id.qhp_listview);
        queueListView.setAdapter(queueAdapter);
        queueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                queueCounter = i;
                setSongInfo();
            }
        });

        seekBar = findViewById(R.id.qhp_seekBar);
        CreateAQueueHomePage.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    int mCurrentPosition = (int) mPlayer.getPlaybackState().positionMs;
                    seekBar.setProgress(mCurrentPosition);

                    int totalSecs = seekBar.getMax() - mCurrentPosition;
                    int minutes = (totalSecs % 3600) / 60;
                    int seconds = totalSecs % 60;

                    String timeString = String.format("%02d:%02d", minutes, seconds);
                    TextView timer = findViewById(R.id.qhp_timer);
                    timer.setText(timeString);

                    /*if (mCurrentPosition == musicQueue.get(queueCounter).length /*&& !mPlayer.getPlaybackState().isPlaying) {
                        View view = findViewById(R.id.qhp_next);
                        nextSong(view);
                    }*/
                    Log.d("PLAYER", "Position set at " + mCurrentPosition);
                    Log.d("PLAYER", "Playing " + mPlayer.getPlaybackState().isPlaying);

                } catch (Exception e) {
                    Log.d("PLAYER", "Player not available");
                }
                mHandler.postDelayed(this, 1000);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int totalSecs = seekBar.getMax() - progress;
                int minutes = (totalSecs % 3600) / 60;
                int seconds = totalSecs % 60;

                String timeString = String.format("%02d:%02d", minutes, seconds);
                TextView timer = findViewById(R.id.qhp_timer);
                timer.setText(timeString);

                if (fromUser) {
                    mPlayer.seekToPosition(null, progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        pdLoading = new ProgressDialog(CreateAQueueHomePage.this);
    }

    public void goToHostSongSearch(View view) {
        qhpConstrainLayout.setVisibility(View.GONE);
        hssConstrainLayout.setVisibility(View.VISIBLE);
    }

    public void searchSong(View view) {
        searchAdapter.clear();
        EditText editText = findViewById(R.id.hss_song_search);
        final ListView listView = findViewById(R.id.hss_listview);
        final String track = editText.getText().toString();

        boolean a;
        String b;
        if (spotify == null)
            a = false;
        else
            a = true;
        b = String.valueOf(a);
        Toast.makeText(CreateAQueueHomePage.this, b, Toast.LENGTH_SHORT).show();
        Toast.makeText(CreateAQueueHomePage.this, track, Toast.LENGTH_SHORT).show();

        Log.d("SPOTIFY", b);
        Log.d("SPOTIFY", track);


        spotify.searchTracks(track, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                for (int i = 0; i < 20; i++) {
                    nameArtistArrayList.add(new TrackInfo());
                    nameArtistArrayList.get(i).trackName = tracksPager.tracks.items.get(i).name;
                    nameArtistArrayList.get(i).artistName = tracksPager.tracks.items.get(i).artists.get(0).name;
                    nameArtistArrayList.get(i).length = (int) (tracksPager.tracks.items.get(i).duration_ms);
                    nameArtistArrayList.get(i).uri = tracksPager.tracks.items.get(i).id;

                    try {
                        nameArtistArrayList.get(i).imageURL = tracksPager.tracks.items.get(i).album.images.get(0).url;
                    } catch (Exception e) {
                        try {
                            nameArtistArrayList.get(i).imageURL = tracksPager.tracks.items.get(i).album.images.get(1).url;
                        } catch (Exception o) {
                            nameArtistArrayList.get(i).imageURL = "";
                            Log.d("FAILED", tracksPager.tracks.items.get(i).name);
                        }
                    }
                }
                searchAdapter.addAll(nameArtistArrayList);
                listView.setAdapter(searchAdapter);
                //mainListView = listView;
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("FAILURE", "You failed...");
                Toast.makeText(CreateAQueueHomePage.this, "Could not retrieve any songs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void playPause(View view) {
        PlaybackState playbackState = mPlayer.getPlaybackState();
        if (playbackState.isPlaying)
            mPlayer.pause(null);
        else
            mPlayer.resume(null);

        view.setActivated(!view.isActivated());
    }

    public void nextSong(View view) {
        if (musicQueue.size() > 0) {
            queueCounter++;

            if (queueCounter > musicQueue.size() - 1)
                queueCounter = 0;

            setSongInfo();
        }
    }

    public void previousSong(View view) {
        if (musicQueue.size() > 0) {
            queueCounter--;

            if (queueCounter < 0)
                queueCounter = musicQueue.size() - 1;

            setSongInfo();
        }
    }

    @Override
    public void onBackPressed() {

        if (qhpConstrainLayout.getVisibility() == View.VISIBLE) {
            Intent intent = new Intent(this, CreateJoinQueue.class);
            intent.putExtra("userName", userName);
            intent.putExtra("productType", product);
            startActivity(intent);
            finish();
        }
        if (hssConstrainLayout.getVisibility() == View.VISIBLE) {
            if (!mPlayer.getPlaybackState().isPlaying && musicQueue.size() > 0)
                setSongInfo();
            hssConstrainLayout.setVisibility(View.GONE);
            qhpConstrainLayout.setVisibility(View.VISIBLE);
        } else{
            Intent intent = new Intent(this, CreateJoinQueue.class);
            intent.putExtra("userName", userName);
            intent.putExtra("productType", product);
            startActivity(intent);
            finish();
        }
    }

    public void setSongInfo() {
        mPlayer.playUri(null, musicQueue.get(queueCounter).uri, 0, 0);
        TextView songName = findViewById(R.id.qhp_song_name);
        TextView artistName = findViewById(R.id.qhp_artists);
        ImageView imageView = findViewById(R.id.qhp_imageView);
        songName.setText(musicQueue.get(queueCounter).trackName);
        artistName.setText(musicQueue.get(queueCounter).artistName);
        if (!musicQueue.get(queueCounter).imageURL.equals("")) {
            Picasso.with(getApplicationContext()).load(musicQueue.get(queueCounter).imageURL).into(imageView);
        }
        Log.d("COUNTER", musicQueue.get(queueCounter).trackName);
        Log.d("COUNTER", musicQueue.get(queueCounter).artistName);
        //Log.d("COUNTER", musicQueue.get(queueCounter).length);
        int j = musicQueue.get(queueCounter).length / 1000;
        Log.d("PLAYER", "MAX SET " + j);
        seekBar.setMax(j);
    }

    /*@Override
    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        for (int i = 0; i < musicQueue.size(); i++) {
            editor.putString("Name" + Integer.toString(i), musicQueue.get(i).trackName);
            editor.putString("Artist" + Integer.toString(i), musicQueue.get(i).artistName);
            editor.putString("Uri" + Integer.toString(i), musicQueue.get(i).uri);
            editor.putInt("Length" + Integer.toString(i), musicQueue.get(i).length);
            editor.putString("ImageURL" + Integer.toString(i), musicQueue.get(i).imageURL);
        }

        editor.putInt("size", musicQueue.size());

        // Commit the edits!
        editor.commit();
    }*/

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
                new AsyncGetQueueSongs().execute();
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(CreateAQueueHomePage.this);
                        mPlayer.addNotificationCallback(CreateAQueueHomePage.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                        Toast.makeText(CreateAQueueHomePage.this, "Error could not log in", Toast.LENGTH_SHORT).show();
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
        Log.d("MAINACTIVITY", "User logged in");
        /*if (musicQueue.size() > 0) {
            Log.d("CREATE", "Info: " + musicQueue.get(0).trackName + musicQueue.get(0).artistName + musicQueue.get(0).uri + queueCounter);
            setSongInfo();
        }
        Log.d("MQUEUE", "3: " + musicQueue.size());

        ListView listView = findViewById(R.id.qhp_listview);
        queueAdapter.getCount();
        ArrayList<TrackInfo> test = new ArrayList<>();
        int j = musicQueue.size() - queueAdapter.getCount();
        for (int i = 0; i < j; i++) {
            test.add(musicQueue.get(queueAdapter.getCount() + i));
            queueAdapter.add(test.get(i));
        }
        listView.setAdapter(queueAdapter);
        Toast toast = Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT);
        toast.show();*/
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
        //ProgressDialog pdLoading = new ProgressDialog(CreateAQueueHomePage.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            /*pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();*/
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
                Toast.makeText(CreateAQueueHomePage.this, "Could not get queue name", Toast.LENGTH_LONG).show();
            }

            if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                Toast.makeText(CreateAQueueHomePage.this, "Oops! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }else if(result.isEmpty()){
                Toast.makeText(CreateAQueueHomePage.this, "No songs in the queue yet", Toast.LENGTH_LONG).show();
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
            ListView listView = findViewById(R.id.qhp_listview);
            TrackInfoAdapter trackInfoAdapter = new TrackInfoAdapter(CreateAQueueHomePage.this, musicQueue);
            listView.setAdapter(trackInfoAdapter);
        }

    }

    private class AsyncAddSong extends AsyncTask<String, String, String>
    {
        //ProgressDialog pdLoading = new ProgressDialog(CreateAQueueHomePage.this);
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
                Log.d("QUEUEPARAM", queueName + " " + params[0]);
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

            //pdLoading.dismiss();

            if (result.equalsIgnoreCase("Notset")){
                Toast.makeText(CreateAQueueHomePage.this, "Could not get queue name", Toast.LENGTH_LONG).show();
            }else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                Toast.makeText(CreateAQueueHomePage.this, "Oops! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }else{
                if (musicQueue.size() > 0) {
                    Log.d("CREATE", "Info: " + musicQueue.get(0).trackName + musicQueue.get(0).artistName + musicQueue.get(0).uri + queueCounter);
                    //setSongInfo();
                }
            }

            new AsyncGetQueueSongs().execute();
        }
    }
}
