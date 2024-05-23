package com.example.scheduleproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.AsyncTaskLoader;

//import com.github.barteksc.pdfviewer.PDFView;
//import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousChannelGroup;

public class FullScreenImageActivity extends AppCompatActivity {

  //  private PDFView pdfView;
    private ProgressBar progressBar;
    private static final String TAG = "FullScreenImageActivity";
    private String pdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        progressBar = findViewById(R.id.progress_bar);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set custom font for the ActionBar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater inflater = LayoutInflater.from(this);
            TextView customTitleView = (TextView) inflater.inflate(R.layout.action_bar_title, null);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_1); // Set your custom drawable
            String title = getIntent().getStringExtra("title");
            if(!TextUtils.isEmpty(title)){
                customTitleView.setText(getImageNameWithoutExtension(title));
            }



            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setCustomView(customTitleView);
            actionBar.setDisplayShowCustomEnabled(true);


            String imageUrl = getIntent().getStringExtra("imageUrl");
             pdfUri = getIntent().getStringExtra("pdfUrl");
            if(imageUrl != null){
                displayImage(imageUrl);
            }
            if(pdfUri != null){

             //   displayPDF(pdfUri);
            }

            // Check for internet connectivity
            if (!isNetworkAvailable()) {
                // No internet connection available, notify the user
                Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show();
            }

        }


    }
    // Method to check for internet connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void displayImage(String imageUrl){
        TouchImageView touchImageView = findViewById(R.id.full_screen_image_view);
        touchImageView.setVisibility(View.VISIBLE);
        imageUrl = convertGoogleDriveUrlToDirect(imageUrl);
        Log.d(TAG, "Loading image from URL: " + imageUrl);
        progressBar.setVisibility(View.VISIBLE);
        Picasso.get().load(imageUrl).into(touchImageView, new Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Image loaded successfully.");
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading image", e);
                Toast.makeText(FullScreenImageActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });



    }

    private void displayPDF(String pdfUrl){
    //    pdfView = findViewById(R.id.pdfView);
    //    pdfView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

     //   String pdfUrl = getIntent().getStringExtra("pdfUrl");
      /*  if (pdfUrl != null) {
            new Thread(() -> {
                try {
                    File file = downloadFile(pdfUrl);
                    runOnUiThread(() -> pdfView.fromFile(file).load());
                   // progressBar.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                  //  progressBar.setVisibility(View.GONE);
                }
            }).start();
        }*/
        new RetrievePDFfromUrl().execute(pdfUrl);
    }

    class RetrievePDFfromUrl extends AsyncTask<String, Void, InputStream>{


        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null;

            try{
                URL url = new URL((strings[0]));
                HttpURLConnection urlConnection  = (HttpURLConnection) url.openConnection();
                if(urlConnection.getResponseCode() == 200){
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
                return  null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            progressBar.setVisibility(View.GONE);
        /*    pdfView.fromStream(inputStream)
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                    .password(null)
                    .scrollHandle(null)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    .spacing(0)
                  // .fitEachPage(false) // fit each page to the view, else smaller pages are scaled relative to largest page.
                // .pageSnap(false) // snap pages to screen boundaries
                 //   .pageFling(false) // make a fling change only a single page like ViewPager
               //   .nightMode(false) // toggle night mode
                    .load();*/
        }
    }
    private File downloadFile(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        File tempFile = File.createTempFile("tempPdf", ".pdf", getCacheDir());
        FileOutputStream fileOutput = new FileOutputStream(tempFile);
        InputStream inputStream = urlConnection.getInputStream();

        byte[] buffer = new byte[1024];
        int bufferLength;
        while ((bufferLength = inputStream.read(buffer)) > 0) {
            fileOutput.write(buffer, 0, bufferLength);
        }
        fileOutput.close();

        return tempFile;
    }
    public String getImageNameWithoutExtension(String attachmentName) {
        if (attachmentName == null || attachmentName.isEmpty()) {
            return "";
        }

        int dotIndex = attachmentName.lastIndexOf('.');
        if (dotIndex == -1) {
            return attachmentName; // No extension found
        }

        return attachmentName.substring(0, dotIndex);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
