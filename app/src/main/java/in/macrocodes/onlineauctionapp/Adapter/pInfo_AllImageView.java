package in.macrocodes.onlineauctionapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import in.macrocodes.onlineauctionapp.ProductInfoActivity;
import in.macrocodes.onlineauctionapp.R;

public class pInfo_AllImageView extends RecyclerView.Adapter<pInfo_AllImageView.Viewholder> {
    Context context;
    ImageView pimageView;
    int current_pos,pre_pos;
    List<String> imageList = new ArrayList<>();
    public pInfo_AllImageView(ProductInfoActivity productInfoActivity, List<String> imageList, ImageView pimageView) {
        this.context=productInfoActivity;
        this.imageList=imageList;
        this.pimageView=pimageView;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_view_imy_lay,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {


       if (position!=0){
           pre_pos=position-1;
       }


        Glide.with(Objects.requireNonNull(context))
                .load(imageList.get(position))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .placeholder(R.drawable.default_send_image)
                .into(holder.imageView);



        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageViewAnimatedChange(context,pimageView,imageList.get(position));
            }
        });
    }

    public static void ImageViewAnimatedChange(Context c, final ImageView v, final String new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                Glide.with(Objects.requireNonNull(c))
                        .load(new_image)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(R.drawable.default_send_image)
                        .into(v);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }
    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder{

        private ImageView imageView;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);

        }
    }
}
