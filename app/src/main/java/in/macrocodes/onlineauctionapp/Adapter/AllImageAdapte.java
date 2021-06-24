package in.macrocodes.onlineauctionapp.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;
import in.macrocodes.onlineauctionapp.PathUtils;
import in.macrocodes.onlineauctionapp.PathUtilvideo;
import in.macrocodes.onlineauctionapp.R;
import in.macrocodes.onlineauctionapp.addProductforBid;

public class AllImageAdapte extends RecyclerView.Adapter<AllImageAdapte.Viewholder> {
    List<Uri> images = new ArrayList<>();
    StorageReference mImageStorage;
    int pos;
    Context mContext;
    public AllImageAdapte(addProductforBid addProductforBid, List<Uri> images) {
        this.images = images ;
         mContext = addProductforBid;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_lay,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

        pos = position;
        Uri uri = images.get(position);
        Glide.with(mContext)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.default_send_image)
                .into(holder.imageView);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAt(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void removeAt(int position) {
        images.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, images.size());
    }

    public void uploadImages(String name){

        mImageStorage = FirebaseStorage.getInstance().getReference();
       DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();


        final String push_id = mRootRef.child("Products").push().getKey();
        final StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");


        int count=0;

        for (int i =0;i<images.size();i++){
            final Map<String, Object> messageMap = new HashMap<String, Object>();
            messageMap.put("image"+i, "default");



            mRootRef.child("Products").child(name).child("Images").updateChildren(messageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {



                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });


            String fileRealPath = null;
            if(Build.VERSION.SDK_INT < 26){
                try {

                    fileRealPath= PathUtils.getPath(mContext,images.get(i));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    fileRealPath = PathUtilvideo.getPath(mContext,images.get(i));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            assert fileRealPath != null;
            Bitmap thumb_bitmap = new Compressor(mContext)
                    .setMaxWidth(400)
                    .setMaxHeight(400)
                    .compressToBitmap(new File(fileRealPath));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            final byte[] thumb_byte = baos.toByteArray();

            UploadTask uploadTask2 = filepath.putBytes(thumb_byte);


            int finalCount = count;
            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                String download_url = Objects.requireNonNull(task.getResult()).toString();


                                DatabaseReference updateData = FirebaseDatabase.getInstance().getReference()
                                        .child("Products").child(name).child("Images");

                                updateData.child("image"+ finalCount).setValue(download_url);


                                Toast.makeText(mContext, "Campaign will start soon", Toast.LENGTH_SHORT).show();


                            }
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {


                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress2 = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                }
            });

            count++;
        }
    }

    public static class Viewholder extends RecyclerView.ViewHolder{

        public ImageView imageView,delete;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.imageView);
            delete = (ImageView)itemView.findViewById(R.id.delete);
        }
    }

    public String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }
}
