package com.example.videoeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TrimActivity extends AppCompatActivity {

    private static final String TAG = "TrimActivity";

    private Uri uri;
    ImageView mImageView;
    VideoView mVideoView;

    boolean isPlaying = false;

    File imageFile;

    String filePrefix;
    String[] commands;
    File dest;
    String original_path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);

        mImageView = findViewById(R.id.img_play_pause);
        mVideoView = findViewById(R.id.videoView);

        Intent thisIntent = getIntent();

        if(thisIntent!=null){
            String imgPath = thisIntent.getStringExtra(MainActivity.TrimIntentExtra);
            uri = Uri.parse(imgPath);
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        }
        
        setListeners();
        
    }

    private void setListeners() {
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    mImageView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mVideoView.pause();
                    isPlaying = false;
                }else {
                    mImageView.setImageResource(R.drawable.ic_pause_black_24dp);
                    mVideoView.start();
                    isPlaying = true;
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.trim){
            final AlertDialog.Builder alert = new AlertDialog.Builder(TrimActivity.this);
            LinearLayout layout = new LinearLayout(TrimActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50,0,50,100);
            final EditText input = new EditText(TrimActivity.this);
            input.setLayoutParams(lp);
            input.setGravity(Gravity.TOP|Gravity.START);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            layout.addView(input,lp);

            alert.setMessage("Set Video name?");
            alert.setTitle("Change Video name");
            alert.setView(layout);
            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.setPositiveButton("submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    filePrefix = input.getText().toString();
                    prepareVideo(filePrefix);
                    Intent intent = new Intent(TrimActivity.this,ProgressBarActivity.class);
                    intent.putExtra("command",commands);
                    intent.putExtra("destination",dest.getAbsolutePath());
                    startActivity(intent);

                    finish();
                    dialog.dismiss();
                }
            });

            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareVideo(String fileName) {
        File folder = new File(Environment.getExternalStorageDirectory()+"/VideoEditor");
        if(!folder.exists()){
            folder.mkdir();
        }
        filePrefix = fileName;
        String fileExt = ".mp4";
        dest = new File(folder,filePrefix+fileExt);
        if(!dest.exists()){
            Log.d(TAG, "prepareVideo: destination doesn't exists");
        }
        original_path = getRealPathFromUri(getApplicationContext(),uri);

        imageFile = new File(Environment.getExternalStorageDirectory() + "/VideoEditor/newlogo.png");
        if (!imageFile.exists())
            try {
            InputStream is = getResources().openRawResource(R.raw.stareinlogo);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //video watermark
        String[] cmdd = {"-i", "" + original_path, "-i", "" + imageFile.getPath(), "-filter_complex", "overlay=10:main_h-overlay_h-10", dest.getAbsolutePath()};

            commands = cmdd;
    }

    private String getRealPathFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        try{
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri,proj,null,null,null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trim_menu,menu);
        return true;
    }
}
