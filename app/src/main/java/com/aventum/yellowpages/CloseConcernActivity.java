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

public class CloseConcernActivity extends AppCompatActivity {

    public static final String CONCERN_KEY = "concernKey";
    public static final String REF_KEY = "refKey";
    public static final String PRIORITY = "priority";
    public static final String DEPT = "dept";
    public static final String SECURITY = "Security";
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.aventum.yellowpages.fileprovider";

    private final String PROOF_PHOTO = "proof";
    private final int PICK_IMAGE_REQUEST = 1;
    private final int CAPTURE_IMAGE_REQUEST = 2;
    String concernKey, refKey, proofImageURL, priority, dept;
    private Uri filePathProof;
    private boolean fromCamera = false;

    AlertDialog.Builder builder;
    AlertDialog dialog;
    Bitmap bitmap;

    View include;
    ImageView proofPic;
    TextView uploadImageTV;
    ImageButton deleteImageButton;
    EditText causeET, immediateActionET, permanentActionET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_concern);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        Intent intent = getIntent();
        concernKey = intent.getStringExtra(CONCERN_KEY);
        refKey = intent.getStringExtra(REF_KEY);
        priority = intent.getStringExtra(PRIORITY);
        dept = intent.getStringExtra(DEPT);

        include = findViewById(R.id.include);
        proofPic = include.findViewById(R.id.uploadImage);
        uploadImageTV = include.findViewById(R.id.uploadProofImageTV);
        deleteImageButton = include.findViewById(R.id.proofImageDeleteButton);
        causeET = include.findViewById(R.id.concernCauseET);
        immediateActionET = include.findViewById(R.id.immediateActionET);
        permanentActionET = include.findViewById(R.id.permanentActionET);

        causeET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        causeET.setRawInputType(InputType.TYPE_CLASS_TEXT);

        immediateActionET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        immediateActionET.setRawInputType(InputType.TYPE_CLASS_TEXT);

        permanentActionET.setImeOptions(EditorInfo.IME_ACTION_DONE);
        permanentActionET.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }

    public void cancelCCA(View view){
        finish();
    }

    public void submitProof(View view){

        if(causeET.getText().length() > 0 && immediateActionET.getText().length() > 0 & permanentActionET.getText().length() > 0 && filePathProof != null) {

            builder = new AlertDialog.Builder(this);
            View alertView = LayoutInflater.from(this).inflate(R.layout.uploading, null);
            /*TextView loadingTV = alertView.findViewById(R.id.loadingTV);
            loadingTV.setText(getResources().getString(R.string.uploadingAndClosingConcern));*/
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


    public void uploadProofPhoto(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.choose_or_take_image_alert_layout, null);
        //builder.setView(dialogView);

        final AlertDialog dialog = new AlertDialog.Builder(CloseConcernActivity.this)
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
        ActivityCompat.requestPermissions(CloseConcernActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
    }

    private void takeImage(){
        ActivityCompat.requestPermissions(CloseConcernActivity.this, new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_REQUEST);
    }


    public void uploadToStorage() {

        final DatabaseReference concernReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.allConcerns)).child(concernKey);

        /*final StorageReference proofRef = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns))
                .child(concernKey).child(PROOF_PHOTO);
        proofRef.putFile(filePathProof)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        proofRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                proofImageURL = uri.toString();

                                Proof proof = new Proof(userName, userDept, causeET.getText().toString(), immediateActionET.getText().toString(),
                                        permanentActionET.getText().toString(), proofImageURL);

                                concernReference.child(getResources().getString(R.string.proof)).setValue(proof);

                                DatabaseReference activeConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.concerns))
                                        .child(getResources().getString(R.string.active)).child(priority).child(refKey);
                                activeConcernsReference.removeValue();

                                DatabaseReference deptRef = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns))
                                        .child(dept).child(priority).child(refKey);
                                deptRef.removeValue();

                                DatabaseReference securityRef = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns))
                                        .child(SECURITY).child(priority).child(refKey);
                                securityRef.removeValue();

                                DatabaseReference resolvedConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.concerns))
                                        .child(getResources().getString(R.string.resolved)).push();
                                resolvedConcernsReference.setValue(concernKey);
                            }
                        });
                        //progressDialog.dismiss();
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "Concern close successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });*/

        Bitmap bitmap = null;
        byte[] picCompressed;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePathProof);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            picCompressed = stream.toByteArray();

            final StorageReference proofRef = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.concerns))
                    .child(concernKey).child(PROOF_PHOTO);
            proofRef.putBytes(picCompressed)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            proofRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    //Save image to gallery if from camera
                                    if(fromCamera) {
                                        BitmapDrawable drawable = (BitmapDrawable) proofPic.getDrawable();
                                        Bitmap bitmap = drawable.getBitmap();
                                        OutputStream fos;
                                        File imageFile = null;
                                        Uri imageUri = null;

                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                ContentResolver resolver = CloseConcernActivity.this.getContentResolver();
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
                                            }
                                            else {
//                                            File file = new File(getApplicationContext().getExternalFilesDir(null).toString());
//                                            File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());

                                                imageFile = new File(getApplicationContext().getExternalFilesDir(null).toString());
                                                File myDir = new File(imageFile, "Open Concern Images");
                                                if (!myDir.exists())
                                                    myDir.mkdirs();

                                                String name = concernKey + ".png";
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

                                    proofImageURL = uri.toString();

                                    String cause = causeET.getText().toString().replace("\\n", "");
                                    String immAction = immediateActionET.getText().toString().replace("\\n", "");
                                    String permAction = permanentActionET.getText().toString().replace("\\n", "");

                                    Proof proof = new Proof(userName, userDept, cause , immAction, permAction, proofImageURL);

                                    concernReference.child(getResources().getString(R.string.proof)).setValue(proof);

                                    DatabaseReference activeConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.concerns))
                                            .child(getResources().getString(R.string.active)).child(priority).child(refKey);
                                    activeConcernsReference.removeValue();

                                    DatabaseReference deptRef = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns))
                                            .child(dept).child(priority).child(refKey);
                                    deptRef.removeValue();

                                    DatabaseReference securityRef = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns))
                                            .child(SECURITY).child(priority).child(refKey);
                                    securityRef.removeValue();

                                    DatabaseReference resolvedConcernsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.concerns))
                                            .child(getResources().getString(R.string.resolved)).push();
                                    resolvedConcernsReference.setValue(concernKey);
                                }
                            });
                            //progressDialog.dismiss();
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Concern close successfully!", Toast.LENGTH_SHORT).show();
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
                filePathProof = FileProvider.getUriForFile(CloseConcernActivity.this, CAPTURE_IMAGE_FILE_PROVIDER, image);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, filePathProof);
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
            }
            else {
                Toast.makeText(getApplicationContext(), "Camera access permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePathProof = data.getData();
            onProofImagePick(filePathProof);
        }
        else if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Log.i("filePath", filePathProof.toString());
            fromCamera = true;
            onProofImagePick(filePathProof);
        }
    }

    private void onProofImagePick(Uri filePath){

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageInByte = stream.toByteArray();
            long size = imageInByte.length;
            if(size > MAX_SIZE){
                Toast.makeText(getApplicationContext(), "Image is too large", Toast.LENGTH_SHORT).show();
                deleteProofPic();
            }
            else {
                Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.5), (int)(bitmap.getHeight()*0.5), true);
                proofPic.setImageBitmap(resized);
                proofPic.setAdjustViewBounds(true);
                uploadImageTV.setVisibility(View.GONE);
                deleteImageButton.setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onProofImageDelete(View view){
        deleteProofPic();
    }

    private void deleteProofPic(){
        proofPic.setAdjustViewBounds(false);
        proofPic.setImageResource(R.drawable.ic_baseline_cloud_upload_24);
        uploadImageTV.setVisibility(View.VISIBLE);
        deleteImageButton.setVisibility(View.GONE);
        filePathProof = null;
        fromCamera = false;
    }
}