package com.example.bookapp;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bookapp.adapters.AdapterPdfAdmin;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Phaser;

import static com.example.bookapp.Constants.MAX_BYTES_PDF;

public class MyApplication extends Application {

    private  static  final String TAG_DOWNLOAD = "PDF_DOWNLOAD_TAG";

    @Override

    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp ( long timestamp)
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();
        return  date;
    }
    public static void deleteBook(Context context,String bookId, String bookUrl,String bookTitle) {

        String TAG ="DELETE_BOOK_TAG";

//        String bookId = modelPdf.getId();
//        String bookUrl = modelPdf.getUrl();
//        String bookTitle = modelPdf.getTitle();

        Log.d(TAG , "deletebooks:Deleting");


        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Vui Long Doi");
        progressDialog.setMessage("Dang Xoa" + bookTitle+"..");
        progressDialog.show();

        Log.d(TAG,"deleteBook: Xoa khoi strorage");

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"onSuccess : da xoa khoi storage ");

                Log.d(TAG,"onSuccess : xoa khoi database");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                reference.child(bookId)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(context,"Xoa Thanh Cong",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                double bytes = storageMetadata.getSizeBytes();
                Log.d(TAG,"onSuccess"+pdfTitle+""+bytes);
                //convert to kb mb
                double kb = bytes/1024;
                double mb = kb/1024;

                if (mb >= 1){
                   sizeTv.setText(String.format("%.2f",mb)+"MB");
                }
                else if (kb >= 1)
                {
                    sizeTv.setText(String.format("%.2f",kb)+"KB");
                }
                else {
                    sizeTv.setText(String.format("%.2f",bytes)+"Bytes");

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"onFailure"+e.getMessage());
            }
        });
    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView , ProgressBar progressBar , TextView pagesTv) {
        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                Log.d(TAG,"onSuccess:"+pdfTitle+"Thanh Cong");

                //set
                Log.d("PDFVIEW","PDFVIEW:"+pdfView+"Thanh Cong1");

                pdfView.fromBytes(bytes)
                        .pages(0) //chi hien trang dau`
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG,"onPageError"+t.getMessage());
                            }
                        }).onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG,"onPageError"+t.getMessage());
                    }
                })
                        .onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                //load thanh cong pdf
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG,"loadComplete:load pdf thanh cong");


                                //PageNumber
                                if (pagesTv != null){
                                    pagesTv.setText("" +nbPages);
                                }
                            }
                        }).load();
                Log.d("PDFVIEW","PDFVIEW:"+pdfView+"Thanh Cong2");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG,"onFailure:fail getting file url" + e.getMessage());
            }
        });
    }

    public static void  loadCategory(String categoryId , TextView categoryTv) {
        //lay category thong qua categoryid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get
                        String category = "" +snapshot.child("category").getValue();

                        //set
                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public  static  void bookViewCount(String bookId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String viewsCount = "" + snapshot.child("viewsCount").getValue();
                // null thi` = 0
                if (viewsCount.equals("") || viewsCount.equals("null"))
                {
                    viewsCount = "0";
                }
                //tang len khi nhan
                long newViewsCount = Long.parseLong(viewsCount) + 1;
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("viewsCount" , newViewsCount);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                ref.child(bookId).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public  static  void downloadBook(Context context,String bookId,String bookTitle,String bookUrl)
    {
        String nameFile = bookTitle + ".pdf";

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Vui Long Doi");
        progressDialog.setMessage("Dang Tai" + nameFile + "..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //download thong qua url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG_DOWNLOAD,"onSuccess:Download Thanh Cong");
                saveDownloadedBook(context,progressDialog,bytes,nameFile,bookId);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG_DOWNLOAD,"onSuccess:Download That Bai" + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context,"Download That Bai" + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameFile, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook:Saved" );
        try{
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdir();

            String filePath = downloadsFolder.getPath() + "/" +nameFile;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context,"Da Luu Vao Folder" ,Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD,"saveDownloadedBook: Saved to Download Folder");
            progressDialog.dismiss();

            BookDownloadCount(bookId);

        }catch (Exception e){
            Log.d(TAG_DOWNLOAD,"saveDownloadedBook: Faild Saving to Download Folder" + e.getMessage());
            Toast.makeText(context,"Luu That Bai" +e.getMessage() ,Toast.LENGTH_SHORT).show();
        }
    }

    private static void BookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD,"BookDownloadCount:Tang");

        //lay so luong download ban dau
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String downloadsCount = "" +snapshot.child("downloadsCount").getValue();
                Log.d(TAG_DOWNLOAD , "onDataChange:Donwload Count" +downloadsCount);

                if (downloadsCount.equals("") || downloadsCount.equals("null"))
                {
                    downloadsCount="0";
                }
                long newDownloadCount = Long.parseLong(downloadsCount) + 1;
                Log.d(TAG_DOWNLOAD , "onDataChange: New Donwload Count " +newDownloadCount);

                //set data de update
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("downloadsCount",newDownloadCount);

                //update
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                reference.child(bookId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG_DOWNLOAD,"onSuccess:Count Update");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD,"onSuccess:Count Fail to Update" + e.getMessage());
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void addToFavorite(Context context,String bookId){
        //chi yeu thich dc khi dang nhap
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context,"Bạn Chưa Đăng Nhập",Toast.LENGTH_SHORT).show();
        }
        else
        {
            long timestamp = System.currentTimeMillis();

            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("bookId", "" +bookId);
            hashMap.put("timestamp",""+timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites")
                    .child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"Đã thêm vào danh sách yêu thích",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,"Thêm vào danh sách yêu thích thất bại"+e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    public static void removeFromFavorite(Context context,String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context,"Bạn Chưa Đăng Nhập",Toast.LENGTH_SHORT).show();
        }
        else
        {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"Đã xóa khỏi danh sách yêu thích",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,"xóa khỏi danh sách yêu thích thất bại"+e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

}
