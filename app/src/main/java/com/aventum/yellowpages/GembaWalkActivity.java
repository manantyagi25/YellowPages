package com.aventum.yellowpages;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.aventum.yellowpages.MainActivity.MAX_SIZE;
import static com.aventum.yellowpages.MainActivity.userDept;
import static com.aventum.yellowpages.MainActivity.userName;

public class GembaWalkActivity extends AppCompatActivity {

    public static String type = "1";
    EditText concernET, locationET;
    Spinner deptSpinner, teamSpinner;
    ImageView teamPic, concernPic;
    RadioGroup radioGroup;
    View include;
    ImageButton teamImageDeleteButton, concernImageDeleteButton;
    TextView teamImageUploadTV, concernImageUploadTV;

    String issueImageURL, teamImageURL;

    private final int TEAM_PICK_IMAGE_REQUEST = 1;
    private final int TEAM_CAPTURE_IMAGE_REQUEST = 2;
    private final int ISSUE_PICK_IMAGE_REQUEST = 3;
    private final int ISSUE_CAPTURE_IMAGE_REQUEST = 4;
    private final String CONCERN_PHOTO = "concern", TEAM_PHOTO = "team";
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.aventum.yellowpages.fileprovider";
    private final String SECURITY = "Security";
    private Uri filePathTeam, filePathIssue, teamURIFromServer;
    private boolean fromCameraTeam = false, fromCameraIssue = false, teamPhotoFromServer = false;

