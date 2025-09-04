package com.Yasas.beatify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ThemeCustomizationActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_BG_COLOR = "background_color";
    private static final String KEY_BG_IMAGE_URI = "background_image_uri";

    private Button buttonPickColor, buttonPickImage, buttonReset;

    private SharedPreferences sharedPreferences;

    // Launcher for picking an image from gallery
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_customization);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        buttonPickColor = findViewById(R.id.buttonPickColor);
        buttonPickImage = findViewById(R.id.buttonPickImage);
        buttonReset = findViewById(R.id.buttonReset);

        // Color picker: For simplicity, show a few preset colors using a dialog or Toast.
        buttonPickColor.setOnClickListener(v -> {
            // For simplicity, pick one color (or implement a real color picker library)
            // Here let's cycle through some colors just as example:
            int currentColor = sharedPreferences.getInt(KEY_BG_COLOR, Color.WHITE);
            int newColor;
            if (currentColor == Color.WHITE) newColor = Color.LTGRAY;
            else if (currentColor == Color.LTGRAY) newColor = Color.CYAN;
            else newColor = Color.WHITE;

            sharedPreferences.edit()
                    .putInt(KEY_BG_COLOR, newColor)
                    .remove(KEY_BG_IMAGE_URI) // clear image if color is chosen
                    .apply();

            // Notify user
            Toast.makeText(this, "Background color changed", Toast.LENGTH_SHORT).show();
        });

        // Image picker from gallery
        buttonPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            sharedPreferences.edit()
                                    .putString(KEY_BG_IMAGE_URI, imageUri.toString())
                                    .remove(KEY_BG_COLOR) // clear color if image is chosen
                                    .apply();

                            Toast.makeText(this, "Background image changed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        buttonReset.setOnClickListener(v -> {
            sharedPreferences.edit().clear().apply();
            Toast.makeText(this, "Background reset to default", Toast.LENGTH_SHORT).show();
        });
    }
}
