package in.macrocodes.onlineauctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.macrocodes.onlineauctionapp.Adapter.MyCampaignAdapter;
import in.macrocodes.onlineauctionapp.Models.Products;

public class MyCampaigns extends AppCompatActivity {
    MyCampaignAdapter mAdapter;
    private RecyclerView mRecyclerView;
    List<Products> myProducts = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_campaigns);
        mRecyclerView = (RecyclerView) findViewById(R.id.myCamp);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new MyCampaignAdapter(MyCampaigns.this,myProducts);
        mRecyclerView.setAdapter(mAdapter);
        getMyCampaign();
    }

    private void getMyCampaign(){

        myProducts.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    reference.child(Objects.requireNonNull(dataSnapshot.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Products productModel = snapshot.getValue(Products.class);
                            assert productModel != null;
                            if (productModel.getUid().equalsIgnoreCase(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                                myProducts.add(productModel);
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}