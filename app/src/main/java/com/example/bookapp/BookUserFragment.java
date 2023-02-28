package com.example.bookapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookapp.adapters.AdapterPdfUser;
import com.example.bookapp.databinding.FragmentBookUserBinding;
import com.example.bookapp.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookUserFragment extends Fragment {

    private String categoryId;
    private String category;
    private String uid;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;

    private FragmentBookUserBinding binding;
    private static  final String TAG = "BOOK_USER_TAG";

    public BookUserFragment() {
        // Required empty public constructor
    }


    public static BookUserFragment newInstance(String categoryId, String category ,  String uid) {
        BookUserFragment fragment = new BookUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(getContext()), container, false);

        Log.d(TAG, "onCreateView:Cate" + category);

        if (category.equals("Tất Cả"))
        {
            loadAllBook();
        }
        else if (category.equals("Xem Nhiều Nhất"))
        {
            loadMostViewed("viewsCount");
        }
        else if (category.equals("Tải Nhiều Nhất"))
        {
            loadMostViewed("downloadsCount");

        }
        else
        {
            // load danh muc sach duoc chon (child = categoryId)
            loadCategorizedBooks();
        }

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //tim kiem gan dung
                try {
                    adapterPdfUser.getFilter().filter(s);
                }
                catch (Exception e)
                {
                    Log.d(TAG,"onTextChanged" + e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }



    private void loadAllBook() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    //get
                    ModelPdf modelPdf = ds.getValue(ModelPdf.class);
                    //add vo list
                    pdfArrayList.add(modelPdf);
                }
                //set adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                //set adapter rcv
                binding.bookRv.setAdapter(adapterPdfUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMostViewed(String orderBy) {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10) // xuat 1 lan  10 mostview hoac downloadview
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    //get
                    ModelPdf modelPdf = ds.getValue(ModelPdf.class);
                    //add vo list
                    pdfArrayList.add(modelPdf);
                }
                //set adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                //set adapter rcv
                binding.bookRv.setAdapter(adapterPdfUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCategorizedBooks() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren())
                        {
                            //get
                            ModelPdf modelPdf = ds.getValue(ModelPdf.class);
                            //add vo list
                            pdfArrayList.add(modelPdf);
                        }
                        //set adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                        //set adapter rcv
                        binding.bookRv.setAdapter(adapterPdfUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}