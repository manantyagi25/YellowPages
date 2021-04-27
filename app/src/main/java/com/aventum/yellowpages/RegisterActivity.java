package com.aventum.yellowpages;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class RegisterActivity extends AppCompatActivity {

    EditText firstNameET, lastNameET, emailET, passwordET, confirmPasswordET, phoneET;
    Spinner deptSpinner;
    private String firstName, lastName, email, password, confirmPassword, phone;
    private FirebaseAuth auth;
    View include;

    public static boolean isInternetAvailable = true;

    public class CheckInternetConnection extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... contexts) {
            try {
                int timeoutMs = 1500;
                Socket sock = new Socket();
                SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

                sock.connect(sockaddr, timeoutMs);
                sock.close();

                return true;
            }
            catch (IOException e) { return false; }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CheckInternetConnection checkInternetConnection = new CheckInternetConnection();
        boolean internetConnectivity = false;
        try {
            internetConnectivity = checkInternetConnection.execute(getApplicationContext()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!internetConnectivity) {
            isInternetAvailable = false;
            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.checkYourInternet), Toast.LENGTH_LONG).show();
        }

        auth = FirebaseAuth.getInstance();

        include = findViewById(R.id.include);

        firstNameET = include.findViewById(R.id.registerFirstName);
        lastNameET = include.findViewById(R.id.registerLastName);
        emailET = include.findViewById(R.id.registerEmail);
        passwordET =include.findViewById(R.id.registerPassword);
        confirmPasswordET = include.findViewById(R.id.registerConfirmPassword);
        phoneET = include.findViewById(R.id.registerPhoneNumber);
        deptSpinner = include.findViewById(R.id.deptSpinner);
    }

    public void goToLogInActivity(View view){
        finish();
    }

    public void RegisterUser(View view){

        if(!isInternetAvailable){
            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.checkYourInternet), Toast.LENGTH_LONG).show();
        }
        else {
            firstName = firstNameET.getText().toString();
            lastName = lastNameET.getText().toString();
            email = emailET.getText().toString();
            password = passwordET.getText().toString();
            confirmPassword = confirmPasswordET.getText().toString();
            phone = phoneET.getText().toString();

            if ((firstName.length() > 0 && lastName.length() > 0 && validateEmail(email) && matchPassword(confirmPassword, password)) && validatePhone(phone)) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {                              //Sign Up Successful

                            Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();

                            FirebaseUser currentUser = auth.getCurrentUser();

                            String firstNameCap = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
                            String lastNameCap = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
                            String displayName = firstNameCap + " " + lastNameCap;
                            String dept = deptSpinner.getSelectedItem().toString();

                            pInfo pInfo = new pInfo(displayName, "Unit 1", dept);
                            accInfo accInfo = new accInfo(email, phone);

                            User user = new User(pInfo, accInfo);
                            assert currentUser != null;
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            databaseReference.child(getResources().getString(R.string.tableName)).child(currentUser.getUid()).child(getResources().getString(R.string.info)).setValue(user);

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName).build();

                            currentUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Update Profile Function", "User profile updated.");
                                            }
                                        }
                                    });

                            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                            startActivity(intent);
                        } else {                           //Sign Up Failed
                            Log.i("Error", task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else {
                Toast.makeText(getApplicationContext(), "Please fill all the above fields!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean matchPassword(String confirmPassword, String password){
        if(confirmPassword.length() != 0 && password.length() != 0)
            return confirmPassword.equals(password);
        return false;
    }

    private boolean validateEmail(String email){
        if (TextUtils.isEmpty(email))
            return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validatePhone(String phone){
        if(phone.length() == 0)
            return true;
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }
}