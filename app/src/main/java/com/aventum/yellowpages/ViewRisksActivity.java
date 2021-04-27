package com.aventum.yellowpages;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static com.aventum.yellowpages.MainActivity.userDept;

public class ViewRisksActivity extends AppCompatActivity {

    RecyclerView risksRV;
    LinearLayout noConcersFoundLL;
//    ConstraintLayout noConcersFoundCL;
    TextView riskPriorityTV;
    //FirebaseRecyclerPagingAdapter<RiskItem, RiskItemHolder> adapter;
    FirebaseRecyclerPagingAdapter<String, RiskItemHolder> adapter;

    public static final String HIGH = "High", LOW = "Low";
    private final String SECURITY = "Security";
    Query query;
    View mainView;

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_risks);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        mainView = findViewById(R.id.mainView);
        riskPriorityTV = findViewById(R.id.riskPriorityTV);
        noConcersFoundLL = findViewById(R.id.noConcernsFoundLL);
//        noConcersFoundCL = findViewById(R.id.noConcernsFoundCL);


        Intent intent = getIntent();
        final String type = intent.getStringExtra("type");

        if(type.equals(HIGH))
            riskPriorityTV.setText(R.string.viewHighRisks);
        else
            riskPriorityTV.setText(R.string.viewLowRisks);

        risksRV = findViewById(R.id.risksRV);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
//        manager.setStackFromEnd(true);
//        manager.setReverseLayout(true);
        risksRV.setLayoutManager(manager);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = LayoutInflater.from(this).inflate(R.layout.loading_risks_alert_layout, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        //dialog.show();

        //Query query = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.concerns)).child(getResources().getString(R.string.active));

        query = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.deptWiseConcerns)).child(userDept).child(type);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        DatabasePagingOptions<String> options = new DatabasePagingOptions.Builder<String>()
                .setLifecycleOwner(this)
                .setQuery(query, config, String.class)
                .build();

        adapter = new FirebaseRecyclerPagingAdapter<String, RiskItemHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RiskItemHolder holder, int position, @NonNull String c) {
                holder.setKey(c, adapter.getRef(position).getKey(), type, ViewRisksActivity.this);
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state){
                    case LOADING_INITIAL:
                        dialog.show();
                        break;
                    case LOADED:
                        dialog.dismiss();
                        break;
                    case ERROR:
                        dialog.dismiss();
                        noConcersFoundLL.setVisibility(View.VISIBLE);
//                        noConcersFoundCL.setVisibility(View.VISIBLE);
                        risksRV.setVisibility(View.GONE);
                        //Toast.makeText(getApplicationContext(), "No concerns found", Toast.LENGTH_SHORT).show();
                        /*Snackbar snackbar = Snackbar.make(mainView, "No active concerns found!", BaseTransientBottomBar.LENGTH_INDEFINITE)
                                .setAction("Go back", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        finish();
                                    }
                                });
                        snackbar.show();*/
                        break;
                }

            }

            @NonNull
            @Override
            public RiskItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.risk_item_layout, parent, false);
                return new RiskItemHolder(view);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }
        };

        risksRV.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void closeActivity(View view){
        finish();
    }
}