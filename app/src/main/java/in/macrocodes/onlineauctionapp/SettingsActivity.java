package in.macrocodes.onlineauctionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profile;
    TextView username;
    RelativeLayout logout;
    private static final int GALLERY = 1;
    private ProgressDialog mRegProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        profile = (CircleImageView) findViewById(R.id.p_profile);
        username = (TextView) findViewById(R.id.p_name);
        logout = (RelativeLayout) findViewById(R.id.logout);

        mRegProgress = new ProgressDialog(this);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegProgress.setTitle("Logging Out");
                mRegProgress.setMessage("Please wait while we create your account !");
                mRegProgress.setCanceledOnTouchOutside(false);
                mRegProgress.show();

                Handler handler =new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent =new Intent(SettingsActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },2000);

            }
        });


        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Select File"), GALLERY);

            }
        });
        getUserData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                final Uri imageUri = data.getData();
                uploadImage(imageUri);
            }
        }
    }


    public void uploadImage(Uri uri){
        StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();


        String current_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference filepath = mImageStorage.child("profile_images").child(current_uid + ".jpg");







        String fileRealPath = null;
        if(Build.VERSION.SDK_INT < 26){
            try {

                fileRealPath= PathUtils.getPath(SettingsActivity.this,uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }else{
            try {
                fileRealPath = PathUtilvideo.getPath(SettingsActivity.this,uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        assert fileRealPath != null;
        Bitmap thumb_bitmap = new Compressor(SettingsActivity.this)
                .setMaxWidth(200)
                .setMaxHeight(200)
                .compressToBitmap(new File(fileRealPath));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        final byte[] thumb_byte = baos.toByteArray();

        UploadTask uploadTask2 = filepath.putBytes(thumb_byte);



        uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            String download_url = Objects.requireNonNull(task.getResult()).toString();


                            DatabaseReference updateData = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child(current_uid);

                            updateData.child("image").setValue(download_url);

                            Glide.with(SettingsActivity.this)
                                    .load(uri)
                                    .centerCrop()
                                    .placeholder(R.drawable.default_send_image)
                                    .into(profile);

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




    }
    public void getUserData(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                username.setText(name);
                String image = snapshot.child("image").getValue().toString();

                if (!image.equalsIgnoreCase("default")){

                    Glide.with(SettingsActivity.this)
                            .load(image)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .centerCrop()
                            .placeholder(R.drawable.default_send_image)
                            .into(profile);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}