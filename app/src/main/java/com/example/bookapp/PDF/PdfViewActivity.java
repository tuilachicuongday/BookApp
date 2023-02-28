package com.example.bookapp.PDF;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.Constants;
import com.example.bookapp.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {
    private  static  final String TAG = "PDF_VIEW_TAG";
    private ActivityPdfViewBinding binding;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(TAG,"onCreate:bookId" + bookId);

        String title = binding.toolbarTitleTv.getText().toString().trim();
        binding.toolbarTitleTv.setText(title);
        loadBookDetails();
        
        
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
    }

    private void loadBookDetails() {
        Log.d(TAG,"loadBookDetails:get Pdf Url");
        //lay bookUrl thong qua bookId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get url
                String pdfUrl = "" +snapshot.child("url").getValue();
                Log.d(TAG,"onDataChange:Pdf Url" + pdfUrl);

                //get name
                String title = "" + snapshot.child("title").getValue();
                binding.toolbarTitleTv.setText(title);

                //load url tu` firebase
                looadBookFromUrl(pdfUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void looadBookFromUrl(String pdfUrl) {
        Log.d(TAG,"looadBookFromUrl Get :Pdf Url From storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
//                binding.progressBar.setVisibility(View.GONE);
                binding.pdfView.fromBytes(bytes).swipeHorizontal(false)  //fail = vertical , true = horizontal
                        .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        int currentPage = (page +1) ;
                        binding.toolbarSubTitleTv.setText(currentPage + "/" + pageCount);
                        Log.d(TAG,"onPageChanged" + currentPage + "/" + pageCount);
                    }
                }).onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Toast.makeText(PdfViewActivity.this,"" + t.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }).onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        Log.d(TAG,"onPageError" + t.getMessage());

                        Toast.makeText(PdfViewActivity.this,"Loi Trang" + page +"" +t.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                }).load();
                binding.progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"onFailure" + e.getMessage());
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}