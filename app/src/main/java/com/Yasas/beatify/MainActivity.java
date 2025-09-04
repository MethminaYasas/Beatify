package com.Yasas.beatify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.util.ArrayList;

import android.database.Cursor;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    private ArrayList<String> songPaths; // List of song file paths
    private ListView listView;
    private String[] items; // Song names for display
    private View rootLayout;
    private Button buttonCustomizeTheme;

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_BG_COLOR = "background_color";
    private static final String KEY_BG_IMAGE_URI = "background_image_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.rootLayout);
        listView = findViewById(R.id.listViewSongs);
        buttonCustomizeTheme = findViewById(R.id.buttonCustomizeTheme);

        applySavedTheme();

        buttonCustomizeTheme.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThemeCustomizationActivity.class);
            startActivity(intent);
        });

        checkPermissionsAndLoadSongs();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String songName = listView.getItemAtPosition(position).toString();

            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putStringArrayListExtra("songs_paths", songPaths);
            intent.putExtra("songname", songName);
            intent.putExtra("pos", position);
            startActivity(intent);
        });
    }

    private void checkPermissionsAndLoadSongs() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
            } else {
                loadSongsFromMediaStore();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            } else {
                loadSongsFromMediaStore();
            }
        }
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String bgImageUriString = prefs.getString(KEY_BG_IMAGE_URI, null);
        int bgColor = prefs.getInt(KEY_BG_COLOR, Color.WHITE);

        if (bgImageUriString != null) {
            InputStream is = null;
            try {
                Uri bgImageUri = Uri.parse(bgImageUriString);
                is = getContentResolver().openInputStream(bgImageUri);
                if (is != null) {
                    Drawable drawable = Drawable.createFromStream(is, bgImageUri.toString());
                    rootLayout.setBackground(drawable);
                } else {
                    rootLayout.setBackgroundColor(bgColor);
                }
            } catch (Exception e) {
                e.printStackTrace();
                rootLayout.setBackgroundColor(bgColor);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            rootLayout.setBackgroundColor(bgColor);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySavedTheme(); // Re-apply theme on resume
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadSongsFromMediaStore();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSongsFromMediaStore() {
        songPaths = new ArrayList<>();
        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME
        };

        try (Cursor cursor = getContentResolver().query(
                collection,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.DISPLAY_NAME + " ASC")) {

            if (cursor != null && cursor.getCount() > 0) {
                items = new String[cursor.getCount()];
                int i = 0;
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

                    songPaths.add(path);
                    items[i++] = name.replace(".mp3", "").replace(".wav", "");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, items);
                listView.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading songs", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // MENU setup

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search songs");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                handleSongSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false; // no live filtering implemented
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_songs) {
            loadSongsFromMediaStore();
            Toast.makeText(this, "Showing all songs", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_artists) {
            // TODO: implement ArtistsActivity
            Intent intent = new Intent(this, ArtistsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_playlists) {
            // TODO: implement PlaylistsActivity
            Intent intent = new Intent(this, PlaylistsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_search) {
            // handled by SearchView
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleSongSearch(String query) {
        boolean found = false;
        if (items != null) {
            for (String songName : items) {
                if (songName.toLowerCase().contains(query.trim().toLowerCase())) {
                    found = true;
                    break;
                }
            }
        }

        if (found) {
            Toast.makeText(this, "Song found in library: " + query, Toast.LENGTH_SHORT).show();
            // Optional: scroll to song or play it
        } else {
            // Open YouTube search for song
            String youtubeSearchUrl = "https://www.youtube.com/results?search_query=" + Uri.encode(query);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeSearchUrl));
            startActivity(intent);
        }
    }
}
