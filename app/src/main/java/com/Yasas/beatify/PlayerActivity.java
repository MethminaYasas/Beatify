package com.Yasas.beatify;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    private Button play, next, previous;
    private TextView textView;
    private MediaPlayer mediaPlayer;
    private int position;
    private ArrayList<String> songs; // List of song file paths
    private String songName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        textView = findViewById(R.id.songLabel);

        Intent intent = getIntent();
        songs = intent.getStringArrayListExtra("songs_paths");
        songName = intent.getStringExtra("songname");
        position = intent.getIntExtra("pos", 0);

        playSong(position);

        play.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    play.setText("▶️");
                } else {
                    mediaPlayer.start();
                    play.setText("⏸️");
                }
            }
        });

        next.setOnClickListener(v -> {
            if (songs != null && songs.size() > 0) {
                position = (position + 1) % songs.size();
                playSong(position);
            }
        });

        previous.setOnClickListener(v -> {
            if (songs != null && songs.size() > 0) {
                position = (position - 1 < 0) ? songs.size() - 1 : position - 1;
                playSong(position);
            }
        });
    }

    private void playSong(int pos) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            File songFile = new File(songs.get(pos));
            Uri uri = Uri.fromFile(songFile);
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);

            songName = songFile.getName().replace(".mp3", "").replace(".wav", "");
            textView.setText(songName);
            textView.setSelected(true);

            mediaPlayer.start();
            play.setText("⏸️");

            // Auto play next song on completion
            mediaPlayer.setOnCompletionListener(mp -> {
                position = (position + 1) % songs.size();
                playSong(position);
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
