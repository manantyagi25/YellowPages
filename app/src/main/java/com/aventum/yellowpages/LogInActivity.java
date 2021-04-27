package com.aventum.yellowpages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class LogInActivity extends Activity {

    EditText email, password;
    SharedPreferences preferences;
    FirebaseAuth auth;
    public static boolean isInternetAvailable = true;

    Context context;
    AlertDialog.Builder builder;
    AlertDialog dialog;

    View rootView;

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
        setContentView(R.layout.activity_log_in);

        context = LogInActivity.this;
        email = findViewById(R.id.emailET);
        password = findViewById(R.id.passwordET);
        auth = FirebaseAuth.getInstance();
        rootView = findViewById(R.id.rootViewLA);
        preferences = getSharedPreferences("com.example.yellowpages", MODE_PRIVATE);

        CheckInternetConnection checkInternetConnection = new CheckInternetConnection();
        boolean internetConnectivity = false;
        try {
            internetConnectivity = checkInternetConnection.execute(getApplicationContext()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!internetConnectivity) {
            isInternetAvailable = false;
            rootView.setVisibility(View.INVISIBLE);
//                Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.checkYourInternet), Toast.LENGTH_LONG).show();
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.no_internet_layout, null);

            final AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            Button exitButton = dialogView.findViewById(R.id.exitButton);

            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finishAndRemoveTask();
                }
            });
        }
        else{
            boolean accept = preferences.getBoolean(getResources().getString(R.string.TCAccepted), false);
//        boolean show = false;

            if(!accept){
                builder = new AlertDialog.Builder(context);
                View alertView = LayoutInflater.from(context).inflate(R.layout.terms_and_conditions_dialog, null);
                alertView.findViewById(R.id.acceptButton).setOnClickListener(acceptListener);
                alertView.findViewById(R.id.declineButton).setOnClickListener(declineListener);
                /*acceptButton.setOnClickListener(acceptListener);
                declineButton.setOnClickListener(declineListener);*/
                builder.setView(alertView);
                dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        }
    }

    public void validateInputAndLoginUser(final View view) {
        if(isInternetAvailable){
            if(email.getText().length() > 0 && password.getText().length() > 0) {
                auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {                              //Sign in Successful

                            Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_LONG).show();

//                        view.setVisibility(View.GONE);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else                                                //Sign in Failure
                            Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.checkYourInternet), Toast.LENGTH_LONG).show();
        }
    }

    public void goToRegisterActivity(View view){
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }

    public void forgotPassword(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.forgot_password_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(LogInActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText resetEmailET = dialogView.findViewById(R.id.resetEmail);
        Button sendResetLinkButton = dialogView.findViewById(R.id.sendResetLinkButton);

        sendResetLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = resetEmailET.getText().toString();

                if(email.length() > 0 && validateEmail(email)) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Password reset email sent. Check your inbox!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Provided email not associated with any account!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "An error occurred, please try again later", Toast.LENGTH_LONG).show();
                                }
                            });
                }
                else if(email.length() == 0){
                    Toast.makeText(getApplicationContext(), "Please enter your email", Toast.LENGTH_LONG).show();
                }
                else if(!validateEmail(email)){
                    Toast.makeText(getApplicationContext(), "Please enter a valid email", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validateEmail(String email){
        if (TextUtils.isEmpty(email))
            return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    View.OnClickListener acceptListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            preferences.edit().putBoolean(getResources().getString(R.string.TCAccepted), true).apply();
            dialog.dismiss();
        }
    };

    View.OnClickListener declineListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            preferences.edit().putBoolean(getResources().getString(R.string.TCAccepted), false).apply();
            finishAndRemoveTask();
        }
    };
}