package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.PDF.PdfDetailActivity;
import com.example.bookapp.databinding.RowPdfFavoriteBinding;
import com.example.bookapp.databinding.RowPdfUserBinding;
import com.example.bookapp.filters.FilterPdfUser;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolerPdfUser> implements Filterable {

    private static final String TAG = "ADAPTER_PDF_USER_TAG";
    private RowPdfUserBinding binding;
    private Context context;
    private FilterPdfUser filter;

    public ArrayList<ModelPdf> pdfArrayList,filterlist;



    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterlist = pdfArrayList;
    }

    @NonNull
    @Override
    public HolerPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolerPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolerPdfUser holder, int position) {
        //get
        ModelPdf modelPdf = pdfArrayList.get(position);
        String bookId = modelPdf.getId();
        String title = modelPdf.getTitle();
        String des = modelPdf.getDes();
        String pdfUrl = modelPdf.getUrl();
        String pdfId = modelPdf.getId();
        String categoryId = modelPdf.getCategoryId();
        long timestamp = modelPdf.getTimestamp();
        //time convert
        String date = MyApplication.formatTimestamp(timestamp);

        //set
        holder.titleTv.setText(title);
        holder.desTv.setText(des);
        holder.dateTv.setText(date);
        System.out.println("Detail Adapter : " + pdfUrl);

        MyApplication.loadPdfFromUrlSinglePage(
                "" +pdfUrl,
                "" +title,
                holder.pdfView,
                holder.progressBar,
                null
        );

        //lay category thong qua categoryid xong truyen` vo textview
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );

        MyApplication.loadPdfSize(
                "" +pdfUrl,
                "" +title,
                holder.sizeTv
        );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", bookId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfUser(filterlist,this);
        }
        return filter;
    }

    class HolerPdfUser extends RecyclerView.ViewHolder{
        TextView titleTv,desTv,categoryTv,sizeTv,dateTv;
        PDFView pdfView;
        ProgressBar progressBar;
        public HolerPdfUser(@NonNull View itemView) {
            super(itemView);
            titleTv = binding.titleTv;
            desTv = binding.desTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
