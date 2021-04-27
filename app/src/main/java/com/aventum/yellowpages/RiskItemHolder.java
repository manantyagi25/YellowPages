package com.aventum.yellowpages;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RiskItemHolder extends RecyclerView.ViewHolder {

    TextView riskNameTV, riskLocationTV;
    View view;
    Context context;

    public String concernKey, refKey, priority;

    public RiskItemHolder(@NonNull View itemView) {
        super(itemView);

        this.view = itemView;
        this.riskNameTV = itemView.findViewById(R.id.issueName);
        this.riskLocationTV = itemView.findViewById(R.id.issueLocation);
    }

    public void setKey(String caseKey, String refKey, String priority, Context c){
        this.context = c;
        this.concernKey = caseKey;
        this.refKey = refKey;
        this.priority = priority;

        itemView.setOnClickListener(detailsListener);

        Log.i("Active Lists Key in Holder", refKey);
        Log.i("Concern Key in Holder", concernKey);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(context.getResources().getString(R.string.allConcerns)).child(concernKey);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RiskItem riskItem = snapshot.getValue(RiskItem.class);
                riskNameTV.setText(riskItem.getConcern());
                riskLocationTV.setText(riskItem.getLoc());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    View.OnClickListener detailsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, ViewRiskDetailsActivity.class);
            intent.putExtra(ViewRiskDetailsActivity.CONCERN_KEY, concernKey);
            intent.putExtra(ViewRiskDetailsActivity.REF_KEY, refKey);
            intent.putExtra(ViewRiskDetailsActivity.PRIORITY, priority);
            context.startActivity(intent);
        }
    };

}
