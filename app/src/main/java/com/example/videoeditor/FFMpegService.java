package com.example.videoeditor;

import android.app.Activity;
import android.app.Service;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class FFMpegService extends Service {
    private static final String TAG = "FFMpegService";

    FFmpeg mFFmpeg;

    String[] commands;
    Callbacks activity;

    public MutableLiveData<Integer> percentage;
    IBinder myBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            commands = intent.getStringArrayExtra("command");
            try{
                loadFFMpegBinary();
                executeFfmpegCommand();
            }catch (FFmpegCommandAlreadyRunningException e){
                e.printStackTrace();
            } catch (FFmpegNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return super.onStartCommand(intent,flags,startId);
    }

    private void executeFfmpegCommand() throws FFmpegCommandAlreadyRunningException {
        mFFmpeg.execute(commands,new ExecuteBinaryResponseHandler(){
            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);
                Log.d(TAG, "onSuccess: Process Succefull");
            }

            @Override
            public void onProgress(String message) {
                String arr[];
                if(message.contains("time=")){
                    arr = message.split("time=");
                    String yalo = arr[1];

                    String abikamha[] = yalo.split(":");
                    String[] yaenda = abikamha[2].split(" ");
                    String seconds = yaenda[0];

                    int hours = Integer.parseInt(abikamha[0]);
                    hours = hours*3600;
                    int min = Integer.parseInt(abikamha[1]);
                    min = min*60;
                    float sec = Float.valueOf(seconds);

                    float timeInSec = hours*min*sec;

                    percentage.setValue((int)((timeInSec)*1000));
                }
            }

            @Override
            public void onFailure(String message) {
                super.onFailure(message);
                Log.d(TAG, "onFailure: Proceess Failed");
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                percentage.setValue(100);
            }
        });

    }

    @Override
    public void onCreate() {
        super.onCreate();
        try{
            loadFFMpegBinary();
        }catch (FFmpegNotSupportedException e){
            e.printStackTrace();

        }
        percentage = new MutableLiveData<>();
    }

    private void loadFFMpegBinary() throws FFmpegNotSupportedException {
        if(mFFmpeg == null){
            mFFmpeg = FFmpeg.getInstance(this);
        }

        mFFmpeg.loadBinary(new LoadBinaryResponseHandler(){
            @Override
            public void onFailure() {
                super.onFailure();
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    public FFMpegService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class LocalBinder extends Binder{
        public FFMpegService getServiceInstance(){
            return FFMpegService.this;
        }
    }

    public void registerClient(Activity activity){
        this.activity = (Callbacks) activity;
    }

    public MutableLiveData<Integer> getPercentage(){
        return percentage;
    }

    public interface Callbacks{
        void updateClient(float data);
    }
}
