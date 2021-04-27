package com.aventum.yellowpages;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportGenerationActivity extends AppCompatActivity {

    Context context;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    SharedPreferences preferences;

    ConstraintLayout generateReportCL;
    ConstraintLayout grActiveCL, grClosedCL;
    TextView aDateTimeTV, cDateTimeTV;
    View rootView;

    String userDept;
    private static final String DEPT_TO_MAKE_REPORT = "Security";
    public static final Long ASPAN = 3600000L; //Cooldown time between report generation for active concerns
    public static final Long CSPAN = 3600000L; //Cooldown time between report generation for closed concerns

    private static String activeConcernsGenerateReportURL = "http://ec2-65-1-2-137.ap-south-1.compute.amazonaws.com:5000/createExcel";
    private static String closedConcernsGenerateReportURL = "http://ec2-65-1-2-137.ap-south-1.compute.amazonaws.com:5000/createExcel";
    private static String activeConcernsReportDownloadURL = "http://ec2-65-1-2-137.ap-south-1.compute.amazonaws.com:5000/api/downloadfile/Report.zip";
    private static String closedConcernsReportDownloadURL = "http://ec2-65-1-2-137.ap-south-1.compute.amazonaws.com:5000/api/downloadfile/Report.zip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_generation);

        generateReportCL = findViewById(R.id.generateReportCL);
        grActiveCL = findViewById(R.id.grActiveConcerns);
        grClosedCL = findViewById(R.id.grClosedConcerns);
        rootView = findViewById(R.id.rootViewRGA);
        aDateTimeTV = findViewById(R.id.activeConcernsReportDT);
        cDateTimeTV = findViewById(R.id.closedConcernsReportDT);

        context = ReportGenerationActivity.this;
        preferences = getSharedPreferences("com.example.yellowpages", MODE_PRIVATE);

        boolean show = preferences.getBoolean(getResources().getString(R.string.dontShowReportDialog), false);
//        boolean show = false;

        userDept = preferences.getString(getResources().getString(R.string.userDept), "");

        if(userDept.equals(DEPT_TO_MAKE_REPORT))
            generateReportCL.setVisibility(View.VISIBLE);

        if(!show){
            builder = new AlertDialog.Builder(context);
            View alertView = LayoutInflater.from(context).inflate(R.layout.report_feature_info_dialog, null);
            Button closeButton = alertView.findViewById(R.id.closeDialog);
            closeButton.setOnClickListener(closeListener);
            CheckBox box = alertView.findViewById(R.id.dontShowAgain);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(box.isChecked()){
                        preferences.edit().putBoolean(getResources().getString(R.string.dontShowReportDialog), true).apply();
                    }
                    else {
                        preferences.edit().putBoolean(getResources().getString(R.string.dontShowReportDialog), false).apply();
                    }
                }
            });
            builder.setView(alertView);
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        else {
            loadLastGenerationDates();
        }


    }

    public void closeRGA(View view){
        finish();
    }

    public void viewInfo(View view){
        builder = new AlertDialog.Builder(context);
        View alertView = LayoutInflater.from(context).inflate(R.layout.report_timespan_info, null);
        builder.setView(alertView);
        dialog = builder.create();
        dialog.show();
    }


    private void loadLastGenerationDates(){

        builder = new AlertDialog.Builder(context);
        View alertView = LayoutInflater.from(context).inflate(R.layout.loading_risks_alert_layout, null);
        builder.setView(alertView);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        FirebaseDatabase.getInstance().getReference()
                .child(getResources().getString(R.string.reportGenerationRecords))
                .child(getResources().getString(R.string.activeConcernsReportTimestamp))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Long aTimestamp = snapshot.getValue(Long.class);

                        FirebaseDatabase.getInstance().getReference()
                                .child(getResources().getString(R.string.reportGenerationRecords))
                                .child(getResources().getString(R.string.closedConcernsReportTimestamp))
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String aTime = getDate(aTimestamp) + " at " + getTime(aTimestamp);
                                        aDateTimeTV.setText(aTime);

                                        Long cTimestamp = snapshot.getValue(Long.class);
                                        String cTime = getDate(cTimestamp) + " at " + getTime(cTimestamp);
                                        cDateTimeTV.setText(cTime);

                                        Long current = System.currentTimeMillis();

                                        Log.i("Diff", String.valueOf(current - aTimestamp));

                                        if((current - aTimestamp < ASPAN) && (current - cTimestamp < CSPAN))
                                            generateReportCL.setVisibility(View.GONE);
                                        else if(current - aTimestamp < ASPAN)
                                            grActiveCL.setVisibility(View.GONE);
                                        else if (current - cTimestamp < CSPAN)
                                            grClosedCL.setVisibility(View.GONE);
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void generateActiveConcernsReport(View view){
        /*GenerateActiveConcernsReport url = new GenerateActiveConcernsReport();
        url.execute();*/
        Toast.makeText(getApplicationContext(), "This feature will be added shortly!", Toast.LENGTH_SHORT).show();
    }

    public void generateClosedConcernsReport(View view){
        GenerateClosedConcernsReport url = new GenerateClosedConcernsReport();
        url.execute();
    }

    public void downloadActiveConcernsReport(View view){
//        downloadReportUtil(activeConcernsReportDownloadURL);
        Toast.makeText(getApplicationContext(), "This feature will be added shortly!", Toast.LENGTH_SHORT).show();
    }

    public void downloadClosedConcernsReport(View view){
        downloadReportUtil(closedConcernsReportDownloadURL);
    }

    class GenerateActiveConcernsReport extends AsyncTask<Void, String, Boolean> {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.sending_report_request, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView);

        AlertDialog dialog = builder.create();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            Snackbar.make(rootView, "Sending report generation request...", Snackbar.LENGTH_LONG).show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            int count;
            try {
                URL url = new URL(activeConcernsGenerateReportURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

//                Log.i("Header", connection.getHeaderFields().toString());
                int responseCode = connection.getResponseCode();

                return responseCode == 200;


            } catch (Exception e) {
                Log.e("Error: ", e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean){
                dialog.dismiss();
                FirebaseDatabase.getInstance().getReference()
                        .child(getResources().getString(R.string.reportGenerationRecords))
                        .child(getResources().getString(R.string.activeConcernsReportTimestamp))
                        .setValue(ServerValue.TIMESTAMP);

                /*FirebaseDatabase.getInstance().getReference()
                        .child(getResources().getString(R.string.reportLastGeneratedTimestamp))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Long timestamp = snapshot.getValue(Long.class);
                                String time = String.valueOf(timestamp);
                                preferences.edit().putString(getResources().getString(R.string.reportLastGeneratedTimestamp), time).apply();
                                Log.i("Last time", preferences.getString(getResources().getString(R.string.reportLastGeneratedTimestamp), ""));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });*/
                downloadReportUtil(activeConcernsReportDownloadURL);
            }
            else {
                Snackbar.make(rootView, "Request could not be completed. Please try again later!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    class GenerateClosedConcernsReport extends AsyncTask<Void, String, Boolean> {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.sending_report_request, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView);

        AlertDialog dialog = builder.create();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            Snackbar.make(rootView, "Sending report generation request...", Snackbar.LENGTH_LONG).show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            int count;
            try {
                URL url = new URL(closedConcernsGenerateReportURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

//                Log.i("Header", connection.getHeaderFields().toString());
                int responseCode = connection.getResponseCode();

                return responseCode == 200;


            } catch (Exception e) {
                Log.e("Error: ", e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean){
                dialog.dismiss();
                FirebaseDatabase.getInstance().getReference()
                        .child(getResources().getString(R.string.reportGenerationRecords))
                        .child(getResources().getString(R.string.closedConcernsReportTimestamp))
                        .setValue(ServerValue.TIMESTAMP);

                /*FirebaseDatabase.getInstance().getReference()
                        .child(getResources().getString(R.string.reportLastGeneratedTimestamp))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Long timestamp = snapshot.getValue(Long.class);
                                String time = String.valueOf(timestamp);
                                preferences.edit().putString(getResources().getString(R.string.reportLastGeneratedTimestamp), time).apply();
                                Log.i("Last time", preferences.getString(getResources().getString(R.string.reportLastGeneratedTimestamp), ""));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });*/
                downloadReportUtil(activeConcernsReportDownloadURL);
            }
            else {
                Snackbar.make(rootView, "Request could not be completed. Please try again later!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void downloadReportUtil(String url){
        Snackbar.make(rootView, "Request completed. Downloading report...", Snackbar.LENGTH_LONG).show();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Report download in progress");
        request.setTitle("Report");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Report.zip");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private String getDate(Object time) {
        Date date = new Date((Long) time);
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdfDate.format(date);
    }

    private String getTime(Object time) {
        Date date = new Date((Long) time);
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(date);
    }

    private String getTimeTaken(Object t1, Object t2) {
        long diff = (Long) t1 - (Long) t2;
        return String.valueOf(diff);
    }

    View.OnClickListener closeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();
            loadLastGenerationDates();
        }
    };
}