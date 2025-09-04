package com.Yasas.beatify;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ArtistsActivity extends AppCompatActivity {

    private ListView listViewArtists;
    private ArrayList<String> artistNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artists);

        listViewArtists = findViewById(R.id.listViewArtists);

        loadArtists();

        listViewArtists.setOnItemClickListener((parent, view, position, id) -> {
            String selectedArtist = artistNames.get(position);
            // Open songs by this artist in PlayerActivity or new activity
            Toast.makeText(this, "Clicked artist: " + selectedArtist, Toast.LENGTH_SHORT).show();

            // TODO: Implement showing songs by artist
        });
    }

    private void loadArtists() {
        artistNames = new ArrayList<>();
        Uri collection = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        };

        try (Cursor cursor = getContentResolver().query(
                collection,
                projection,
                null,
                null,
                MediaStore.Audio.Artists.ARTIST + " ASC")) {

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));
                    artistNames.add(artist);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, artistNames);
                listViewArtists.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No artists found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading artists", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
