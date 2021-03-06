package com.example.veditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddVideoActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private EditText titleEt;
    private VideoView videoView;
    private Button uploadvideo;
    private FloatingActionButton pickVideoFab;

    private static final int VIDEO_PICK_GALLERY_CODE = 100;
    private static final int VIDEO_PICK_CAMERA_CODE = 101;
    private static final int CAMERA_CODE = 102;

    private String[] cameraPermission;

    private Uri videoUri = null;

    private String title;

    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        //Initialize variables
        titleEt = findViewById(R.id.titleEt);
        videoView= findViewById(R.id.video_View);
        uploadvideo = findViewById(R.id.uploadVideo);
        pickVideoFab = findViewById(R.id.pickVideoFab);

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading Video");
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        uploadvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                     title = titleEt.getText().toString().trim();
                    if (TextUtils.isEmpty(title)){
                        Toast.makeText(AddVideoActivity.this,"Title is Required...", Toast.LENGTH_SHORT).show();
                    }else if (videoUri == null){
                        //video is not picked
                        Toast.makeText(AddVideoActivity.this, "Pick a Video Before Uploading....", Toast.LENGTH_SHORT).show();
                    }else{
                     uploadVideoFirebase();
                    }
            }
        });

        pickVideoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPickDialog();
            }
        });

        actionBar = getSupportActionBar();

        actionBar.setTitle("Add New Video");

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void uploadVideoFirebase() {
        //show progress
        progressDialog.show();
        final String timestamp ="" + System.currentTimeMillis();
        //file path and name
        String filePathAndName = "Videos/" + "video_" + timestamp;
        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        //upload video
        storageReference.putFile(videoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //video uploaded get url of video
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            //url of uploaded video is recieved
                            //now add video details in firebase
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id","" + timestamp);
                            hashMap.put("title",""  + title);
                            hashMap.put("timestamp","" + timestamp);
                            hashMap.put("videoUrl","" + downloadUri);


                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Videos");
                            reference.child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AddVideoActivity.this,"Video Uploaded...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AddVideoActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddVideoActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void videoPickDialog() {
        String[] option = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Video From")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){
                           //camera clicked
                            if (!checkCameraPermission()){
                                //camera permission not allowed, request it
                                requestCameraPermission();
                            }else {
                                //permissionalready allowed,take picture
                                setVideoPickCamera();
                            }
                        }
                        else if (i==1){
                            //gallery clicked
                            setVideoPickGallery();
                        }
                    }
                })
                .show();
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this,Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED;

        return result1 && result2;
    }

    private void  setVideoPickGallery(){
        Intent intent = new Intent();
        intent.setType("Video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_PICK_GALLERY_CODE);
    }

    private void setVideoPickCamera(){

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent , VIDEO_PICK_CAMERA_CODE);
    }

    private void setVideoToVideoView(){
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);

        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.pause();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_CODE:
                if (grantResults.length > 0){
                    //check permission allowed or not
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //both permission allowed
                        setVideoPickCamera();
                    }
                    else{
                        //both or none of those denied
                        Toast.makeText(this,"Camera and Storage Permission is Required", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode ==  VIDEO_PICK_GALLERY_CODE){
                videoUri = data.getData();
                setVideoToVideoView();
            }
            else if (requestCode == VIDEO_PICK_CAMERA_CODE){
                videoUri = data.getData();
                setVideoToVideoView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go to privious activity
        return super.onSupportNavigateUp();
    }
}