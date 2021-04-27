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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.aventum.yellowpages.MainActivity.MAX_SIZE;
import static com.aventum.yellowpages.MainActivity.userDept;
import static com.aventum.yellowpages.MainActivity.userName;

public class SafetyConcernActivity extends AppCompatActivity {

    public static String type = "0";
    EditText concernET, locationET;
    Spinner deptSpinner;
    ImageView pic;
    RadioGroup radioGroup;
    View include;
    String imageURL;
    ImageButton deleteButton;
    TextView uploadPhotoTV;
    AlertDialog.Builder builder;
    AlertDialog dialog;

    private final int PICK_IMAGE_REQUEST = 1;
    private final int CAPTURE_IMAGE_REQUEST = 2;
    private final String CONCERN_PHOTO = "concern";
    private final String SECURITY = "Security";
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.aventum.yellowpages.fileprovider";
    private boolean fromCamera = false;

    private Uri filePath;
    String currentPhotoPath;
    File concernImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_concern);
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
        pic = include.findViewById(R.id.uploadImage);
        radioGroup = include.findViewById(R.id.priorityRG);
        deleteButton = include.findViewById(R.id.deleteButton);
        uploadPhotoTV = include.findViewById(R.id.uploadConcernPhotoTV);

        concernET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        concernET.setRawInputType(InputType.TYPE_CLASS_TEXT);

        locationET.setImeOptions(EditorInfo.IME_ACTION_DONE);
        locationET.setRawInputType(InputType.TYPE_CLASS_TEXT);

        //toolbar.setTitle(getResources().getString(R.string.newSafetyConcern));
    }



    public void cancelSCA(View view){
        finish();
    }

    public void submitSafetyConcern(View view){
        if(concernET.getText().length() > 0 && locationET.getText().length() > 0 && filePath != null) {
            uploadToStorage();

            builder = new AlertDialog.Builder(this);
            View alertView = LayoutInflater.from(this).inflate(R.layout.uploading, null);
            builder.setView(alertView);
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        else {
            //Toast.makeText(getApplicationContext(), "One or more necessary fields are empty", Toast.LENGTH_SHORT).show();
            Snackbar snackbar = Snackbar.make(view, "Please fill all the fields", BaseTransientBottomBar.LENGTH_LONG);
            snackbar.show();
        }

    }

    public void uploadPhoto(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.choose_or_take_image_alert_layout, null);
        //builder.setView(dialogView);

        final AlertDialog dialog = new AlertDialog.Builder(SafetyConcernActivity.this)
                .setView(dialogView)
                .show();

        ConstraintLayout fromGalleryCL, fromCameraCL;
        fromGalleryCL = dialogView.findViewById(R.id.fromGalleryCL);
        fromCameraCL = dialogView.findViewById(R.id.fromCameraCL);

        fromGalleryCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                dialog.dismiss();
            }
        });

        fromCameraCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeImage();
                dialog.dismiss();
            }
        });
    }

    public void chooseImage() {
        ActivityCompat.requestPermissions(SafetyConcernActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
    }

    private void takeImage(){
        ActivityCompat.requestPermissions(SafetyConcernActivity.this, new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE_REQUEST){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
            else {
                Toast.makeText(getApplicationContext(), "Storage access permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == CAPTURE_IMAGE_REQUEST){
            if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                File path = new File(getFilesDir(), "newPhoto/");
                if (!path.exists())
                    path.mkdirs();

                File image = new File(path, "concernImage.jpg");
                filePath = FileProvider.getUriForFile(SafetyConcernActivity.this, CAPTURE_IMAGE_FILE_PROVIDER, image);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
            }
            else {
                Toast.makeText(getApplicationContext(), "Camera access permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void uploadToStorage(){
        /*final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image...");
        progressDialog.show();*/

        final DatabaseReference allConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.allConcerns)).push();
        final String caseKey = allConcernsReference.getKey();

        Bitmap bitmap = null;
        byte[] picCompressed;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            picCompressed = stream.toByteArray();

            final StorageReference ref = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns)).child(caseKey).child(CONCERN_PHOTO);
            ref.putBytes(picCompressed)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    //Save image to gallery if from camera
                                    if(fromCamera) {
                                        BitmapDrawable drawable = (BitmapDrawable) pic.getDrawable();
                                        Bitmap bitmap = drawable.getBitmap();
                                        OutputStream fos;
                                        File imageFile = null;
                                        Uri imageUri = null;

                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                ContentResolver resolver = SafetyConcernActivity.this.getContentResolver();
                                                ContentValues contentValues = new ContentValues();
                                                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey);
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
//                                            File file = new File(getApplicationContext().getExternalFilesDir(null).toString());
//                                            File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());

                                                imageFile = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                File myDir = new File(imageFile, "Open Concern Images");
                                                if (!myDir.exists())
                                                    myDir.mkdirs();

                                                String name = caseKey + ".png";
                                                myDir = new File(myDir, name);

                                        /*FileOutputStream outputStream = new FileOutputStream(myDir);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                        outputStream.flush();
                                        outputStream.close();*/
                                                fos = new FileOutputStream(myDir);
                                                Log.i("File save", "Successful");

                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                                fos.flush();
                                                fos.close();

//                                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
                                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile.getPath()}, new String[]{"image/jpeg"}, null);

                                            }

