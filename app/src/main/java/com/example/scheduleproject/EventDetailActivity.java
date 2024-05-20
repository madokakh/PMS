package com.example.scheduleproject;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttachment;
import com.google.api.services.drive.Drive;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    private GoogleAccountCredential credential;
    private RecyclerView attachmentsRecyclerView;
    private AttachmentsAdapter attachmentsAdapter;
    private List<EventAttachment> attachmentsList;
    private LinearLayout linearLayoutRemarked;
    private LinearLayout linearLayoutLocation;
    private LinearLayout linearLayoutAttchements;
    private Drive googleDriveService;
    private TextView tvAM_PM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //getSupportActionBar().setTitle(R.string.detail_acitivity_title);


        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Center the title
        TextView toolbarTitle = findViewById(R.id.action_bar_title);
        toolbarTitle.setText(R.string.detail_acitivity_title);  // Set your title here
   /*
        // Enable the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Hide the default title
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }*/

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_1); // Set your custom drawable

            getSupportActionBar() .setDisplayShowTitleEnabled(false);


            getSupportActionBar() .setDisplayShowCustomEnabled(true);
        }

        TextView textDate = findViewById(R.id.text_date);
        TextView textTime = findViewById(R.id.text_time);
        TextView textDescription = findViewById(R.id.text_description);
        TextView textTitle = findViewById(R.id.text_title);
        TextView textLocation = findViewById(R.id.location);
        linearLayoutRemarked = findViewById(R.id.llRemarked);
        linearLayoutLocation = findViewById(R.id.llLocation);
        linearLayoutAttchements = findViewById(R.id.llAttachments);

        tvAM_PM = findViewById(R.id.am_pm);
        attachmentsRecyclerView = findViewById(R.id.attachments_recycler_view);
        attachmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        attachmentsList = new ArrayList<>();
        attachmentsAdapter = new AttachmentsAdapter(attachmentsList, this::onAttachmentClick);
        attachmentsRecyclerView.setAdapter(attachmentsAdapter);

        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String description = getIntent().getStringExtra("description");
        if(description != null){
            linearLayoutRemarked.setVisibility(View.VISIBLE);
        }


        String title = getIntent().getStringExtra("title");
        String eventId = getIntent().getStringExtra("eventId");
        String location = getIntent().getStringExtra("location");
        if(location != null){
            linearLayoutLocation.setVisibility(View.VISIBLE);
        }

        textDate.setText(date);
        textTime.setText(time);
        textDescription.setText(description);
        textTitle.setText(title);
        textLocation.setText(location);
        setAM_PMText(time);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Collections.singleton(com.google.api.services.calendar.CalendarScopes.CALENDAR));
            credential.setSelectedAccount(account.getAccount());
            googleDriveService = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("Your App Name")
                    .build();
            new LoadEventAttachmentsTask().execute(eventId);
        } else {
            Toast.makeText(this, "User is not signed in", Toast.LENGTH_SHORT).show();
        }
    }



    private void setAM_PMText(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date date = sdf.parse(time);
            int hours = date.getHours();
            if (hours < 12) {
                tvAM_PM.setText(R.string.morning);
            } else if(hours < 17) {
                tvAM_PM.setText(R.string.afternoon);
            }else if(hours < 7) {
                tvAM_PM.setText(R.string.evening);
            }else{
                tvAM_PM.setText(R.string.night);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private void onAttachmentClick(EventAttachment attachment) {
        if (attachment.getMimeType().startsWith("image/")) {
            Intent intent = new Intent(this, FullScreenImageActivity.class);
            intent.putExtra("imageUrl", attachment.getFileUrl());
            startActivity(intent);
        } else if (attachment.getMimeType().equals("application/pdf")) {
            Uri uri = Uri.parse(convertGoogleDriveUrlToDirect(attachment.getFileUrl()));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e("EventDetailActivity", "Error opening PDF: " + e.getMessage());
                Toast.makeText(this, "Error opening PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Unsupported attachment type", Toast.LENGTH_SHORT).show();
        }
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

    private class LoadEventAttachmentsTask extends AsyncTask<String, Void, List<EventAttachment>> {
        private Exception lastError = null;

        @Override
        protected List<EventAttachment> doInBackground(String... params) {
            String eventId = params[0];
            try {
                Calendar service = new Calendar.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("Your App Name")
                        .build();

                Event event = service.events().get("primary", eventId).execute();
                return event.getAttachments();

            } catch (Exception e) {
                lastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<EventAttachment> attachments) {
            Log.d("EventDetailActivity", "Attachments loaded: " + (attachments == null ? 0 : attachments.size()));
            if (attachments == null || attachments.size() == 0) {
               // Toast.makeText(EventDetailActivity.this, "No attachments found.", Toast.LENGTH_SHORT).show();
                linearLayoutAttchements.setVisibility(View.GONE);
            } else {
                linearLayoutAttchements.setVisibility(View.VISIBLE);
                attachmentsList.clear();
                attachmentsList.addAll(attachments);
                attachmentsAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onCancelled() {
            if (lastError != null) {
                if (lastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Toast.makeText(EventDetailActivity.this, "Google Play Services is not available.", Toast.LENGTH_SHORT).show();
                } else if (lastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(((UserRecoverableAuthIOException) lastError).getIntent(), 1001);
                } else {
                    Toast.makeText(EventDetailActivity.this, "The following error occurred:\n" + lastError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EventDetailActivity.this, "Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
