package com.example.videoeditor;

import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dinuscxj.progressbar.CircleProgressBar;

import java.util.Observable;

public class ProgressBarActivity extends AppCompatActivity {

    CircleProgressBar mCircleProgressBar;
    String[] commands;
    String path;

    ServiceConnection mServiceConnection;
    FFMpegService mFFMpegService;
    Integer res;

    private TextView tv_progress_percent,tv_prepare;
    private Button btn_share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);

        mCircleProgressBar = findViewById(R.id.progress_circular);
        mCircleProgressBar.setMax(100);

        tv_progress_percent = findViewById(R.id.tv_progress_percent);
        btn_share = findViewById(R.id.btn_share_whatsapp);
        tv_prepare = findViewById(R.id.tv_prepare);

        Intent intent = getIntent();

        if(intent!=null){
            commands = intent.getStringArrayExtra("command");
            path = intent.getStringExtra("destination");

            final Intent myIntent = new Intent(ProgressBarActivity.this,FFMpegService.class);
            myIntent.putExtra("command",commands);
            myIntent.putExtra("destination",path);
            startService(myIntent);

            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, final IBinder service) {
                    FFMpegService.LocalBinder binder = (FFMpegService.LocalBinder) service;
                    mFFMpegService = binder.getServiceInstance();
                    mFFMpegService.registerClient(getParent());

                    final Observer<Integer> resultObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(@Nullable Integer integer) {
                            res = integer;
                            if(res<100){
                                mCircleProgressBar.setProgress(res);
                                tv_progress_percent.setText(""+res+"%");
                            }if(res==100){
                                mCircleProgressBar.setProgress(res);
                                tv_progress_percent.setText(""+res+"%");
                                tv_prepare.setText("Video ready to be shared");
                                btn_share.setVisibility(View.VISIBLE);
                                stopService(myIntent);
                                Toast.makeText(getApplicationContext(),"Video prepared successfully",Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    mFFMpegService.getPercentage().observe(ProgressBarActivity.this,resultObserver);

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };

            bindService(myIntent,mServiceConnection, Context.BIND_AUTO_CREATE);
            btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareVideoWhatsApp(path);
                }
            });
        }


    }

    public void shareVideoWhatsApp(String file) {
        Uri uri = Uri.parse(file);
        Intent videoshare = new Intent(Intent.ACTION_SEND);
        videoshare.setType("*/*");
        videoshare.setPackage("com.whatsapp");
        videoshare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        videoshare.putExtra(Intent.EXTRA_STREAM,uri);

        startActivity(videoshare);

    }

}