    AlertDialog.Builder builder;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemba_walk);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        include = findViewById(R.id.include);
        concernET = include.findViewById(R.id.safetyConcernET);
        locationET = include.findViewById(R.id.locationET);
        deptSpinner = include.findViewById(R.id.departmentSpinner);
        teamSpinner = include.findViewById(R.id.teamNameSpinner);
        teamPic = include.findViewById(R.id.uploadTeamImage);
        concernPic = include.findViewById(R.id.uploadImage);
        radioGroup = include.findViewById(R.id.priorityRG);
        teamImageDeleteButton = include.findViewById(R.id.teamImageDeleteButton);
        concernImageDeleteButton = include.findViewById(R.id.concernImageDeleteButton);
        teamImageUploadTV = include.findViewById(R.id.uploadTeamPhotoTV);
        concernImageUploadTV = include.findViewById(R.id.uploadConcernPhotoTV);

        concernET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        concernET.setRawInputType(InputType.TYPE_CLASS_TEXT);

        locationET.setImeOptions(EditorInfo.IME_ACTION_DONE);
        locationET.setRawInputType(InputType.TYPE_CLASS_TEXT);

        teamSpinner.setOnItemSelectedListener(spinnerListener);
    }

    public void cancelGWA(View view){
        finish();
    }

    public void submitGembaWalk(View view){

        if(concernET.getText().length() > 0 && locationET.getText().length() > 0 && filePathIssue != null && (filePathTeam != null || teamPhotoFromServer)) {

            builder = new AlertDialog.Builder(this);
            View alertView = LayoutInflater.from(this).inflate(R.layout.uploading, null);
            builder.setView(alertView);
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            uploadToStorage();
        }
        else {
            //Toast.makeText(getApplicationContext(), "One or more necessary fields are empty", Toast.LENGTH_SHORT).show();
            Snackbar snackbar = Snackbar.make(view, "Please fill all the fields", BaseTransientBottomBar.LENGTH_LONG);
            snackbar.show();
        }
    }

    public void uploadTeamPhoto(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.choose_or_take_image_alert_layout, null);
        //builder.setView(dialogView);

        final AlertDialog dialog = new AlertDialog.Builder(GembaWalkActivity.this)
                .setView(dialogView)
                .show();

        ConstraintLayout fromGalleryCL, fromCameraCL;
        fromGalleryCL = dialogView.findViewById(R.id.fromGalleryCL);
        fromCameraCL = dialogView.findViewById(R.id.fromCameraCL);

        fromGalleryCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseTeamImage();
                dialog.dismiss();
            }
        });

        fromCameraCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeTeamImage();
                dialog.dismiss();
            }
        });
    }

    public void uploadIssuePhoto(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.choose_or_take_image_alert_layout, null);
        //builder.setView(dialogView);

        final AlertDialog dialog = new AlertDialog.Builder(GembaWalkActivity.this)
                .setView(dialogView)
                .show();

        ConstraintLayout fromGalleryCL, fromCameraCL;
        fromGalleryCL = dialogView.findViewById(R.id.fromGalleryCL);
        fromCameraCL = dialogView.findViewById(R.id.fromCameraCL);

        fromGalleryCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseIssueImage();
                dialog.dismiss();
            }
        });

        fromCameraCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeIssueImage();
                dialog.dismiss();
            }
        });
    }

    public void chooseTeamImage() {
        ActivityCompat.requestPermissions(GembaWalkActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},TEAM_PICK_IMAGE_REQUEST);
    }

    private void takeTeamImage(){
        ActivityCompat.requestPermissions(GembaWalkActivity.this, new String[]{Manifest.permission.CAMERA}, TEAM_CAPTURE_IMAGE_REQUEST);
    }

    public void chooseIssueImage() {
        ActivityCompat.requestPermissions(GembaWalkActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},ISSUE_PICK_IMAGE_REQUEST);
    }

    private void takeIssueImage(){
        ActivityCompat.requestPermissions(GembaWalkActivity.this, new String[]{Manifest.permission.CAMERA}, ISSUE_CAPTURE_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case TEAM_PICK_IMAGE_REQUEST:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), TEAM_PICK_IMAGE_REQUEST);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Storage access permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;

            case TEAM_CAPTURE_IMAGE_REQUEST:
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    File path = new File(getFilesDir(), "newTeamPhoto");
                    if (!path.exists())
                        path.mkdirs();

                    File image = new File(path, "teamImage.jpg");
                    filePathTeam = FileProvider.getUriForFile(GembaWalkActivity.this, CAPTURE_IMAGE_FILE_PROVIDER, image);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, filePathTeam);
                    startActivityForResult(intent, TEAM_CAPTURE_IMAGE_REQUEST);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Camera access permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;

            case ISSUE_PICK_IMAGE_REQUEST:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), ISSUE_PICK_IMAGE_REQUEST);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Storage access permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;

            case ISSUE_CAPTURE_IMAGE_REQUEST:
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    File path = new File(getFilesDir(), "newIssuePhoto");
                    if (!path.exists())
                        path.mkdirs();

                    File image = new File(path, "issueImage.jpg");
                    filePathIssue = FileProvider.getUriForFile(GembaWalkActivity.this, CAPTURE_IMAGE_FILE_PROVIDER, image);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, filePathIssue);
                    startActivityForResult(intent, ISSUE_CAPTURE_IMAGE_REQUEST);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Camera access permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    public void uploadToStorage() {
        /*final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image...");
        progressDialog.show();*/

        final DatabaseReference allConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.allConcerns)).push();
        final String caseKey = allConcernsReference.getKey();

        //If photo is from server, just put that reference into concern data
        if(teamPhotoFromServer){
            try {
                Bitmap bitmapIssue = null;
                byte[] issuePicCompressed;

                bitmapIssue = MediaStore.Images.Media.getBitmap(GembaWalkActivity.this.getContentResolver(), filePathIssue);
                ByteArrayOutputStream streamIssue = new ByteArrayOutputStream();
                bitmapIssue.compress(Bitmap.CompressFormat.JPEG, 50, streamIssue);
                issuePicCompressed = streamIssue.toByteArray();

                final StorageReference ref = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns)).child(caseKey).child(CONCERN_PHOTO);
                ref.putBytes(issuePicCompressed)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        //Here code for saving images to device

                                        /*if (fromCameraTeam) {
                                            BitmapDrawable drawable1 = (BitmapDrawable) teamPic.getDrawable();
                                            Bitmap bitmap1 = drawable1.getBitmap();
                                            OutputStream fos1;
                                            File imageFile1 = null;
                                            Uri imageUri1 = null;

                                            try {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                    ContentResolver resolver1 = GembaWalkActivity.this.getContentResolver();
                                                    ContentValues contentValues1 = new ContentValues();
                                                    contentValues1.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey + TEAM_PHOTO);
                                                    contentValues1.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                                    contentValues1.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Concerns");
                                                    contentValues1.put(MediaStore.Images.Media.IS_PENDING, 1);
                                                    imageUri1 = resolver1.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues1);
                                                    fos1 = resolver1.openOutputStream(imageUri1);

                                                    bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos1);
                                                    fos1.flush();
                                                    fos1.close();

                                                    contentValues1.put(MediaStore.Images.Media.IS_PENDING, 0);
                                                    resolver1.update(imageUri1, contentValues1, null, null);
                                                } else {

                                                    imageFile1 = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                    File myDir1 = new File(imageFile1, "Open Concern Images");
                                                    if (!myDir1.exists())
                                                        myDir1.mkdirs();

                                                    String name1 = caseKey + TEAM_PHOTO + ".png";
                                                    myDir1 = new File(myDir1, name1);

                                                    fos1 = new FileOutputStream(myDir1);
                                                    Log.i("File save", "Successful");

                                                    bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos1);
                                                    fos1.flush();
                                                    fos1.close();

                                                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile1.getPath()}, new String[]{"image/jpeg"}, null);
                                                }
//                                                              EXTERNAL_CONTENT_URI

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }*/

                                        if (fromCameraIssue) {
                                            BitmapDrawable drawable2 = (BitmapDrawable) concernPic.getDrawable();
                                            Bitmap bitmap2 = drawable2.getBitmap();
                                            OutputStream fos2;
                                            File imageFile2 = null;
                                            Uri imageUri2 = null;

                                            try {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                                                    ContentResolver resolver2 = GembaWalkActivity.this.getContentResolver();
                                                    ContentValues contentValues2 = new ContentValues();
                                                    contentValues2.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey + CONCERN_PHOTO);
                                                    contentValues2.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                                    contentValues2.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Concerns");
                                                    contentValues2.put(MediaStore.Images.Media.IS_PENDING, 1);
                                                    imageUri2 = resolver2.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues2);
                                                    fos2 = resolver2.openOutputStream(imageUri2);

                                                    bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                                                    fos2.flush();
                                                    fos2.close();

                                                    contentValues2.put(MediaStore.Images.Media.IS_PENDING, 0);
                                                    resolver2.update(imageUri2, contentValues2, null, null);
                                                } else {

                                                    imageFile2 = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                    File myDir2 = new File(imageFile2, "Open Concern Images");
                                                    if (!myDir2.exists())
                                                        myDir2.mkdirs();

                                                    String name2 = caseKey + CONCERN_PHOTO + ".png";
                                                    myDir2 = new File(myDir2, name2);

                                                    fos2 = new FileOutputStream(myDir2);
                                                    Log.i("File save", "Successful");

                                                    bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                                                    fos2.flush();
                                                    fos2.close();

                                                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile2.getPath()}, new String[]{"image/jpeg"}, null);

                                                }

