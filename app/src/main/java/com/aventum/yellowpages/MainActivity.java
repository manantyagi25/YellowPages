package com.aventum.yellowpages;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;


public class MainActivity extends Activity {

    Button closeConcernButton;
    TextView nameTV, unitTV, deptTV;
//    ConstraintLayout generateReportCL;
//    ConstraintLayout adminSettingsCL;
    public static String userName, userUnit, userDept, lastGenTime;
    SharedPreferences preferences;
    View includedView, rootView;
    static public FirebaseAuth auth;
    static public FirebaseUser user;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    String generateReportURL = "http://ec2-65-1-2-137.ap-south-1.compute.amazonaws.com:5000/createExcel";
    String reportDownloadURL = "http://ec2-65-1-2-137.ap-south-1.compute.amazonaws.com:5000/api/downloadfile/Report.zip";

    public static final String CHANNEL_ID = "scheduledReminderService";
    private static final String DEPT_TO_MAKE_REPORT = "Security";
    public static final int progress_bar_type = 0;
    public static final int MAX_SIZE = 10485760; //10MB max
    public static final int MAX_TIME_DIFF = 6000000;

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
        setContentView(R.layout.activity_main);

        //createNotificationChannel();

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        preferences = getSharedPreferences("com.example.yellowpages", MODE_PRIVATE);

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
            startActivity(intent);
        }
        else {
            includedView = findViewById(R.id.include);
            rootView = findViewById(R.id.rootView);

            CheckInternetConnection checkInternetConnection = new CheckInternetConnection();
            boolean internetConnectivity = false;
            try {
                internetConnectivity = checkInternetConnection.execute(getApplicationContext()).get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //When no internet is available
            if(!internetConnectivity) {
                isInternetAvailable = false;
                rootView.setVisibility(View.INVISIBLE);
//                Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.checkYourInternet), Toast.LENGTH_LONG).show();
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.no_internet_layout, null);

                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
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

            //When internet is available
            else {

                isInternetAvailable = true;

                //To show admin settings
                    /*FirebaseDatabase.getInstance().getReference()
                            .child(getResources().getString(R.string.adminUID)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String adminUID = snapshot.getValue(String.class);
                            if(user.getUid().equals(adminUID))
                                adminSettingsCL.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });*/

                nameTV = includedView.findViewById(R.id.userName);
                unitTV = includedView.findViewById(R.id.userUnit);
                deptTV = includedView.findViewById(R.id.userDept);
//                generateReportCL = includedView.findViewById(R.id.generateReport);
                //            adminSettingsCL = includedView.findViewById(R.id.adminSettingsCL);

                //If details are present in preferences, no need to load from server
                if (!preferences.getString(getResources().getString(R.string.userName), "").equals("")) {

                    //To show admin settings
                    /*FirebaseDatabase.getInstance().getReference()
                            .child(getResources().getString(R.string.adminUID)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String adminUID = snapshot.getValue(String.class);
                            if(user.getUid().equals(adminUID))
                                adminSettingsCL.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });*/

                    Log.i("Data", "Found in preferences");
                    userName = preferences.getString(getResources().getString(R.string.userName), "");
                    userUnit = preferences.getString(getResources().getString(R.string.userUnit), "");
                    userDept = preferences.getString(getResources().getString(R.string.userDept), "");
                    /*lastGenTime = preferences.getString(getResources().getString(R.string.reportLastGeneratedTimestamp), "");
                    long time = Long.parseLong(lastGenTime);*/

                    /*if (userDept.equals(DEPT_TO_MAKE_REPORT))
                        generateReportCL.setVisibility(View.VISIBLE);*/

                    /*if (userDept.equals(DEPT_TO_MAKE_REPORT)) {
                        if(System.currentTimeMillis() -  time < MAX_TIME_DIFF){
                            generateReportCL.setVisibility(View.GONE);
                        }
                        else{
                            generateReportCL.setVisibility(View.VISIBLE);
                        }
                    }*/

                    nameTV.setText(userName);
                    unitTV.setText(userUnit);
                    deptTV.setText(userDept);
                }

                //If first login or in case details not in preferences, load from server and store in preferences
                else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    final View view = LayoutInflater.from(this).inflate(R.layout.loading_risks_alert_layout, null);
                    builder.setView(view);
                    final AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();

                    FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.tableName)).child(user.getUid())
                            .child(getResources().getString(R.string.info)).child(getResources().getString(R.string.personalInfo))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    pInfo pInfo = snapshot.getValue(com.aventum.yellowpages.pInfo.class);

                                    userName = pInfo.getName();
                                    userUnit = pInfo.getUnit();
                                    userDept = pInfo.getDept();

                                    /*if (userDept.equals(DEPT_TO_MAKE_REPORT))
                                        generateReportCL.setVisibility(View.VISIBLE);*/

                                    /*FirebaseDatabase.getInstance().getReference()
                                            .child(getResources().getString(R.string.adminUID)).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String adminUID = snapshot.getValue(String.class);
                                            if(user.getUid().equals(adminUID))
                                                adminSettingsCL.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });*/

                                    nameTV.setText(userName);
                                    unitTV.setText(userUnit);
                                    deptTV.setText(userDept);

                                    /*FirebaseDatabase.getInstance().getReference()
                                            .child(getResources().getString(R.string.reportLastGeneratedTimestamp))
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    Long timestamp = snapshot.getValue(Long.class);
                                                    String time = String.valueOf(timestamp);
                                                    preferences.edit().putString(getResources().getString(R.string.reportLastGeneratedTimestamp), time).apply();
                                                    Log.i("Last time", preferences.getString(getResources().getString(R.string.reportLastGeneratedTimestamp), ""));

                                                    if (userDept.equals(DEPT_TO_MAKE_REPORT)) {
                                                        if(System.currentTimeMillis() -  timestamp < MAX_TIME_DIFF){
                                                            generateReportCL.setVisibility(View.GONE);
                                                        }
                                                        else{
                                                            generateReportCL.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });*/

                                    dialog.dismiss();

                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(getResources().getString(R.string.userName), userName);
                                    editor.putString(getResources().getString(R.string.userUnit), userUnit);
                                    editor.putString(getResources().getString(R.string.userDept), userDept);
                                    editor.apply();
                                    Log.i("Data", "Entered in preferences");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

                preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                        recreate();
                    }
                });
            }

        }
    }


    public void newConcern(View view){

        if (!isInternetAvailable) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.checkYourInternet), Toast.LENGTH_LONG).show();
        }
        else {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.new_concern_type_alert_box_layout, null);

            final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(dialogView)
                    .show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            ConstraintLayout newConcernCL, newGembaWalkCL;
            newConcernCL = dialogView.findViewById(R.id.newSafetyConcernCL);
            newGembaWalkCL = dialogView.findViewById(R.id.newGembaWalkCL);

            newConcernCL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), SafetyConcernActivity.class);
                    startActivity(intent);
                }
            });

            newGembaWalkCL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), GembaWalkActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    public void closeConcerns(View view){

        if (!isInternetAvailable) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.checkYourInternet), Toast.LENGTH_LONG).show();
        }
        else {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.close_concern_type_alert_box_layout, null);

            final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(dialogView)
                    .show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            ConstraintLayout highRisksCL, lowRisksCL;
            highRisksCL = dialogView.findViewById(R.id.highRisksCL);
            lowRisksCL = dialogView.findViewById(R.id.lowRiskskCL);

            highRisksCL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), ViewRisksActivity.class);
                    intent.putExtra("type", ViewRisksActivity.HIGH);
                    startActivity(intent);
                }
            });

            lowRisksCL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), ViewRisksActivity.class);
                    intent.putExtra("type", ViewRisksActivity.LOW);
                    startActivity(intent);
                }
            });
        }
    }

    public void generateReport(View view){

            // csv = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyCs)vFile.csv");
            /*String csv = getApplicationContext().getExternalFilesDir("/MyCsvFile").toString();
            CSVWriter writer = null;
            try {
                writer = new CSVWriter(new FileWriter(csv));

                List<String[]> data = new ArrayList<String[]>();
                data.add(new String[]{"Country", "Capital"});
                data.add(new String[]{"India", "New Delhi"});
                data.add(new String[]{"United States", "Washington D.C"});
                data.add(new String[]{"Germany", "Berlin"});

                writer.writeAll(data); // data is adding to csv

                writer.close();
                //callRead();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            /*HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet firstSheet = workbook.createSheet("Sheet No 1");
            HSSFRow rowA = firstSheet.createRow(0);
            HSSFCell cellA = rowA.createCell(0);
            cellA.setCellValue(new HSSFRichTextString("Sheet One"));
            FileOutputStream fos = null;
            try {
                String str_path = getApplicationContext().getExternalFilesDir("Report").toString();
                File file;
                String name = getString(R.string.app_name) + ".xls";
                file = new File(str_path, name);
                fos = new FileOutputStream(file, false);
                workbook.write(fos);
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Toast.makeText(MainActivity.this, "Excel Sheet Generated", Toast.LENGTH_SHORT).show();*/

            /*final StringBuilder data = new StringBuilder();
            data.append(//"Sr.No., " +
                    "Date, " +
                    "Time, " +
                    "Name of person reporting concern," +
                    "Department, " +
                    "Safety Concern, " +
                    "Location, " +
                    "Department responsible, " +
                    "Priority, " +
                    "Name of person closing the concern, " +
                    "Department of person closing the concern, " +
                    "Cause of concern, " +
                    "Immediate Corrective Action, " +
                    "Permanent Counter measure, " +
                    "Date of closure, " +
                    "Time of closure, " +
                    "Time taken to close concern");

                    int i = 1;
                    data.append("\n" +
                            //finalI + "," +
                            "Today" + "," +
                            "6PM" + "," +
                            "Manan" + "," +
                            "Electrical" + "," +
                            "TV Issue" + "," +
                            "Room 102" + "," +
                            "Electrical" + "," +
                            "High" + "," +
                            "Me" + "," +
                            "Elec" + "," +
                            "PLug" + "," +
                            "New plug" + "," +
                            "New plug" + "," +
                            "Today" + "," +
                            "7PM" + "," +
                            "1 hour");*/

                    /*for(DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getValue(String.class);
                        Log.i("Concern Key", key);

                        DatabaseReference caseRef= FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.allConcerns)).child(key);
                        final int finalI = i;
                        caseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                SafetyConcernClass s = snapshot.getValue(SafetyConcernClass.class);
                                Log.i("Case snapshot", s.toString());

                                Proof p = s.getProof();

                                /*data.append("\n" +
                                        //finalI + "," +
                                        getDate(s.getRepTime()) + "," +
                                        getTime(s.getRepTime()) + "," +
                                        s.getRepBy() + "," +
                                        s.getRepByDept() + "," +
                                        s.getConcern() + "," +
                                        s.getLoc() + "," +
                                        s.getDeptTo() + "," +
                                        s.getPriority() + "," +
                                        p.getClosedBy() + "," +
                                        p.getCloseByDept() + "," +
                                        p.getCause() + "," +
                                        p.getImmAction() + "," +
                                        p.getPermAction() + "," +
                                        getDate(p.getRepTime()) + "," +
                                        getTime(p.getRepTime()) + "," +
                                        getTimeTaken(p.getRepTime(), s.getRepTime()));


                                try{

                                    //FileOutputStream out = openFileOutput("reports.csv", Context.MODE_PRIVATE);
                                    FileOutputStream out = new FileOutputStream("reports.csv", true);
                                    out.write(data.toString().getBytes());
                                    out.close();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }*/

                    /*try{
                        FileOutputStream out = new FileOutputStream(getFilesDir() + "reports.csv");
                        out.write(data.toString().getBytes());
                        out.close();

                        Context context = getApplicationContext();
                        File fileLocation = new File(context.getFilesDir(), "reports.csv");
                        Uri path = FileProvider.getUriForFile(context, "com.example.yellowpages.fileprovider", fileLocation);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("type/csv");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Report");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.putExtra(Intent.EXTRA_STREAM, path);
                        startActivity(Intent.createChooser(intent, "Save to"));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }*/

                /*@Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }*/
            /*NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My Notification");
            builder.setContentTitle("Generating report");
            builder.setContentText("You can continue working while we generate report in the background");
            builder.setSmallIcon(R.drawable.ic_launcher_background);
            builder.setAutoCancel(true);
            //builder.setOngoing(true);

            NotificationManagerCompat compat = NotificationManagerCompat.from(MainActivity.this);
            compat.notify(1, builder.build());*/

            //startService(new JobIntentService(MainActivity.this, GenerateReport.class));

            /*Toast.makeText(getApplicationContext(), "Generating report...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            JobIntentService.enqueueWork(MainActivity.this, GenerateReport.class, 0, intent);*/

            /*String data = "Hello world!";

            File dir = new File(getApplicationContext().getFilesDir(), "reports");
            if(!dir.exists())
                dir.mkdir();

            try{
                File csv = new File(dir, "report");
                FileWriter writer = new FileWriter(csv);
                writer.append(data);
                writer.flush();
                writer.close();
            }catch (Exception e){
                e.printStackTrace();
            }*/

        }

    public void viewReports(View view){
        if (!isInternetAvailable) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.checkYourInternet), Toast.LENGTH_LONG).show();
        }
        else{
            Intent intent = new Intent(getApplicationContext(), ReportGenerationActivity.class);
            startActivity(intent);
        }
    }

    /*public void generateAndDownloadReport(View view){
        GenerateAndDownloadReport url = new GenerateAndDownloadReport();
        url.execute();
    }

    public void downloadReport(View view){
        downloadReportUtil();
    }*/

    public void accountSettings(View view){
        Intent intent = new Intent(MainActivity.this, AccountSettingsActivity.class);
        startActivity(intent);
    }

    public void showUID(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.show_uid_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView uidTV;
        Button copyCode;

        uidTV = dialogView.findViewById(R.id.uidTV);
        copyCode = dialogView.findViewById(R.id.copyCode);

        uidTV.setText(user.getUid());

        copyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("uid", user.getUid());
                manager.setPrimaryClip(data);

                Toast.makeText(getApplicationContext(),"Copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void logOutUser(View view) {

        if (!isInternetAvailable) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.checkYourInternet), Toast.LENGTH_LONG).show();
        }
        else {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Confirm logout?")
                    .setMessage("This would sign you out of this device until you log in back")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(getResources().getString(R.string.userName), "");
                            editor.putString(getResources().getString(R.string.userUnit), "");
                            editor.putString(getResources().getString(R.string.userDept), "");
                            editor.apply();

                            auth.getInstance().signOut();
                            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);

                            //To make sure going back is not possible
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    public void testAnim(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.uploading, null);

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /*LottieAnimationView animationView = dialogView.findViewById(R.id.lottieAnim);
        boolean isAnimating = true;


        animationView.setMinAndMaxProgress(0.0f, 0.5f);

        Toast.makeText(getApplicationContext(), "Paused till upload is complete!", Toast.LENGTH_LONG).show();*/
        //animationView.setMinAndMaxProgress(0.5f, 1.0f);


    }

    public void generateCodeForDeptChange(View view){

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.generate_code_for_dept_change_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        EditText uid;
        Button generateButton;
        ImageButton closeDialog;
        LinearLayout codeContent;
        TextView code;
        ProgressBar bar;

        uid = dialogView.findViewById(R.id.uid);
        generateButton = dialogView.findViewById(R.id.generateCodeButton);
        codeContent = dialogView.findViewById(R.id.codeViewLL);
        code = dialogView.findViewById(R.id.code);
        bar = dialogView.findViewById(R.id.pb);
        closeDialog = dialogView.findViewById(R.id.closeDialog);

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String u = uid.getText().toString();
                if(u.length() > 0){
                    bar.setVisibility(View.VISIBLE);
                    FirebaseDatabase.getInstance().getReference()
                            .child(getResources().getString(R.string.tableName))
                            .child(u).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Random rand = new Random();
                                String newCode = String.format("%04d", rand.nextInt(10000));

                                FirebaseDatabase.getInstance().getReference().child(getResources()
                                        .getString(R.string.deptChangeCodes))
                                        .child(u).setValue(newCode);

                                code.setText(newCode);
                                codeContent.setVisibility(View.VISIBLE);
                                bar.setVisibility(View.GONE);
                                generateButton.setEnabled(false);

                            }
                            else {
                                bar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Invalid UID! Please enter a valid UID", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please fill the UID field!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void changeAdmin(View view){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.show_uid_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(dialogView)
                .show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText email, password;
        Button authenticateButton;
        ConstraintLayout authenticateCL, newAdminUIDCL;
        ProgressBar barAuth, barNewAdmin;

        email = dialogView.findViewById(R.id.email);
        password = dialogView.findViewById(R.id.password);
        authenticateButton = dialogView.findViewById(R.id.deleteButton);
        authenticateCL = dialogView.findViewById(R.id.authenticateCL);
        newAdminUIDCL = dialogView.findViewById(R.id.newAdminUIDCL);
        barAuth = dialogView.findViewById(R.id.authPB);
        barNewAdmin = dialogView.findViewById(R.id.newAdminPB);


        authenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String e = email.getText().toString(), p = password.getText().toString();

                if(validateEmail(e) && password.length() > 0){
                    barAuth.setVisibility(View.VISIBLE);
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    AuthCredential credential = EmailAuthProvider.getCredential(e, p);

                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.i("TAG", "onComplete: authentication complete");

                                        authenticateCL.setVisibility(View.GONE);
                                        newAdminUIDCL.setVisibility(View.VISIBLE);

                                        EditText newAdminUID = dialogView.findViewById(R.id.uidOfNewAdmin);
                                        Button makeAdminButton = dialogView.findViewById(R.id.makeAdminButton);

                                        makeAdminButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                barNewAdmin.setVisibility(View.VISIBLE);
                                                String newUID = newAdminUID.getText().toString();

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child(getResources().getString(R.string.tableName))
                                                        .child(newUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            FirebaseDatabase.getInstance().getReference()
                                                                    .child(getResources().getString(R.string.adminUID))
                                                                    .setValue(newUID);

//                                                            adminSettingsCL.setVisibility(View.GONE);
                                                            dialog.dismiss();
                                                            Toast.makeText(getApplicationContext(), "Admin account changed successfully!", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else {
                                                            barNewAdmin.setVisibility(View.GONE);
                                                            Toast.makeText(getApplicationContext(), "Invalid UID, please try again!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        });

                                    }
                                    else {
                                        barAuth.setVisibility(View.GONE);
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

    private boolean validateEmail(String email){
        if (TextUtils.isEmpty(email))
            return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /*class GenerateAndDownloadReport extends AsyncTask<String, String, Boolean> {

        Context context = MainActivity.this;

        @Override
        protected Boolean doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(generateReportURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

//                Log.i("Header", connection.getHeaderFields().toString());
                int responseCode = connection.getResponseCode();

                return responseCode == 200;


            } catch (Exception e) {
                Log.e("Error: ", e.toString());
                return false;
            }
        }

    }*/

    /*class GenerateAndDownloadReport extends AsyncTask<Void, String, Boolean> {

        Context context = MainActivity.this;
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.sending_report_request, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setView(dialogView);

        AlertDialog dialog = builder.create();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            Snackbar.make(rootView, "Sending report generation request...", Snackbar.LENGTH_LONG).show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            int count;
            try {
                URL url = new URL(generateReportURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

//                Log.i("Header", connection.getHeaderFields().toString());
                int responseCode = connection.getResponseCode();

                return responseCode == 200;


            } catch (Exception e) {
                Log.e("Error: ", e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean){
                dialog.dismiss();
                FirebaseDatabase.getInstance().getReference()
                        .child(getResources().getString(R.string.reportLastGeneratedTimestamp))
                        .setValue(ServerValue.TIMESTAMP);

                FirebaseDatabase.getInstance().getReference()
                        .child(getResources().getString(R.string.reportLastGeneratedTimestamp))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Long timestamp = snapshot.getValue(Long.class);
                                String time = String.valueOf(timestamp);
                                preferences.edit().putString(getResources().getString(R.string.reportLastGeneratedTimestamp), time).apply();
                                Log.i("Last time", preferences.getString(getResources().getString(R.string.reportLastGeneratedTimestamp), ""));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                downloadReportUtil();
            }
            else {
                Snackbar.make(rootView, "Request could not be completed. Please try again later!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void downloadReportUtil(){
        Snackbar.make(rootView, "Request completed. Downloading report...", Snackbar.LENGTH_LONG).show();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(reportDownloadURL));
        request.setDescription("Report download in progress");
        request.setTitle("Report");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Report.zip");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private String getDate(Object time) {
        Date date = new Date((Long) time);
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
        return sdfDate.format(date);
    }

    private String getTime(Object time) {
        Date date = new Date((Long) time);
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(date);
    }

    private String getTimeTaken(Object t1, Object t2) {
        long diff = (Long) t1 - (Long) t2;
        return String.valueOf(diff);
    }*/
}