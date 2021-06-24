package in.macrocodes.onlineauctionapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.macrocodes.onlineauctionapp.Models.Products;
import in.macrocodes.onlineauctionapp.ProductInfoActivity;
import in.macrocodes.onlineauctionapp.R;


public class MyCampaignAdapter extends RecyclerView.Adapter<MyCampaignAdapter.Viewholder> {
    Context context;
    List<Products> myProducts = new ArrayList<>();
    List<String>imageUrl = new ArrayList<>();
    public MyCampaignAdapter(Context context, List<Products> myProducts) {
        this.context = context;
        this.myProducts=myProducts;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_camp_layout,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

        Products productModel = myProducts.get(position);
        holder.pName.setText(productModel.getName());
        holder.pdesc.setText("Rs "+productModel.getBid());
        getPData(productModel.getName(),holder.pImage);

        if (productModel.getStatus().equalsIgnoreCase("stop")){

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products").child(productModel.getName());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child("winner").hasChild("uid")){
                        String uid = Objects.requireNonNull(snapshot.child("winner").child("uid").getValue()).toString();
                        String ammount = Objects.requireNonNull(snapshot.child("winner").child("bid").getValue()).toString();

                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                        reference1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                String email = Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                                String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();

                                holder.bidstatus.setText("Product sold to "+name+" at Rs "+ammount+"\nContact them at - "+email);

                            }

                            @Override
                            public void onCancelled(@NonNull  DatabaseError error) {

                            }
                        });


                    }else{
                        holder.bidstatus.setText("Bidding is over.\nNo one bidded for your product");
                    }
                }

                @Override
                public void onCancelled(@NonNull  DatabaseError error) {

                }
            });

        }else{
            holder.bidstatus.setText("Campaign is currently running");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductInfoActivity.class);
                intent.putExtra("pname",productModel.getName());
                intent.putExtra("pdesc",productModel.getDescription());
                intent.putExtra("prate",productModel.getBid());
                intent.putExtra("uid",productModel.getUid());
                intent.putExtra("status",productModel.getStatus());
                intent.putExtra("mine","mine");
                context.startActivity(intent);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAt(position);
                DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Products").child(productModel.getName());
                dR.removeValue();
            }
        });

    }
    public void removeAt(int position) {

        myProducts.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, myProducts.size());
    }
    @Override
    public int getItemCount() {
        return myProducts.size();
    }

    private void deleteProduct(){

    }



    private void getPData(String pname, ImageView holder){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products");
        reference.child(pname).child("Images").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String img = Objects.requireNonNull(snapshot.child("image0").getValue()).toString();

                Glide.with(Objects.requireNonNull(context))
                        .load(img)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(R.drawable.default_send_image)
                        .into(holder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
    public static class Viewholder extends RecyclerView.ViewHolder{

        private ImageView pImage,delete;
        private TextView pName,pdesc,bidstatus;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            pName = (TextView) itemView.findViewById(R.id.pname);
            pdesc = (TextView) itemView.findViewById(R.id.pdesc);
            pImage = (ImageView) itemView.findViewById(R.id.pimage);
            delete = (ImageView) itemView.findViewById(R.id.delete);
            bidstatus = (TextView) itemView.findViewById(R.id.bidStatus);
        }
    }
}
