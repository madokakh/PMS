package com.example.scheduleproject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GoogleAccountCredential credential;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventsList;
    private ProgressBar progressBar;
    private TextView tvAM_PM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  getSupportActionBar().setTitle(R.string.main_acitivity_title );

      /*  // Set custom font for the ActionBar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater inflater = LayoutInflater.from(this);
            TextView customTitleView = (TextView) inflater.inflate(R.layout.custom_action_bar_title, null);
            customTitleView.setText(R.string.main_acitivity_title);
            actionBar.setCustomView(customTitleView);
            actionBar.setDisplayShowCustomEnabled(true);
        }*/
        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*if (getSupportActionBar() != null) {

            getSupportActionBar() .setDisplayShowTitleEnabled(false);
            LayoutInflater inflater = LayoutInflater.from(this);
            TextView customTitleView = (TextView) inflater.inflate(R.layout.custom_action_bar_title, null);
            customTitleView.setText(R.string.main_acitivity_title);
            getSupportActionBar() .setCustomView(customTitleView);
            getSupportActionBar() .setDisplayShowCustomEnabled(true);
        }
*/
        // Center the title
        TextView toolbarTitle = findViewById(R.id.action_bar_title);
        toolbarTitle.setText(R.string.main_acitivity_title);  // Set your title here

        // Hide the default title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsList = new ArrayList<>();
        adapter = new EventAdapter(this, eventsList);
        recyclerView.setAdapter(adapter);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Collections.singleton(com.google.api.services.calendar.CalendarScopes.CALENDAR));
            credential.setSelectedAccount(account.getAccount());
            new LoadCalendarEventsTask().execute();
        } else {
            Toast.makeText(this, "User is not signed in", Toast.LENGTH_SHORT).show();
            // Optionally, navigate back to LoginActivity
        }
    }


    private class LoadCalendarEventsTask extends AsyncTask<Void, Void, List<Event>> {
        private Exception lastError = null;
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Event> doInBackground(Void... voids) {
            try {
                Calendar service = new Calendar.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("Your App Name")
                        .build();

                // Get the start of today
                java.util.Calendar startOfToday = java.util.Calendar.getInstance();
                startOfToday.set(java.util.Calendar.HOUR_OF_DAY, 0);
                startOfToday.set(java.util.Calendar.MINUTE, 0);
                startOfToday.set(java.util.Calendar.SECOND, 0);
                startOfToday.set(java.util.Calendar.MILLISECOND, 0);
                DateTime startDateTime = new DateTime(startOfToday.getTime());

                // Get the end of tomorrow
                java.util.Calendar endOfTomorrow = java.util.Calendar.getInstance();
                endOfTomorrow.add(java.util.Calendar.DAY_OF_MONTH, 1);
                endOfTomorrow.set(java.util.Calendar.HOUR_OF_DAY, 23);
                endOfTomorrow.set(java.util.Calendar.MINUTE, 59);
                endOfTomorrow.set(java.util.Calendar.SECOND, 59);
                endOfTomorrow.set(java.util.Calendar.MILLISECOND, 999);
                DateTime endDateTime = new DateTime(endOfTomorrow.getTime());

                Events events = service.events().list("primary")
                        .setTimeMin(startDateTime)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                return events.getItems();

            } catch (Exception e) {
                lastError = e;
                cancel(true);
                return null;
            }
        }


        @Override
        protected void onPostExecute(List<Event> events) {
            progressBar.setVisibility(View.GONE);
            if (events == null || events.size() == 0) {
                Toast.makeText(MainActivity.this, "No results returned.", Toast.LENGTH_SHORT).show();
            } else {
                eventsList.clear();
                eventsList.addAll(events);
                adapter = new EventAdapter(MainActivity.this, eventsList);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }


        @Override
        protected void onCancelled() {
            progressBar.setVisibility(View.GONE);
            if (lastError != null) {
                if (lastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Toast.makeText(MainActivity.this, "Google Play Services is not available.", Toast.LENGTH_SHORT).show();
                } else if (lastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(((UserRecoverableAuthIOException) lastError).getIntent(), 1001);
                } else {
                    Toast.makeText(MainActivity.this, "The following error occurred:\n" + lastError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
