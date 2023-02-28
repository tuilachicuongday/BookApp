package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.PDF.PdfDetailActivity;
import com.example.bookapp.databinding.RowPdfFavoriteBinding;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.EmptyOrBuilder;

import java.util.ArrayList;

public class AdapterPdfFavorite  extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite>{

    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    private RowPdfFavoriteBinding binding;
    private static final String TAG = "FAV_BOOKS_TAG";

    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {
        ModelPdf modelPdf = pdfArrayList.get(position);

        loadBookDetails(modelPdf,holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", modelPdf.getId());
                context.startActivity(intent);

            }
        });

        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context,modelPdf.getId());
            }
        });
    }

    private void loadBookDetails(ModelPdf modelPdf, HolderPdfFavorite holder) {
        String bookId = modelPdf.getId();
        Log.d(TAG,"loadBookDetails:ID" + bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String bookTitle = "" + snapshot.child("title").getValue();
                String des = "" + snapshot.child("des").getValue();
                String categoryId = "" + snapshot.child("categoryId").getValue();
                String bookUrl = "" + snapshot.child("url").getValue();
                String timestamp = "" + snapshot.child("timestamp").getValue();
                String uid = "" + snapshot.child("uid").getValue();
                String viewsCount = "" + snapshot.child("viewsCount").getValue();
                String downloadsCount = "" + snapshot.child("downloadCount").getValue();

                modelPdf.setFavorite(true);
                modelPdf.setTitle(bookTitle);
                modelPdf.setDes(des);
                modelPdf.setTimestamp(Long.parseLong(timestamp));
                modelPdf.setCategoryId(categoryId);
                modelPdf.setUid(uid);
                modelPdf.setUrl(bookUrl);

                String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                MyApplication.loadCategory(
                        categoryId,
                        holder.categoryTv
                );

                MyApplication.loadPdfFromUrlSinglePage(
                        ""+bookUrl,
                        ""+bookTitle,
                        binding.pdfView,
                        binding.progressBar,
                        null
                );
                MyApplication.loadPdfSize(
                        ""+bookUrl,
                        ""+bookTitle,
                        holder.sizeTv);

                //set data vo view
                holder.titleTv.setText(bookTitle);
                holder.desTv.setText(des);
                holder.dateTv.setText(date);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfFavorite extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv,desTv,categoryTv,sizeTv,dateTv;
        ImageButton removeFavBtn;


        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            removeFavBtn = binding.removeFavBtn;
            desTv = binding.desTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
        }
    }
}

