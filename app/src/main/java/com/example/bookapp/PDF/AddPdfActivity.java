package com.example.bookapp.PDF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.DashboardAdminActivity;
import com.example.bookapp.databinding.ActivityAddPdfBinding;
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

import java.util.ArrayList;
import java.util.HashMap;

public class AddPdfActivity extends AppCompatActivity {
    private ActivityAddPdfBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "ADD_PDF_TAG";
    private static final int PDF_PICK_CODE = 1000;
    private Uri pdfUri = null;
    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTiltleArrayList , categoryIdArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui Long Doi");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddPdfActivity.this, DashboardAdminActivity.class);
                startActivity(intent);
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }
    private  String title="",des = "";
    private void validateData() {
        title = binding.titleEt.getText().toString().trim();
        des = binding.desEt.getText().toString().trim();

        if (TextUtils.isEmpty(title))
        {
            Toast.makeText(this, "Nhap Ten", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(des)){
            Toast.makeText(this, "Nhap Mo Ta", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(selectedCatogoryTitle)){
            Toast.makeText(this, "Chon Danh Muc", Toast.LENGTH_SHORT).show();
        }else if (pdfUri == null) {
            Toast.makeText(this, "Chon PDF", Toast.LENGTH_SHORT).show();
        } else{
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        progressDialog.setMessage("Dang Upload");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String filePathAndName = "Books/" +timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG,"onSuccess:Upload to Strorage");

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadPdfUrl = "" +uriTask.getResult();
                
                //upload to firebase
                uploadPdfToDb(uploadPdfUrl,timestamp);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure:Fail" +e.getMessage());
                Toast.makeText(AddPdfActivity.this,"Khong the Upload" + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadPdfToDb(String uploadPdfUrl, long timestamp) {
        Log.d(TAG,"uploadPdfToDb:upload to firebase");
        progressDialog.setMessage("Dang Upload");
        String uid = firebaseAuth.getUid();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("des",""+des);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("url",""+uploadPdfUrl);
        hashMap.put("timestamp",timestamp);
        //count
        hashMap.put("viewsCount",0);
        hashMap.put("downloadsCount",0);


        //db ref Db > Books
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure:Upload Success");
                        Toast.makeText(AddPdfActivity.this,"Upload Thanh Cong",Toast.LENGTH_SHORT).show();


                        //add thanh cong tra Et ve null
                        binding.titleEt.setText("");
                        binding.desEt.setText("");
                        binding.categoryTv.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure:Upload Failed"+e.getMessage());
                Toast.makeText(AddPdfActivity.this,"Upload Khong Thanh Cong"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPdfCategories() {
        categoryTiltleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTiltleArrayList.clear();
                categoryIdArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()){
                    //lay id va tiltle cua cate
                    String categoryId = "" +ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    categoryTiltleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private  String selectedCategoryId,selectedCatogoryTitle;
    private void categoryPickDialog() {
        String[] categoriesArray = new  String[categoryTiltleArrayList.size()];
        for (int i = 0; i < categoryTiltleArrayList.size(); i++)
        {
            categoriesArray[i] = categoryTiltleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chon Danh Muc")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedCatogoryTitle = categoryTiltleArrayList.get(i);
                selectedCategoryId = categoryIdArrayList.get(i);
                binding.categoryTv.setText(selectedCatogoryTitle);
            }
        }).show();
    }
    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent:pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Chon PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult:Picked");
                pdfUri = data.getData();
                Log.d(TAG, "onActivityResult:URI" + pdfUri);
            }
        }
        else {
            Log.d(TAG, "onActivityResult:cancle");
            Toast.makeText(this,"Khong The Chon",Toast.LENGTH_SHORT).show();
        }
    }
}