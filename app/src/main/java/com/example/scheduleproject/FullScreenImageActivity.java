package com.example.scheduleproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import android.util.Log;
import android.widget.Toast;

public class FullScreenImageActivity extends AppCompatActivity {

    private static final String TAG = "FullScreenImageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        TouchImageView touchImageView = findViewById(R.id.full_screen_image_view);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        imageUrl = convertGoogleDriveUrlToDirect(imageUrl);
        Log.d(TAG, "Loading image from URL: " + imageUrl);

        Picasso.get().load(imageUrl).into(touchImageView, new Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Image loaded successfully.");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading image", e);
                Toast.makeText(FullScreenImageActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String convertGoogleDriveUrlToDirect(String url) {
        if (url.contains("open?id=")) {
            return url.replace("open?id=", "uc?export=download&id=");
        } else if (url.contains("/file/d/")) {
            String fileId = url.split("/file/d/")[1].split("/")[0];
            return "https://drive.google.com/uc?export=download&id=" + fileId;
        }
        return url;
    }
}
