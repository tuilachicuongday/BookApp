package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookapp.adapters.AdapterPdfFavorite;
import com.example.bookapp.databinding.ActivityProfileBinding;
import com.example.bookapp.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private static final String TAG = "PROFILE_TAG";
    private ProgressDialog progressDialog;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfFavorite adapterPdfFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.accountTypeTv.setText("null");
        binding.memberDate.setText("null");
        binding.favoriteBookCountTv.setText("null");
        binding.accountStatusTv.setText("null");


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        loadUserInfo();
        loadFavoriteBooks();


        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,ProfileEditActivity.class));
            }
        });


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.accountStatusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseUser.isEmailVerified()){
                    Toast.makeText(ProfileActivity.this,"Đã Xác Minh",Toast.LENGTH_SHORT).show();
                }else
                {
                    emailVerificationDialog();
                }
            }
        });
    }

    private void emailVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác minh Email")
                .setMessage("Bạn có muốn gửi mã xác minh đến email " + firebaseUser.getEmail() + "không?")
                .setPositiveButton("Gửi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmailVerification();
                    }
                }).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void sendEmailVerification() {
        progressDialog.setMessage("Đang gửi mã xác minh đến " + firebaseUser.getEmail());
        progressDialog.show();

        firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this,"Đã gửi mã xác minh đến email" + firebaseUser.getEmail(),Toast.LENGTH_SHORT).show();
                // ->Loaduserinfo
            }
        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this,"Không thể gửi" +e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavoriteBooks() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()){
                    String bookId = "" + ds.child("bookId").getValue();

                    ModelPdf modelPdf = new ModelPdf();
                    modelPdf.setId(bookId);

                    pdfArrayList.add(modelPdf);
                }
                binding.favoriteBookCountTv.setText(""+pdfArrayList.size());
                //set adapter
                adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this,pdfArrayList);
                //set adapter Rcv
                binding.booksRv.setAdapter(adapterPdfFavorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private void loadUserInfo() {
        Log.d(TAG,"loadUserInfo" + firebaseAuth.getUid());

        if(firebaseUser.isEmailVerified())
        {
            binding.accountStatusTv.setText("Đã xác minh");

        }else{
            binding.accountStatusTv.setText("Chưa xác minh");
        }


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get tat ca du lieu cua user
                String email = "" +snapshot.child("email").getValue();
                String name = "" +snapshot.child("name").getValue();
                String profileImg = "" +snapshot.child("profileImage").getValue();
                String timestamp = "" +snapshot.child("timestamp").getValue();
                String uid = "" +snapshot.child("uid").getValue();
                String userType = "" +snapshot.child("userType").getValue();
                //format date dd//MM/yyyy
                String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                //set du lieu
                binding.emailTv.setText(email);
                binding.nameTv.setText(name);
                binding.memberDate.setText(formattedDate);
                binding.accountTypeTv.setText(userType);

                //set img
                Glide.with(ProfileActivity.this)
                        .load(profileImg)
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}