//                                    EXTERNAL_CONTENT_URI

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    imageURL = uri.toString();
                                    int selected = radioGroup.getCheckedRadioButtonId();
                                    RadioButton button = include.findViewById(selected);
                                    String concern = concernET.getText().toString().replace("\\n", "");
                                    String location = locationET.getText().toString().replace("\\n", "");
                                    String priority = button.getText().toString();
                                    String selectedDept = deptSpinner.getSelectedItem().toString();
                                    SafetyConcernClass s = new SafetyConcernClass(type, userName, userDept, concern, location, selectedDept, imageURL, priority);
                                    allConcernsReference.setValue(s);

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
        catch (IOException e){
            e.printStackTrace();
        }

        /*final StorageReference ref = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns)).child(caseKey).child(CONCERN_PHOTO);
        ref.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                //Save image to gallery if from camera
                                if(fromCamera) {
                                    BitmapDrawable drawable = (BitmapDrawable) pic.getDrawable();
                                    Bitmap bitmap = drawable.getBitmap();
                                    OutputStream fos;
                                    File imageFile = null;
                                    Uri imageUri = null;

                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            ContentResolver resolver = SafetyConcernActivity.this.getContentResolver();
                                            ContentValues contentValues = new ContentValues();
                                            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, caseKey);
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
//                                            File file = new File(getApplicationContext().getExternalFilesDir(null).toString());
//                                            File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());

                                            imageFile = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                            File myDir = new File(imageFile, "Open Concern Images");
                                            if (!myDir.exists())
                                                myDir.mkdirs();

                                            String name = caseKey + ".png";
                                            myDir = new File(myDir, name);

                                        *//*FileOutputStream outputStream = new FileOutputStream(myDir);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                        outputStream.flush();
                                        outputStream.close();*//*
                                            fos = new FileOutputStream(myDir);
                                            Log.i("File save", "Successful");

                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                            fos.flush();
                                            fos.close();

//                                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
                                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile.getPath()}, new String[]{"image/jpeg"}, null);

                                        }

//                                    EXTERNAL_CONTENT_URI

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                imageURL = uri.toString();
                                int selected = radioGroup.getCheckedRadioButtonId();
                                RadioButton button = include.findViewById(selected);
                                String priority = button.getText().toString();
                                String selectedDept = deptSpinner.getSelectedItem().toString();
                                SafetyConcernClass s = new SafetyConcernClass(type, userName, userDept, concernET.getText().toString(), locationET.getText().toString(), selectedDept, imageURL, priority);
                                allConcernsReference.setValue(s);

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
                });*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            Log.i("File path from pick gallery", filePath.toString());
            onImagePick(filePath);
        }
        else if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Log.i("filePath", filePath.toString());
            fromCamera = true;
            onImagePick(filePath);
        }
    }

    private void onImagePick(Uri filePath){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageInByte = stream.toByteArray();
            long size = imageInByte.length;
            if(size > MAX_SIZE){
                Toast.makeText(getApplicationContext(), "Image is too large", Toast.LENGTH_SHORT).show();
                delete();
            }
            else {
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.5), (int) (bitmap.getHeight() * 0.5), true);
                pic.setImageBitmap(resized);
                pic.setAdjustViewBounds(true);
                uploadPhotoTV.setVisibility(View.GONE);
                deleteButton.setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onImageDelete(View view){
        delete();
    }

    private void delete(){
        pic.setAdjustViewBounds(false);
        pic.setImageResource(R.drawable.ic_baseline_cloud_upload_24);
        uploadPhotoTV.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.GONE);
        filePath = null;
        fromCamera = false;
    }
}