//                                                              EXTERNAL_CONTENT_URI

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        issueImageURL = uri.toString();
                                        int selected = radioGroup.getCheckedRadioButtonId();
                                        RadioButton button = include.findViewById(selected);
                                        String concern = concernET.getText().toString().replace("\\n", "");
                                        String location = locationET.getText().toString().replace("\\n", "");
                                        String priority = button.getText().toString();
                                        String selectedDept = deptSpinner.getSelectedItem().toString();
                                        String teamName = teamSpinner.getSelectedItem().toString();
                                        GembaWalkClass g = new GembaWalkClass(type, userName, userDept, teamName, concern, location, selectedDept, issueImageURL, teamURIFromServer.toString(), priority);
                                        allConcernsReference.setValue(g);

                                        DatabaseReference activeConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.concerns))
                                                .child(getResources().getString(R.string.active)).child(priority).push();
                                        String activeCaseKey = activeConcernsReference.getKey();
                                        activeConcernsReference.setValue(caseKey);

                                        DatabaseReference securityDeptReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns)).child(SECURITY).child(priority).child(activeCaseKey);

                                        securityDeptReference.setValue(caseKey);

                                        DatabaseReference deptWiseConcernReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns)).child(selectedDept).child(priority).child(activeCaseKey);

                                        deptWiseConcernReference.setValue(caseKey);
                                    }
                                });
                                //progressDialog.dismiss();
                                dialog.dismiss();
                                finish();
                                Toast.makeText(getApplicationContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

        //If photo is not on server, upload the photo first and then put that reference into concern data
        else {
            Bitmap bitmapTeam = null;
            byte[] teamPicCompressed;
            try {
                bitmapTeam = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePathTeam);
                ByteArrayOutputStream streamTeam = new ByteArrayOutputStream();
                bitmapTeam.compress(Bitmap.CompressFormat.JPEG, 50, streamTeam);
                teamPicCompressed = streamTeam.toByteArray();

                String teamName = teamSpinner.getSelectedItem().toString();

                /*final StorageReference teamRef = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns)).child(caseKey).child(TEAM_PHOTO);
                teamRef.putBytes(teamPicCompressed)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                teamRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        teamImageURL = uri.toString();

                                        Bitmap bitmapIssue = null;
                                        byte[] issuePicCompressed;
                                        try {
                                            bitmapIssue = MediaStore.Images.Media.getBitmap(GembaWalkActivity.this.getContentResolver(), filePathIssue);
                                            ByteArrayOutputStream streamIssue = new ByteArrayOutputStream();
                                            bitmapIssue.compress(Bitmap.CompressFormat.JPEG, 50, streamIssue);
                                            issuePicCompressed = streamIssue.toByteArray();

                                            final StorageReference ref = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns)).child(caseKey).child(CONCERN_PHOTO);
                                            ref.putBytes(issuePicCompressed)
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {

                                                                    //Code for saving images to device

                                                                    if (fromCameraTeam) {
                                                                        BitmapDrawable drawable1 = (BitmapDrawable) teamPic.getDrawable();
                                                                        Bitmap bitmap1 = drawable1.getBitmap();
                                                                        OutputStream fos1;
                                                                        File imageFile1 = null;
                                                                        Uri imageUri1 = null;

                                                                        try {
                                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                                                ContentResolver resolver1 = GembaWalkActivity.this.getContentResolver();
                                                                                ContentValues contentValues1 = new ContentValues();
                                                                                contentValues1.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey + TEAM_PHOTO);
                                                                                contentValues1.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                                                                contentValues1.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Concerns");
                                                                                contentValues1.put(MediaStore.Images.Media.IS_PENDING, 1);
                                                                                imageUri1 = resolver1.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues1);
                                                                                fos1 = resolver1.openOutputStream(imageUri1);

                                                                                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos1);
                                                                                fos1.flush();
                                                                                fos1.close();

                                                                                contentValues1.put(MediaStore.Images.Media.IS_PENDING, 0);
                                                                                resolver1.update(imageUri1, contentValues1, null, null);
                                                                            } else {

                                                                                imageFile1 = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                                                File myDir1 = new File(imageFile1, "Open Concern Images");
                                                                                if (!myDir1.exists())
                                                                                    myDir1.mkdirs();

                                                                                String name1 = caseKey + TEAM_PHOTO + ".png";
                                                                                myDir1 = new File(myDir1, name1);

                                                                                fos1 = new FileOutputStream(myDir1);
                                                                                Log.i("File save", "Successful");

                                                                                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos1);
                                                                                fos1.flush();
                                                                                fos1.close();

                                                                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile1.getPath()}, new String[]{"image/jpeg"}, null);
                                                                            }
//                                                              EXTERNAL_CONTENT_URI

                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }

                                                                    if (fromCameraIssue) {
                                                                        BitmapDrawable drawable2 = (BitmapDrawable) concernPic.getDrawable();
                                                                        Bitmap bitmap2 = drawable2.getBitmap();
                                                                        OutputStream fos2;
                                                                        File imageFile2 = null;
                                                                        Uri imageUri2 = null;

                                                                        try {
                                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                                                                                ContentResolver resolver2 = GembaWalkActivity.this.getContentResolver();
                                                                                ContentValues contentValues2 = new ContentValues();
                                                                                contentValues2.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey + CONCERN_PHOTO);
                                                                                contentValues2.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                                                                contentValues2.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Concerns");
                                                                                contentValues2.put(MediaStore.Images.Media.IS_PENDING, 1);
                                                                                imageUri2 = resolver2.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues2);
                                                                                fos2 = resolver2.openOutputStream(imageUri2);

                                                                                bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                                                                                fos2.flush();
                                                                                fos2.close();

                                                                                contentValues2.put(MediaStore.Images.Media.IS_PENDING, 0);
                                                                                resolver2.update(imageUri2, contentValues2, null, null);
                                                                            } else {

                                                                                imageFile2 = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                                                File myDir2 = new File(imageFile2, "Open Concern Images");
                                                                                if (!myDir2.exists())
                                                                                    myDir2.mkdirs();

                                                                                String name2 = caseKey + CONCERN_PHOTO + ".png";
                                                                                myDir2 = new File(myDir2, name2);

                                                                                fos2 = new FileOutputStream(myDir2);
                                                                                Log.i("File save", "Successful");

                                                                                bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                                                                                fos2.flush();
                                                                                fos2.close();

                                                                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile2.getPath()}, new String[]{"image/jpeg"}, null);

                                                                            }

//                                                              EXTERNAL_CONTENT_URI

                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }

                                                                    issueImageURL = uri.toString();
                                                                    int selected = radioGroup.getCheckedRadioButtonId();
                                                                    RadioButton button = include.findViewById(selected);
                                                                    String concern = concernET.getText().toString().replace("\\n", "");
                                                                    String location = locationET.getText().toString().replace("\\n", "");
                                                                    String priority = button.getText().toString();
                                                                    String selectedDept = deptSpinner.getSelectedItem().toString();
                                                                    String teamName = teamSpinner.getSelectedItem().toString();
                                                                    GembaWalkClass g = new GembaWalkClass(type, userName, userDept, teamName, concern, location, selectedDept, issueImageURL, teamImageURL, priority);
                                                                    allConcernsReference.setValue(g);

                                                                    DatabaseReference activeConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.concerns))
                                                                            .child(getResources().getString(R.string.active)).child(priority).push();
                                                                    String activeCaseKey = activeConcernsReference.getKey();
                                                                    activeConcernsReference.setValue(caseKey);

                                                                    DatabaseReference securityDeptReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns)).child(SECURITY).child(priority).child(activeCaseKey);
                                                                    ;
                                                                    securityDeptReference.setValue(caseKey);

                                                                    DatabaseReference deptWiseConcernReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns)).child(selectedDept).child(priority).child(activeCaseKey);
                                                                    ;
                                                                    deptWiseConcernReference.setValue(caseKey);
                                                                }
                                                            });
                                                            //progressDialog.dismiss();
                                                            dialog.dismiss();
                                                            finish();
                                                            Toast.makeText(getApplicationContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            dialog.dismiss();
                                                            Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                        catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });*/

                final StorageReference teamRef = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.teamPhotos)).child(teamName);
                teamRef.putBytes(teamPicCompressed)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                teamRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        teamImageURL = uri.toString();

                                        Bitmap bitmapIssue = null;
                                        byte[] issuePicCompressed;
                                        try {
                                            bitmapIssue = MediaStore.Images.Media.getBitmap(GembaWalkActivity.this.getContentResolver(), filePathIssue);
                                            ByteArrayOutputStream streamIssue = new ByteArrayOutputStream();
                                            bitmapIssue.compress(Bitmap.CompressFormat.JPEG, 50, streamIssue);
                                            issuePicCompressed = streamIssue.toByteArray();

                                            final StorageReference ref = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns)).child(caseKey).child(CONCERN_PHOTO);
                                            ref.putBytes(issuePicCompressed)
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {

                                                                    //Code for saving images to device

                                                                    if (fromCameraTeam) {
                                                                        BitmapDrawable drawable1 = (BitmapDrawable) teamPic.getDrawable();
                                                                        Bitmap bitmap1 = drawable1.getBitmap();
                                                                        OutputStream fos1;
                                                                        File imageFile1 = null;
                                                                        Uri imageUri1 = null;

                                                                        try {
                                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                                                ContentResolver resolver1 = GembaWalkActivity.this.getContentResolver();
                                                                                ContentValues contentValues1 = new ContentValues();
                                                                                contentValues1.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey + TEAM_PHOTO);
                                                                                contentValues1.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                                                                contentValues1.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Concerns");
                                                                                contentValues1.put(MediaStore.Images.Media.IS_PENDING, 1);
                                                                                imageUri1 = resolver1.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues1);
                                                                                fos1 = resolver1.openOutputStream(imageUri1);

                                                                                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos1);
                                                                                fos1.flush();
                                                                                fos1.close();

                                                                                contentValues1.put(MediaStore.Images.Media.IS_PENDING, 0);
                                                                                resolver1.update(imageUri1, contentValues1, null, null);
                                                                            } else {

                                                                                imageFile1 = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                                                File myDir1 = new File(imageFile1, "Open Concern Images");
                                                                                if (!myDir1.exists())
                                                                                    myDir1.mkdirs();

                                                                                String name1 = caseKey + TEAM_PHOTO + ".png";
                                                                                myDir1 = new File(myDir1, name1);

                                                                                fos1 = new FileOutputStream(myDir1);
                                                                                Log.i("File save", "Successful");

                                                                                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos1);
                                                                                fos1.flush();
                                                                                fos1.close();

                                                                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile1.getPath()}, new String[]{"image/jpeg"}, null);
                                                                            }
                                                                            //                                                              EXTERNAL_CONTENT_URI

                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }

                                                                    if (fromCameraIssue) {
                                                                        BitmapDrawable drawable2 = (BitmapDrawable) concernPic.getDrawable();
                                                                        Bitmap bitmap2 = drawable2.getBitmap();
                                                                        OutputStream fos2;
                                                                        File imageFile2 = null;
                                                                        Uri imageUri2 = null;

                                                                        try {
                                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                                                                                ContentResolver resolver2 = GembaWalkActivity.this.getContentResolver();
                                                                                ContentValues contentValues2 = new ContentValues();
                                                                                contentValues2.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey + CONCERN_PHOTO);
                                                                                contentValues2.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                                                                contentValues2.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Concerns");
                                                                                contentValues2.put(MediaStore.Images.Media.IS_PENDING, 1);
                                                                                imageUri2 = resolver2.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues2);
                                                                                fos2 = resolver2.openOutputStream(imageUri2);

                                                                                bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                                                                                fos2.flush();
                                                                                fos2.close();

                                                                                contentValues2.put(MediaStore.Images.Media.IS_PENDING, 0);
                                                                                resolver2.update(imageUri2, contentValues2, null, null);
                                                                            } else {

                                                                                imageFile2 = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                                                File myDir2 = new File(imageFile2, "Open Concern Images");
                                                                                if (!myDir2.exists())
                                                                                    myDir2.mkdirs();

                                                                                String name2 = caseKey + CONCERN_PHOTO + ".png";
                                                                                myDir2 = new File(myDir2, name2);

                                                                                fos2 = new FileOutputStream(myDir2);
                                                                                Log.i("File save", "Successful");

                                                                                bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                                                                                fos2.flush();
                                                                                fos2.close();

                                                                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile2.getPath()}, new String[]{"image/jpeg"}, null);

                                                                            }

                                                                            //                                                              EXTERNAL_CONTENT_URI

                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }

                                                                    issueImageURL = uri.toString();
                                                                    int selected = radioGroup.getCheckedRadioButtonId();
                                                                    RadioButton button = include.findViewById(selected);
                                                                    String concern = concernET.getText().toString().replace("\\n", "");
                                                                    String location = locationET.getText().toString().replace("\\n", "");
                                                                    String priority = button.getText().toString();
                                                                    String selectedDept = deptSpinner.getSelectedItem().toString();
                                                                    GembaWalkClass g = new GembaWalkClass(type, userName, userDept, teamName, concern, location, selectedDept, issueImageURL, teamImageURL, priority);
                                                                    allConcernsReference.setValue(g);

                                                                    DatabaseReference activeConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.concerns))
                                                                            .child(getResources().getString(R.string.active)).child(priority).push();
                                                                    String activeCaseKey = activeConcernsReference.getKey();
                                                                    activeConcernsReference.setValue(caseKey);

                                                                    DatabaseReference securityDeptReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns)).child(SECURITY).child(priority).child(activeCaseKey);

                                                                    securityDeptReference.setValue(caseKey);

                                                                    DatabaseReference deptWiseConcernReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns)).child(selectedDept).child(priority).child(activeCaseKey);

                                                                    deptWiseConcernReference.setValue(caseKey);
                                                                }
                                                            });
                                                            //progressDialog.dismiss();
                                                            dialog.dismiss();
                                                            finish();
                                                            Toast.makeText(getApplicationContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            dialog.dismiss();
                                                            Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                        catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            catch (IOException e) {
                e.printStackTrace();
            }
    }


        /*final StorageReference teamRef = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns)).child(caseKey).child(TEAM_PHOTO);
        teamRef.putFile(filePathTeam)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        teamRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                teamImageURL = uri.toString();

                                final StorageReference ref = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns)).child(caseKey).child(CONCERN_PHOTO);
                                ref.putFile(filePathIssue)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {

                                                        //Here code for saving images to device

                                                        if(fromCameraTeam){
                                                            BitmapDrawable drawable1 = (BitmapDrawable) teamPic.getDrawable();
                                                            Bitmap bitmap1 = drawable1.getBitmap();
                                                            OutputStream fos1;
                                                            File imageFile1 = null;
                                                            Uri imageUri1 = null;

                                                            try {
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                                    ContentResolver resolver1 = GembaWalkActivity.this.getContentResolver();
                                                                    ContentValues contentValues1 = new ContentValues();
                                                                    contentValues1.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey + TEAM_PHOTO);
                                                                    contentValues1.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                                                    contentValues1.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Concerns");
                                                                    contentValues1.put(MediaStore.Images.Media.IS_PENDING, 1);
                                                                    imageUri1 = resolver1.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues1);
                                                                    fos1 = resolver1.openOutputStream(imageUri1);

                                                                    bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos1);
                                                                    fos1.flush();
                                                                    fos1.close();

                                                                    contentValues1.put(MediaStore.Images.Media.IS_PENDING, 0);
                                                                    resolver1.update(imageUri1, contentValues1, null, null);
                                                                }
                                                                else {

                                                                    imageFile1 = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                                    File myDir1 = new File(imageFile1, "Open Concern Images");
                                                                    if (!myDir1.exists())
                                                                        myDir1.mkdirs();

                                                                    String name1 = caseKey + TEAM_PHOTO + ".png";
                                                                    myDir1 = new File(myDir1, name1);

                                                                    fos1 = new FileOutputStream(myDir1);
                                                                    Log.i("File save", "Successful");

                                                                    bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos1);
                                                                    fos1.flush();
                                                                    fos1.close();

                                                                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile1.getPath()}, new String[]{"image/jpeg"}, null);
                                                                }
//                                                              EXTERNAL_CONTENT_URI

                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        if(fromCameraIssue){
                                                            BitmapDrawable drawable2 = (BitmapDrawable) concernPic.getDrawable();
                                                            Bitmap bitmap2 = drawable2.getBitmap();
                                                            OutputStream fos2;
                                                            File imageFile2 = null;
                                                            Uri imageUri2 = null;

                                                            try {
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                                                                    ContentResolver resolver2 = GembaWalkActivity.this.getContentResolver();
                                                                    ContentValues contentValues2 = new ContentValues();
                                                                    contentValues2.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey + CONCERN_PHOTO);
                                                                    contentValues2.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                                                    contentValues2.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Concerns");
                                                                    contentValues2.put(MediaStore.Images.Media.IS_PENDING, 1);
                                                                    imageUri2 = resolver2.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues2);
                                                                    fos2 = resolver2.openOutputStream(imageUri2);

                                                                    bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                                                                    fos2.flush();
                                                                    fos2.close();

                                                                    contentValues2.put(MediaStore.Images.Media.IS_PENDING, 0);
                                                                    resolver2.update(imageUri2, contentValues2, null, null);
                                                                }
                                                                else {

                                                                    imageFile2 = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                                    File myDir2 = new File(imageFile2, "Open Concern Images");
                                                                    if (!myDir2.exists())
                                                                        myDir2.mkdirs();

                                                                    String name2 = caseKey + CONCERN_PHOTO + ".png";
                                                                    myDir2 = new File(myDir2, name2);

                                                                    fos2 = new FileOutputStream(myDir2);
                                                                    Log.i("File save", "Successful");

                                                                    bitmap2.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                                                                    fos2.flush();
                                                                    fos2.close();

                                                                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile2.getPath()}, new String[]{"image/jpeg"}, null);

                                                                }

//                                                              EXTERNAL_CONTENT_URI

                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        issueImageURL = uri.toString();
                                                        int selected = radioGroup.getCheckedRadioButtonId();
                                                        RadioButton button = include.findViewById(selected);
                                                        String priority = button.getText().toString();
                                                        String selectedDept = deptSpinner.getSelectedItem().toString();
                                                        String teamName = teamSpinner.getSelectedItem().toString();
                                                        GembaWalkClass g = new GembaWalkClass(type, userName, userDept, teamName, concernET.getText().toString(), locationET.getText().toString(), selectedDept, issueImageURL, teamImageURL, priority);
                                                        allConcernsReference.setValue(g);

                                                        DatabaseReference activeConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.concerns))
                                                                .child(getResources().getString(R.string.active)).child(priority).push();
                                                        String activeCaseKey = activeConcernsReference.getKey();
                                                        activeConcernsReference.setValue(caseKey);

                                                        DatabaseReference securityDeptReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns)).child(SECURITY).child(priority).child(activeCaseKey);;
                                                        securityDeptReference.setValue(caseKey);

                                                        DatabaseReference deptWiseConcernReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns)).child(selectedDept).child(priority).child(activeCaseKey);;
                                                        deptWiseConcernReference.setValue(caseKey);
                                                    }
                                                });
                                                //progressDialog.dismiss();
                                                dialog.dismiss();
                                                finish();
                                                Toast.makeText(getApplicationContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                dialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case TEAM_PICK_IMAGE_REQUEST:
                if(resultCode == RESULT_OK && data != null && data.getData() != null){
                    filePathTeam = data.getData();
                    onTeamImagePick(filePathTeam);
                }
                break;

            case TEAM_CAPTURE_IMAGE_REQUEST:
                if(resultCode == RESULT_OK) {
                    fromCameraTeam = true;
                    onTeamImagePick(filePathTeam);
                }
                break;

            case ISSUE_PICK_IMAGE_REQUEST:
                if(resultCode == RESULT_OK && data != null && data.getData() != null){
                    filePathIssue = data.getData();
                    onConcernImagePick(filePathIssue);
                }
                break;

            case ISSUE_CAPTURE_IMAGE_REQUEST:
                if(resultCode == RESULT_OK) {
                    fromCameraIssue = true;
                    onConcernImagePick(filePathIssue);
                }
                break;
        }
    }

    private void onTeamImagePick(Uri filePath){
        try {
            if(teamPhotoFromServer){
                Picasso.get().load(filePath).into(teamPic, new Callback() {
                    @Override
                    public void onSuccess() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
                teamPic.setAdjustViewBounds(true);
                teamImageUploadTV.setVisibility(View.GONE);
                teamImageDeleteButton.setVisibility(View.VISIBLE);

            }
            else {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageInByte = stream.toByteArray();
                long size = imageInByte.length;
                if(size > MAX_SIZE){
                    Toast.makeText(getApplicationContext(), "Image is too large", Toast.LENGTH_SHORT).show();
                    deleteTeamPic();
                }
                else {
                    teamPic.setAdjustViewBounds(true);
                    teamImageUploadTV.setVisibility(View.GONE);
                    teamImageDeleteButton.setVisibility(View.VISIBLE);
                    Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.5), (int) (bitmap.getHeight() * 0.5), true);
                    teamPic.setImageBitmap(resized);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onTeamImageDelete(View view){
        deleteTeamPic();
    }

    private void onConcernImagePick(Uri filePath){

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageInByte = stream.toByteArray();
            long size = imageInByte.length;
            if(size > MAX_SIZE){
                Toast.makeText(getApplicationContext(), "Image is too large", Toast.LENGTH_SHORT).show();
                deleteIssuePic();
            }
            else {
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.5), (int) (bitmap.getHeight() * 0.5), true);
                concernPic.setImageBitmap(resized);
                concernPic.setAdjustViewBounds(true);
                concernImageUploadTV.setVisibility(View.GONE);
                concernImageDeleteButton.setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onConcernImageDelete(View view){
        deleteIssuePic();
    }

    private void deleteTeamPic(){
        if(teamPhotoFromServer)
            teamPhotoFromServer = false;

        teamPic.setAdjustViewBounds(false);
        teamPic.setImageResource(R.drawable.ic_baseline_cloud_upload_24);
        teamImageUploadTV.setVisibility(View.VISIBLE);
        teamImageDeleteButton.setVisibility(View.GONE);
        filePathTeam = null;
        fromCameraTeam = false;
    }

    private void deleteIssuePic(){
        concernPic.setAdjustViewBounds(false);
        concernPic.setImageResource(R.drawable.ic_baseline_cloud_upload_24);
        concernImageUploadTV.setVisibility(View.VISIBLE);
        concernImageDeleteButton.setVisibility(View.GONE);
        filePathIssue = null;
    }

    AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            deleteTeamPic();

            String selected = teamSpinner.getItemAtPosition(i).toString();

            builder = new AlertDialog.Builder(GembaWalkActivity.this);
            View alertView = LayoutInflater.from(GembaWalkActivity.this).inflate(R.layout.uploading, null);
            TextView textView = alertView.findViewById(R.id.loadingTV);
            textView.setText(getResources().getString(R.string.loading));
            builder.setView(alertView);
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();


            StorageReference teamReference = FirebaseStorage.getInstance().getReference()
                    .child(getResources().getString(R.string.teamPhotos)).child(selected);
            teamReference.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.i("Photo file", "Found");

                            teamURIFromServer = uri;
                            Log.i("Team URI", teamURIFromServer.toString());
                            teamPhotoFromServer = true;
                            onTeamImagePick(teamURIFromServer);
                            Toast.makeText(getApplicationContext(), "Image found, you can proceed with this photo or choose a new photo for team", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Team photo file", "Not found");
                            Toast.makeText(getApplicationContext(), "Image not found, choose a new photo for team", Toast.LENGTH_LONG).show();
                            teamPhotoFromServer = false;
                            dialog.dismiss();
                        }
                    });
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}