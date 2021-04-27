package com.aventum.yellowpages;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class TeamPhotoActivity extends AppCompatActivity {

    public static final String URL = "url";
    public static final String TEAM_NAME = "teamName";

    ImageView teamPicIV;
    TextView teamNameTV;

    String teamName, url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_photo);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.loading_risks_alert_layout, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Intent intent = getIntent();
        teamName = intent.getStringExtra(TEAM_NAME);
        url = intent.getStringExtra(URL);

        teamPicIV = findViewById(R.id.teamPhotoIV);
        teamNameTV = findViewById(R.id.teamNameHeadingTV);

        teamNameTV.setText(teamName);

        Picasso.get().load(url).into(teamPicIV, new Callback() {
            @Override
            public void onSuccess() {
                dialog.dismiss();
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public void close(View view){
        finish();
    }
}