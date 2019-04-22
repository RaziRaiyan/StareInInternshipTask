package com.example.videoeditor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int VIDEO_PICK_REQ_CODE = 1001;
    private static final int VIDEO_CAPTURE_REQ_CODE = 1002;
    public static String TrimIntentExtra = "selectedUri";

    private static final int REQUEST_PERMISSION_CODE = 1;

    private Uri selectedUri;
    private Button btn_pick;
    private ImageButton btn_capture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_pick = findViewById(R.id.btn_pick_video);
        btn_capture = findViewById(R.id.btn_capture);
        requestPermission();
        setListener();
    }

    private void setListener() {
        if(btn_pick!=null){
            btn_pick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openVideo();
                }
            });
        }
        if(btn_capture!=null){
            btn_capture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    captureVideo();
                }
            });
        }
    }

    private void openVideo(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(intent,VIDEO_PICK_REQ_CODE);
    }

    private void captureVideo(){
        Intent captureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(captureIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(captureIntent,VIDEO_CAPTURE_REQ_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode == VIDEO_PICK_REQ_CODE || requestCode== VIDEO_CAPTURE_REQ_CODE) && resultCode == RESULT_OK){
            selectedUri = data.getData();
            Intent intent = new Intent(MainActivity.this,TrimActivity.class);
            intent.putExtra(TrimIntentExtra,selectedUri.toString());
            startActivity(intent);
        }
    }

    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(this,"you have already granted perimission",Toast.LENGTH_SHORT).show();
        }else {
            requestStoragePermission();
        }
    }

    private void requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)
            && ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Permission needed to read data")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Storage Permission granted",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"Storage Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
