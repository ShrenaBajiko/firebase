package com.example.dell.firebaseupload;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button mButtonChooseImage;
    private Button mButtonupload;
    private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

   private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mTextViewShowUploads = findViewById(R.id.text_view_show_upload);
        mButtonupload = findViewById(R.id.button_upload);
        mEditTextFileName = findViewById(R.id.edit_text_file_name);
        mButtonupload = findViewById(R.id.button_upload);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();

            }
        });

        mButtonupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(MainActivity.this,"Upload is in progress",Toast.LENGTH_SHORT).show();
                }
                uploadFile();

            }
        });

        mTextViewShowUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
            ;
        {
            mImageUri = data.getData();
            mImageView.setImageURI(mImageUri);
        }
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadFile()
    {
       if(mImageUri != null)
       {
           StorageReference fileReference=mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
         mUploadTask = fileReference.putFile(mImageUri)
                   .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           Handler handler = new Handler();
                           handler.postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   mProgressBar.setProgress(0);
                               }
                           },5000);

                           Toast.makeText(MainActivity.this,"upload sucessful",Toast.LENGTH_SHORT).show();
                           Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                   taskSnapshot.getDownloadUrl().toString());
                           String uploadId = mDatabaseRef.push().getKey();
                           mDatabaseRef.child(uploadId).setValue(upload);
                       }
                   })
                   .addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                       }
                   })
                   .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                           double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                           mProgressBar.setProgress((int)progress);

                       }
                   });
       }
       else{
           Toast.makeText(this,"NO FILE SELECTED",Toast.LENGTH_SHORT).show();
       }
    }
}
