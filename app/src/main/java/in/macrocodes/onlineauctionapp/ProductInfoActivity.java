package in.macrocodes.onlineauctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import in.macrocodes.onlineauctionapp.Adapter.LiveBidding;
import in.macrocodes.onlineauctionapp.Adapter.pInfo_AllImageView;
import in.macrocodes.onlineauctionapp.Models.BiddingModal;

public class ProductInfoActivity extends AppCompatActivity {
    String name,desc,bid,uid,mine,status;
    RecyclerView allImageView;
    pInfo_AllImageView mAdapter;
    TextView pname,pdesc,sellername;
    ImageView imageView;
    TextView rate,sellerbidViewname,sellercity;
    CircleImageView sellerImage;
    Button bidNow;
    List<String> imageList = new ArrayList<>();
    RecyclerView bidView;

    List<BiddingModal> biddingList = new ArrayList<>();
    EditText bidtext;
    LiveBidding mAdapter2;
    Button bidBtn;

    TextView title,sold;
    LinearLayout biddingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);
        allImageView = (RecyclerView) findViewById(R.id.allImageView);

        sellername = (TextView) findViewById(R.id.SellerName);
        sellercity = (TextView) findViewById(R.id.SellerCity);
        sellerImage = (CircleImageView) findViewById(R.id.sellerProfile);
        title = (TextView) findViewById(R.id.title);
        sold = (TextView) findViewById(R.id.sold);
        biddingLayout = (LinearLayout) findViewById(R.id.bidLayout);

        bidBtn = (Button) findViewById(R.id.bidbtn);
        bidtext = (EditText) findViewById(R.id.bidtxt);
        bidView =(RecyclerView) findViewById(R.id.bidView);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        imageView = (ImageView) findViewById(R.id.pImage);
        rate = (TextView) findViewById(R.id.pbid);
        pname = (TextView) findViewById(R.id.pname);
        pdesc = (TextView) findViewById(R.id.pdesc);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        allImageView.setLayoutManager(linearLayoutManager);
        mAdapter = new pInfo_AllImageView(ProductInfoActivity.this,imageList,imageView);
        allImageView.setAdapter(mAdapter);



        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);


        bidView.setLayoutManager(linearLayoutManager2);
        mAdapter2 = new LiveBidding(ProductInfoActivity.this,biddingList);
        bidView.setAdapter(mAdapter2);



        getPreInfo();
        getSellerInfo();
        getData();
        bidStart();
        getBidding();


        if (status.equalsIgnoreCase("stop")){
            biddingLayout.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            sold.setVisibility(View.VISIBLE);
            sold.setText("Bidding is over for this product");
        }


    }

    private void getBidding(){
        biddingList.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products")
                .child(name).child("bidding");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        reference.child(Objects.requireNonNull(dataSnapshot.getKey())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                BiddingModal biddingModal = snapshot.getValue(BiddingModal.class);
                                biddingList.add(biddingModal);
                                bidView.scrollToPosition(biddingList.size() - 1);
                                mAdapter2.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void bidStart(){
        bidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!uid.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    String b = bidtext.getText().toString().trim();
                    if (!b.isEmpty()){

                        if (Integer.parseInt(b)>Integer.parseInt(bid)){

                            bidtext.setText("");

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products")
                                    .child(name).child("bidding");
                            String puch_id = reference.push().getKey();
                            HashMap<Object,String>hashMap = new HashMap<>();
                            hashMap.put("bid",b);
                            hashMap.put("uid", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            assert puch_id != null;
                            reference.child(puch_id).setValue(hashMap);
                        }else{
                            Toast.makeText(ProductInfoActivity.this, "Bidding ammount must be greator than the product value", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ProductInfoActivity.this, "Please enter a bidding amount", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(ProductInfoActivity.this, "You can't bid in your own product", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void getSellerInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String profile = snapshot.child("image").getValue().toString();
                String city = snapshot.child("city").getValue().toString();

                sellername.setText(name);
                sellercity.setText("From "+city);

                Glide.with(Objects.requireNonNull(ProductInfoActivity.this))
                        .load(profile)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(R.drawable.default_avatar)
                        .into(sellerImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void getData(){
        imageList.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products").child(name).child("Images");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i=0;i<snapshot.getChildrenCount();i++){
                    imageList.add(Objects.requireNonNull(snapshot.child("image" + i).getValue()).toString());
                }
                Glide.with(ProductInfoActivity.this)
                        .load(imageList.get(0))
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(R.drawable.default_send_image)
                        .into(imageView);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getPreInfo(){
        name = getIntent().getStringExtra("pname");
        desc = getIntent().getStringExtra("pdesc");
        bid = getIntent().getStringExtra("prate");
        uid = getIntent().getStringExtra("uid");
        status = getIntent().getStringExtra("status");
        mine = getIntent().getStringExtra("mine");
        if (mine!=null){
           // bidNow.setText("View Bidding of Your Product");
        }

        pname.setText(name);
        pdesc.setText(desc);
        rate.setText("Bidding Starts at Rs "+bid);
    }
}