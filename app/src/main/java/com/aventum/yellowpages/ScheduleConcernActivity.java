package com.aventum.yellowpages;

import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ScheduleConcernActivity extends AppCompatActivity {

    View include;
    Spinner valueSpinner, intervalSpinner;
    String duration, interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_concern);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        include = findViewById(R.id.include);
        valueSpinner = include.findViewById(R.id.durationSpinner);
        intervalSpinner = include.findViewById(R.id.intervalSpinner);

        duration = valueSpinner.getSelectedItem().toString();
        interval = intervalSpinner.getSelectedItem().toString();




    }

    public void schedule(View view){
        int total = 0;
        int dur = Integer.parseInt(duration);

        switch (interval){
            case "minutes":
                total = dur * 60;
                break;

            case "hours":
                total = dur * 60 * 60;
                break;

            case "days":
                total = dur * 60 * 60 * 24;
                break;
        }


    }
}