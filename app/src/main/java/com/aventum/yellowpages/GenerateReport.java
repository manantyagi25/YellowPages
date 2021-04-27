package com.aventum.yellowpages;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class GenerateReport extends JobIntentService {

    /*@Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "Generating report...", Toast.LENGTH_SHORT).show();
    }*/

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Report generated!", Toast.LENGTH_SHORT).show();
    }*/
}
