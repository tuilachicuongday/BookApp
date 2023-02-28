package com.example.bookapp.PDF;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapters.AdapterComment;
import com.example.bookapp.adapters.AdapterPdfFavorite;
import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.example.bookapp.databinding.DialogCmtAddBinding;
import com.example.bookapp.models.ModelComment;
import com.example.bookapp.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;
    private  static  final String TAG_DOWNLOAD = "PDF_DOWNLOAD_TAG";
    String bookId , bookTitle , bookUrl;

    private ProgressDialog progressDialog;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;

    boolean isIntMyFavorite = false;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        //progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui Lòng Đợi");
        progressDialog.setCanceledOnTouchOutside(false);


        //hide download button, can` bookUrl de load sau funtion loadBookDetails()
        binding.downloadBookBtn.setVisibility(View.GONE);


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null)
        {
            checkIsFavorite();
        }

        loadBookDetails();
        loadComments();
        MyApplication.bookViewCount(bookId);


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                i.putExtra("bookId",bookId);
                startActivity(i);
            }
        });

        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD,"onClick:Kiem tra Quyen");
                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {

                    Log.d(TAG_DOWNLOAD,"onClick:Kiem tra Quyen Thanh Cong");
                    MyApplication.downloadBook(
                            PdfDetailActivity.this,
                            "" + bookId,
                            "" +bookTitle,
                            "" +bookUrl);
                }
                else {
                    Log.d(TAG_DOWNLOAD,"onClick:Kiem tra Quyen That Bai,xin lai quyen");
                    requestPermisson.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    Toast.makeText(PdfDetailActivity.this,"Bạn Chưa Đăng Nhập",Toast.LENGTH_SHORT).show();
                }
                else
                    {
                    if(isIntMyFavorite) {
                        //ton tai => xoa
                        MyApplication.removeFromFavorite(PdfDetailActivity.this,bookId);
                    }
                    else{
                        // k ton tai => them
                        MyApplication.addToFavorite(PdfDetailActivity.this,bookId);
                    }
                }
            }
        });

        binding.addCmtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    Toast.makeText(PdfDetailActivity.this,"Bạn Chưa Đăng Nhập",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    addCommentDialog();
                }
            }
        });

    }

    private void loadComments() {
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .child("Comments")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentArrayList.add(modelComment);
                }
                adapterComment = new AdapterComment(PdfDetailActivity.this,commentArrayList);
                binding.commentsRv.setAdapter(adapterComment);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String comment = "";
    private void addCommentDialog() {
        DialogCmtAddBinding cmtAddBinding = DialogCmtAddBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(cmtAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        cmtAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        cmtAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment = cmtAddBinding.commentEt.getText().toString().trim();

                if (TextUtils.isEmpty(comment)){
                    Toast.makeText(PdfDetailActivity.this,"Nhập Bình Luận",Toast.LENGTH_SHORT).show();
                }
                else{
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });

    }

    private void addComment() {
        progressDialog.setMessage("Đang tải bình luận của bạn");
        progressDialog.show();

        //thoi gian cho id va thoi gian cmt
        String timestamp = "" +System.currentTimeMillis();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id","" + timestamp);
        hashMap.put("bookId", "" + bookId);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("comment", "" + comment);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        //add data  vao` Books > bookId > Comments > commentId > commentData
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PdfDetailActivity.this,"Đã tải bình luận",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PdfDetailActivity.this,"không thể tải bình luận"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    //permission
    private ActivityResultLauncher<String> requestPermisson =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
                if (isGranted){
                    Log.d(TAG_DOWNLOAD,"Permisson Granted");
                    MyApplication.downloadBook(this,
                            ""+bookId,
                            ""+bookUrl ,
                            ""+bookTitle);
                }
                else
                {
                    Log.d(TAG_DOWNLOAD,"Permisson Denied");
                    Toast.makeText(this,"Khong The Cap Quyen" , Toast.LENGTH_SHORT).show();

                }
            });

    private void loadBookDetails() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get
                bookTitle = "" + snapshot.child("title").getValue();
                String des = "" + snapshot.child("des").getValue();
                String categoryId = "" + snapshot.child("categoryId").getValue();
                String viewsCount = "" + snapshot.child("viewsCount").getValue();
                String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                bookUrl = "" + snapshot.child("url").getValue();

                System.out.println("DETAIL: " + bookUrl);
                String timestamp = "" + snapshot.child("timestamp").getValue();

                //load data xong hien button
                binding.downloadBookBtn.setVisibility(View.VISIBLE);


                //format date
                String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                //set
                MyApplication.loadCategory(
                        ""+categoryId,
                        binding.categoryTv
                );

                MyApplication.loadPdfFromUrlSinglePage(
                        ""+ bookUrl,
                        "" +bookTitle,
                        binding.detailPdfView,
                        binding.progressBar,
                        binding.pagesTv
                );

                MyApplication.loadPdfSize(
                        "" + bookUrl,
                        "" + bookTitle,
                        binding.sizeTv
                );


                //set data
                binding.titleTv.setText(bookTitle);
                binding.desTv.setText(des);
                binding.viewsTv.setText(viewsCount.replace("null","N/A"));
                binding.downloadsTv.setText(downloadsCount.replace("null","N/A"));
                binding.dateTv.setText(date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void checkIsFavorite(){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            isIntMyFavorite = snapshot.exists();
                                if(isIntMyFavorite)
                                    {
                                        //exits => remove
                                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white,0,0);
                                        binding.favoriteBtn.setText("Xóa Yêu Thích");
                                    }
                                else {
                                    // not exits => add
                                    binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white,0,0);
                                    binding.favoriteBtn.setText("Yêu Thích");
                                 }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
