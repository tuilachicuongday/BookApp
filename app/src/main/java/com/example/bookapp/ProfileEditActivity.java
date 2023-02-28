package com.example.bookapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookapp.databinding.ActivityProfileBinding;
import com.example.bookapp.databinding.ActivityProfileEditBinding;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {
    private ActivityProfileEditBinding binding;
    private FirebaseAuth firebaseAuth;
    private Uri imgUri = null;
    private  static  final  String TAG = "EDIT_PROFILE_TAG";
    private String name = "";
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui Lòng Đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();


        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImgAttachMenu();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });



    }

    private void validateData() {
        //get
        name = binding.nameEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(name)){
            Toast.makeText(this,"Nhập Tên",Toast.LENGTH_SHORT).show();
        }else
            //ten da nhap con` img null
        {
            if (imgUri == null)
            {
                updateProfile("");
            }
            else {
                updaloadImg();
            }
        }
    }

    private void updateProfile(String imgUrl) {
        Log.d(TAG, "updateProfile:Updating");
        progressDialog.setMessage("Đang Cập Nhật");
        progressDialog.show();

        //set data update lên db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("name",""+name);
        if (imgUri != null)
        {
            hashMap.put("profileImage", "" + imgUrl);
        }

        //update
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this,"Update thành công ",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileEditActivity.this,"Update lên firebase Thất bại",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updaloadImg() {
        Log.d(TAG,"updaloadImg : Uploading");
        progressDialog.setMessage("Đang Cập Nhật Ảnh");
        progressDialog.show();


        String filePathAndName = "ProfileImages/" + firebaseAuth.getUid();

        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG,"onSuccess : Uploaded");

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadImageUrl = "" + uriTask.getResult();

                Log.d(TAG,"onSuccess : Uploaded Img Url" +uploadImageUrl );
                updateProfile(uploadImageUrl);
            }
        }).
                addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"onSuccess : Upload Fail" + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(ProfileEditActivity.this,"Không Thể Tải Hình Lên " + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImgGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivity.launch(intent);
    }

    private void pickImgCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Chọn Hình Mới");
//        values.put(MediaStore.Images.Media.DESCRIPTION,"Des");
        imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
        cameraActivity.launch(intent);
    }

    private void loadUserInfo() {
        Log.d(TAG,"loadUserInfo:Edit" + firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = "" +snapshot.child("name").getValue();
                String profileImg = "" +snapshot.child("profileImage").getValue();

                //set du lieu
                binding.nameEt.setText(name);


                //set img
                Glide.with(ProfileEditActivity.this)
                        .load(profileImg)
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private ActivityResultLauncher<Intent> cameraActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            //get uri cua img
            if (result.getResultCode() == Activity.RESULT_OK){
                Log.d(TAG,"onActivityResult:Camera" + imgUri);
                Intent data = result.getData();
                binding.profileImage.setImageURI(imgUri);
            }
            else
            {
                Toast.makeText(ProfileEditActivity.this,"Đã Hủy" , Toast.LENGTH_SHORT).show();
            }
        }
    });

    private ActivityResultLauncher<Intent> galleryActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG,"onActivityResult" + imgUri);

                        Intent data = result.getData();
                        imgUri = data.getData();
                        Log.d(TAG,"onActivityResult:Gallery" + imgUri);

                        binding.profileImage.setImageURI(imgUri);
                    }
                    else
                    {
                        Toast.makeText(ProfileEditActivity.this,"Đã Hủy" , Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void showImgAttachMenu() {
        PopupMenu popupMenu = new PopupMenu(this,binding.profileImage);
        popupMenu.getMenu().add(Menu.NONE, 0,0,"Camera");
        popupMenu.getMenu().add(Menu.NONE, 1,1,"Thư Viện Ảnh");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int pick = item.getItemId();
                if(pick ==0)
                {
                    pickImgCamera();
                }
                else if (pick == 1) //
                {
                    pickImgGallery();
                }

                return false;
            }
        });

    }

}