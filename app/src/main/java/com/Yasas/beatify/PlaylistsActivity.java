package com.Yasas.beatify;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PlaylistsActivity extends AppCompatActivity {

    private ListView listViewPlaylists;
    private ArrayList<String> playlists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        listViewPlaylists = findViewById(R.id.listViewPlaylists);

        loadPlaylists();

        listViewPlaylists.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPlaylist = playlists.get(position);
            Toast.makeText(this, "Clicked playlist: " + selectedPlaylist, Toast.LENGTH_SHORT).show();

            // TODO: Open playlist detail to show songs inside, allow play etc.
        });
    }

    private void loadPlaylists() {
        playlists = new ArrayList<>();

        // TODO: Load playlists from database or storage
        // For now, add dummy playlists
        playlists.add("Favorites");
        playlists.add("Workout Mix");
        playlists.add("Chill Vibes");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, playlists);
        listViewPlaylists.setAdapter(adapter);
    }
}
