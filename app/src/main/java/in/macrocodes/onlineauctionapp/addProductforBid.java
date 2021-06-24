package in.macrocodes.onlineauctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import in.macrocodes.onlineauctionapp.Adapter.AdapterClass;
import in.macrocodes.onlineauctionapp.Adapter.AllImageAdapte;

public class addProductforBid extends AppCompatActivity {

    EditText pName,pDesc,pBid,pDays;
    Button addProduct,addImage;
    String name,desc,bid,days;
    DatabaseReference reference;
    private static final int GALLERY = 1;
    String fileRealPath;
    RecyclerView allImagesView;
    AllImageAdapte mAdapter;
    String uid;
    List<Uri> images = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_productfor_bid);
        pName = (EditText) findViewById(R.id.productName);
        pDesc = (EditText) findViewById(R.id.description);
        pBid = (EditText) findViewById(R.id.bid);
        pDays = (EditText) findViewById(R.id.days);
        addProduct = (Button) findViewById(R.id.addProduct);
        addImage = (Button) findViewById(R.id.addImage);
        allImagesView = (RecyclerView) findViewById(R.id.Allimages);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true);
        linearLayoutManager.setStackFromEnd(true);
        allImagesView.setLayoutManager(linearLayoutManager);

        mAdapter = new AllImageAdapte(addProductforBid.this,images);
        allImagesView.setAdapter(mAdapter);



        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProduct();
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Select File"), GALLERY);
            }
        });


    }

    private void addProduct() {

        name = pName.getText().toString();
        desc = pDesc.getText().toString();
        bid = pBid.getText().toString();
        days = pDays.getText().toString().trim();

        reference = FirebaseDatabase.getInstance().getReference().child("Products");
        String push_id = FirebaseDatabase.getInstance().getReference().child("Products").push().getKey();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String expireDate = null;
        String todayDate = formatter.format(date);
        Calendar c = Calendar.getInstance();

        try {
            c.setTime(formatter.parse(todayDate));
            c.add(Calendar.DATE, Integer.parseInt(days));  // number of days to add
            expireDate = formatter.format(c.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }



        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("description", desc);
        userMap.put("bid", bid);
        userMap.put("days", days);
        userMap.put("winner", "default");
        userMap.put("status", "running");
        userMap.put("timestamp", expireDate);
        //add timestamp
        userMap.put("uid", uid);




        assert push_id != null;
        reference.child(name).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                reference.child(name).child("Images").child("image0").setValue("default");
                Toast.makeText(addProductforBid.this, "Your product is added for bid", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        mAdapter.uploadImages(name);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                final Uri imageUri = data.getData();
                images.add(imageUri);
                mAdapter.notifyDataSetChanged();


            } else if (data.getClipData() != null) {
                int total = data.getClipData().getItemCount();

                for (int i = 0; i < total; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                   // multipleUri.add(imageUri);


                }
            }
        }
}
}