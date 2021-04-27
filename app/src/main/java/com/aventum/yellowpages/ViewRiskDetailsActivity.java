package com.aventum.yellowpages;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewRiskDetailsActivity extends Activity {

    Button closeConcernButton, scheduleConcernButton;
    View include;
    String concernKey, refKey, teamPhotoURL, teamName;
    String dept, priority;

    public static final String CONCERN_KEY = "concernKey";
    public static final String REF_KEY = "refKey";
    public static final String PRIORITY = "priority";
    public static final int SAVE_FILE_CODE = 1;

    ConstraintLayout reporterTeamCL;
    TextView concern, location, reportedBy, reporterDept, reporterTeam, handledByDept, time;
    ImageView imageConcern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_risk_details);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        Intent intent = getIntent();
        concernKey = intent.getStringExtra(CONCERN_KEY);
        refKey = intent.getStringExtra(REF_KEY);
        priority = intent.getStringExtra(PRIORITY);

        include = findViewById(R.id.include);
        imageConcern = include.findViewById(R.id.concernPhotoIV);
        reporterTeamCL = include.findViewById(R.id.reporterTeamCL);
        concern = include.findViewById(R.id.concern);
        location = include.findViewById(R.id.location);
        reportedBy = include.findViewById(R.id.reportedBy);
        reporterDept = include.findViewById(R.id.reporterDept);
        reporterTeam = include.findViewById(R.id.reporterTeam);
        handledByDept = include.findViewById(R.id.handledByDept);
        time = include.findViewById(R.id.time);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.loading_risks_alert_layout, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        reporterTeamCL.setVisibility(View.GONE);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.allConcerns)).child(concernKey);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type = snapshot.child("type").getValue().toString();
                if(type.equals("0")) {
                    reporterTeamCL.setVisibility(View.GONE);
                    SafetyConcernClass s = snapshot.getValue(SafetyConcernClass.class);
                    concern.setText(s.getConcern());
                    location.setText(s.getLoc());
                    reportedBy.setText(s.getRepBy());
                    reporterDept.setText(s.getRepByDept());
                    dept = s.getDeptTo();
                    handledByDept.setText(dept);

                    Object repTime = s.getRepTime();
                    time.setText(getDateAndTime(repTime));

                    Picasso.get().load(s.getcPhotoURL()).into(imageConcern, new Callback() {
                        @Override
                        public void onSuccess() {
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
                else {
                    reporterTeamCL.setVisibility(View.VISIBLE);
                    GembaWalkClass g = snapshot.getValue(GembaWalkClass.class);
                    concern.setText(g.getConcern());
                    location.setText(g.getLoc());
                    reportedBy.setText(g.getRepBy());
                    reporterDept.setText(g.getRepByDept());
                    dept = g.getDeptTo();
                    handledByDept.setText(dept);

                    Object repTime = g.getRepTime();
                    time.setText(getDateAndTime(repTime));

                    teamName = g.getTeamName();
                    reporterTeam.setText(teamName);
                    teamPhotoURL = g.gettPhotoURL();
                    Picasso.get().load(g.getcPhotoURL()).into(imageConcern, new Callback() {
                        @Override
                        public void onSuccess() {
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        include = findViewById(R.id.include);
        closeConcernButton = include.findViewById(R.id.closeConcernButton);
        scheduleConcernButton = include.findViewById(R.id.scheduleButton);
    }

    private String getDateAndTime(Object time){
        Date date = new Date((Long)time);
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfDate.format(date) + " at " + sdfTime.format(date);
    }

    public void viewTeamPhoto(View view){
        Intent intent = new Intent(getApplicationContext(), TeamPhotoActivity.class);
        intent.putExtra(TeamPhotoActivity.URL, teamPhotoURL);
        intent.putExtra(TeamPhotoActivity.TEAM_NAME, teamName);
        startActivity(intent);
    }

    public void closeConcern(View view){
        Intent intent = new Intent(getApplicationContext(), CloseConcernActivity.class);
        intent.putExtra(CloseConcernActivity.CONCERN_KEY, concernKey);
        intent.putExtra(CloseConcernActivity.REF_KEY, refKey);
        intent.putExtra(CloseConcernActivity.PRIORITY, priority);
        intent.putExtra(CloseConcernActivity.DEPT, dept);
        startActivity(intent);
    }

    public void scheduleConcern(View view){
        Intent intent = new Intent(getApplicationContext(), ScheduleConcernActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        finish();
    }

    public void saveImageToGallery1(View view){
        new AlertDialog.Builder(ViewRiskDetailsActivity.this)
                .setTitle("Save image to device")
                .setMessage("Save this image to device for viewing it outside of app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Context context = ViewRiskDetailsActivity.this;

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View alertView = LayoutInflater.from(context).inflate(R.layout.saving_image_dialog, null);
                        builder.setView(alertView);
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        /*SaveImage saveImage = new SaveImage(ViewRiskDetailsActivity.this);
                        try {
                            boolean done = saveImage.loadInBackground();*/


                        /*SaveImage saveImage = new SaveImage();
                        boolean done = false;
                        try {
                            done = saveImage.executeOnExecutor(Executors.newScheduledThreadPool(1)).get();

                            if(done){
                                Toast.makeText(getApplicationContext(), "Image successfully stored!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error while saving image!", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }*/

                        BitmapDrawable drawable = (BitmapDrawable) imageConcern.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();

                        try {
//                            File file = new File(getApplicationContext().getExternalFilesDir(null).toString());
                            File file = new File(getApplicationContext().getExternalFilesDir(null).toString());
//                            File file = new File();
                            File myDir = new File(file, "Open Concern Images");
                            if (!myDir.exists())
                                myDir.mkdirs();

                            String name = concernKey + ".png";
                            myDir = new File(myDir, name);
                            FileOutputStream outputStream = new FileOutputStream(myDir);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.flush();
                            outputStream.close();
                            Log.i("File save", "Successful");

                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Image successfully stored!", Toast.LENGTH_SHORT).show();

                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error while saving image!", Toast.LENGTH_SHORT).show();
                        }
                        /*Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(intent, SAVE_FILE_CODE);*/

                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void saveImageToGallery(View view){

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.image_select_options_dialog, null);

        final AlertDialog dialog = new AlertDialog.Builder(ViewRiskDetailsActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ConstraintLayout saveImageCL = dialogView.findViewById(R.id.saveImageCL);
        saveImageCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                SaveImage saveImage = new SaveImage();
                saveImage.execute();
            }
        });
    }


    private class SaveImage extends AsyncTask<Void, Void, String> {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(ViewRiskDetailsActivity.this);
        AlertDialog dialog1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            View view1 = LayoutInflater.from(ViewRiskDetailsActivity.this).inflate(R.layout.saving_image_dialog, null);
            builder1.setView(view1);
            dialog1 = builder1.create();
            dialog1.setCancelable(false);
            dialog1.setCanceledOnTouchOutside(false);
            dialog1.show();
        }

        @Override
        protected String doInBackground(Void... voids) {

            BitmapDrawable drawable = (BitmapDrawable) imageConcern.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            OutputStream fos;
            File imageFile = null;
            Uri imageUri = null;

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = ViewRiskDetailsActivity.this.getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, concernKey);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Concerns");
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
                    imageUri = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);
                    fos = resolver.openOutputStream(imageUri);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();

                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(imageUri, contentValues, null, null);
                } else {
//                    File file = new File(getApplicationContext().getExternalFilesDir(null).toString());
//                File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());

                    imageFile = new File(getApplicationContext().getExternalFilesDir(null).toString());
                    File myDir = new File(imageFile, "Open Concern Images");
                    if (!myDir.exists())
                        myDir.mkdirs();

                    String name = concernKey + ".png";
                    myDir = new File(myDir, name);

                    if (myDir.exists()) {
                        return "exists";
                    }

                    /*FileOutputStream outputStream = new FileOutputStream(myDir);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();*/
                    fos = new FileOutputStream(myDir);
                    Log.i("File save", "Successful");

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();

//                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile.getPath()}, new String[]{"image/jpeg"}, null);

                }

//                EXTERNAL_CONTENT_URI

            } catch (Exception e) {
                e.printStackTrace();

            }
            return "saved";
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            dialog1.dismiss();
            if(string.equals("saved"))
                Toast.makeText(ViewRiskDetailsActivity.this, "Image saved to device!", Toast.LENGTH_SHORT).show();
            else if(string.equals("exists"))
                Toast.makeText(ViewRiskDetailsActivity.this, "Image already saved!", Toast.LENGTH_SHORT).show();
        }
    }

}