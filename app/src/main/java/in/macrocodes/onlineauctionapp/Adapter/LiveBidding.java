package in.macrocodes.onlineauctionapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import in.macrocodes.onlineauctionapp.Models.BiddingModal;
import in.macrocodes.onlineauctionapp.ProductInfoActivity;
import in.macrocodes.onlineauctionapp.R;

public class LiveBidding extends RecyclerView.Adapter<LiveBidding.Viewholder> {
    List<BiddingModal> biddingList = new ArrayList<>();
    Context mContext;
    public LiveBidding(ProductInfoActivity productInfoActivity, List<BiddingModal> biddingList) {
        this.mContext = productInfoActivity;
        this.biddingList=biddingList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_bid,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

        BiddingModal biddingModal = biddingList.get(position);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(biddingModal.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                holder.bidTxt.setText(name+" bidded for Rs "+biddingModal.getBid());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return biddingList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public static class Viewholder extends RecyclerView.ViewHolder{

        private TextView bidTxt;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            bidTxt = (TextView) itemView.findViewById(R.id.bidtxt);
        }
    }
}
