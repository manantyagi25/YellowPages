package com.aventum.yellowpages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.aventum.yellowpages.MainActivity.user;

public class AccountSettingsActivity extends AppCompatActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        preferences = getSharedPreferences("com.example.yellowpages", MODE_PRIVATE);
    }

    public void backToMain(View view){
        finish();
    }

    public void updatePassword(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_password_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(AccountSettingsActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText email, oldPW, newPW, confirmNewPW;
        Button updateButton;

        email = dialogView.findViewById(R.id.email);
        oldPW = dialogView.findViewById(R.id.lastPassword);
        newPW = dialogView.findViewById(R.id.newPassword);
        confirmNewPW = dialogView.findViewById(R.id.confirmNewPassword);
        updateButton = dialogView.findViewById(R.id.updateButton);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(validateEmail(email.getText().toString()) && oldPW.getText().length() > 8) {
                    AuthCredential credential = EmailAuthProvider.getCredential(email.getText().toString(), oldPW.getText().toString());

                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        if(newPW.getText().length() > 0 && confirmNewPW.getText().length() > 0) {
                                            if (matchPassword(newPW.getText().toString(), confirmNewPW.getText().toString())) {
                                                user.updatePassword(newPW.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getApplicationContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                                            dialog.dismiss();
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), "Couldn't update password, please try again later!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                            else {
                                                Toast.makeText(getApplicationContext(), "New Password and Confirm New Password fields are not matching!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), "New Password or Confirm New Password field empty!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Email ID or password invalid!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else{
                    Toast.makeText(getApplicationContext(), "Email or Password empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updatePhone(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_phone_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(AccountSettingsActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText phone;
        Button updateButton;

        phone = dialogView.findViewById(R.id.newPhone);
        updateButton = dialogView.findViewById(R.id.updateButton);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phone.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Phone number empty!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(validatePhone(phone.getText().toString())){
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child(getResources().getString(R.string.tableName))
                                .child(user.getUid())
                                .child(getResources().getString(R.string.info))
                                .child("accInfo")
                                .child("phone")
                                .setValue(phone.getText().toString());

                        Toast.makeText(getApplicationContext(), "Phone number updated successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Please enter a valid phone number!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void updateName(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_name_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(AccountSettingsActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText name;
        Button updateButton;

        name = dialogView.findViewById(R.id.newName);
        updateButton = dialogView.findViewById(R.id.updateButton);

        name.setText(preferences.getString(getResources().getString(R.string.userName), ""));
        name.setSelection(name.getText().length());

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Name can't be empty!", Toast.LENGTH_SHORT).show();
                }
                else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference.child(getResources().getString(R.string.tableName))
                            .child(user.getUid())
                            .child(getResources().getString(R.string.info))
                            .child("pInfo")
                            .child("name")
                            .setValue(name.getText().toString());

                    preferences.edit().putString(getResources().getString(R.string.userName), name.getText().toString()).apply();

                    Toast.makeText(getApplicationContext(), "Name updated successfully! " +  getResources().getString(R.string.restartForChanges), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    public void updateDept(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_department_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(AccountSettingsActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText codeET;
        Button verifyButton;
        LinearLayout verifyCodeLL, allowDeptChangeLL;
        ImageButton closeDialog;

        codeET = dialogView.findViewById(R.id.code);
        verifyButton = dialogView.findViewById(R.id.verifyCodeButton);
        verifyCodeLL = dialogView.findViewById(R.id.verifyingLL);
        allowDeptChangeLL = dialogView.findViewById(R.id.allowDeptChangeLL);
        closeDialog = dialogView.findViewById(R.id.cancelDeptChange);

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = codeET.getText().toString();
                if(code.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please enter a code!", Toast.LENGTH_SHORT).show();
                }
                else {
                    verifyCodeLL.setVisibility(View.VISIBLE);

                    DatabaseReference codeRef = FirebaseDatabase.getInstance().getReference().child(getResources()
                            .getString(R.string.deptChangeCodes))
                            .child(user.getUid());

                    codeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String serverCode = snapshot.getValue(String.class);

                                if(code.equals(serverCode)){

                                    verifyCodeLL.setVisibility(View.GONE);
                                    allowDeptChangeLL.setVisibility(View.VISIBLE);

                                    Spinner deptSpinner = dialogView.findViewById(R.id.newDeptSpinner);
                                    Button changeDeptButton = dialogView.findViewById(R.id.changeDeptButton);

                                    changeDeptButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String newDept = deptSpinner.getSelectedItem().toString();

                                            codeRef.removeValue();

                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(getResources().getString(R.string.tableName))
                                                    .child(user.getUid())
                                                    .child(getResources().getString(R.string.info))
                                                    .child("pInfo")
                                                    .child("dept")
                                                    .setValue(newDept);

                                            preferences.edit().putString(getResources().getString(R.string.userDept), newDept).apply();

                                            Toast.makeText(getApplicationContext(), "Department updated successfully! " +  getResources().getString(R.string.restartForChanges), Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    });
                                }
                                else {
                                    verifyCodeLL.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Invalid code, please try again!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                verifyCodeLL.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(),
                                        "You don't have permission to change your department currently! Contact your admin for this action!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    public void updateUnit(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_unit_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(AccountSettingsActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText unit;
        Button updateButton;

        unit = dialogView.findViewById(R.id.newUnit);
        updateButton = dialogView.findViewById(R.id.updateButton);

        unit.setText(preferences.getString(getResources().getString(R.string.userUnit), ""));
        unit.setSelection(unit.getText().length());

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(unit.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Name can't be empty!", Toast.LENGTH_SHORT).show();
                }
                else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference.child(getResources().getString(R.string.tableName))
                            .child(user.getUid())
                            .child(getResources().getString(R.string.info))
                            .child("pInfo")
                            .child("unit")
                            .setValue(unit.getText().toString());

                    preferences.edit().putString(getResources().getString(R.string.userUnit), unit.getText().toString()).apply();

                    Toast.makeText(getApplicationContext(), "Unit updated successfully! " + getResources().getString(R.string.restartForChanges), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    public void deleteAccount(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.delete_account_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(AccountSettingsActivity.this)
                .setView(dialogView)
                .show();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.TOP);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText email, password;
        Button deleteButton;

        email = dialogView.findViewById(R.id.email);
        password = dialogView.findViewById(R.id.password);
        deleteButton = dialogView.findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String e = email.getText().toString(), p = password.getText().toString();

                if(validateEmail(e) && password.length() > 0){
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = user.getUid();

                    AuthCredential credential = EmailAuthProvider.getCredential(e, p);

                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.i("TAG", "onComplete: authentication complete");
                                        user.delete()
                                                .addOnCompleteListener (new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            FirebaseDatabase.getInstance().getReference()
                                                            .child(getResources().getString(R.string.tableName))
                                                                    .child(uid)
                                                                    .removeValue();

                                                            SharedPreferences.Editor editor = preferences.edit();
                                                            editor.putString(getResources().getString(R.string.userName), "");
                                                            editor.putString(getResources().getString(R.string.userUnit), "");
                                                            editor.putString(getResources().getString(R.string.userDept), "");
                                                            editor.apply();

                                                            dialog.dismiss();

                                                            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);

                                                            Toast.makeText(getApplicationContext(), "Account deleted successfully!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), "There was an error while deleting account, please try again later!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Authentication failed, either username or password incorrect!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else if(!validateEmail(e)){
                    Toast.makeText(getApplicationContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                }
                else if(e.length() == 0 || p.length() == 0){
                    Toast.makeText(getApplicationContext(), "Please fill the required fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean matchPassword(String password, String confirmPassword){
        if(confirmPassword.length() != 0 && password.length() != 0 && password.length() >= 8)
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
            return false;
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }
}