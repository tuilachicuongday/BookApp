package com.example.bookapp.PDF;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityEditPdfBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class EditPdfActivity extends AppCompatActivity {
    private ActivityEditPdfBinding binding;
    private String bookId;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList,categoryIdArrayList;
    private static final String TAG = "BOOK_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui Long Doi");
        progressDialog.setCanceledOnTouchOutside(false);
        
        loadCategories();
        loadBookInfo();
        
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String title="" , des = "";
    private void validateData() {
        title = binding.titleEt.getText().toString().trim();
        des = binding.desEt.getText().toString().trim();

        if (TextUtils.isEmpty(title))
        {
            Toast.makeText(this,"Nhap Tieu De",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(des))
        {
            Toast.makeText(this,"Nhap Mo Ta",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryId))
        {
            Toast.makeText(this,"Chon Danh Muc",Toast.LENGTH_SHORT).show();
        }
        else {
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG,"updatePdf : update to database");

        progressDialog.setMessage("Dang Cap nhat");
        progressDialog.show();

        //set data de update database
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("des",""+des);
        hashMap.put("categoryId",""+selectedCategoryId);

        //update
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(EditPdfActivity.this,"Update Thanh Cong",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure:Update failed" + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(EditPdfActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void loadBookInfo() {
        Log.d(TAG,"loadBookInfo:Loading");
        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedCategoryId = ""+ snapshot.child("categoryId").getValue();
                String des = "" + snapshot.child("des").getValue();
                String title = "" + snapshot.child("title").getValue();

                binding.titleEt.setText(title);
                binding.desEt.setText(des);
                Log.d(TAG,"onDataChange:Loading Book cate");

                DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                refBookCategory.child(selectedCategoryId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get
                        String category = "" + snapshot.child("category").getValue();
                        //set
                        binding.categoryTv.setText(category);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId ="", selectedCategoryTitle="";

    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size();i++)
        {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chon Danh Muc")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedCategoryId = categoryIdArrayList.get(i);
                selectedCategoryTitle = categoryTitleArrayList.get(i);

                binding.categoryTv.setText(selectedCategoryTitle);
            }
        }).show();
    }

    private void loadCategories() {
        Log.d(TAG,"loadCategories:Loading");

        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren())
                {

                    String id = "" + ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();

                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG,"onDataChange:ID" +id);
                    Log.d(TAG,"onDataChange:Category" +category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}