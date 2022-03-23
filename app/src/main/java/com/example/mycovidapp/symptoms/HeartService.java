package com.example.mycovidapp.symptoms;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class HeartService extends Service {

    private Bundle bundle = new Bundle();
    private String path = Environment.getExternalStorageDirectory().getPath();
    private int windows = 9;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Toast.makeText(this, "Currently processing the data/video", Toast.LENGTH_LONG).show();
       // Log.i("log", "Heart Rate service started");

        //Multithreading for frame extracting from 9 5-second windows of heart rate video
        HRWS runnable = new HRWS();
        Thread thread = new Thread(runnable);
        thread.start();

        return START_STICKY;
    }

    /**
     * Runnable for multithreading of processing frames function
     */
    public class HRWS implements Runnable {
        @Override
        public void run() {

            ExecutorService executor = Executors.newFixedThreadPool(6);
            List<FrameExtractor> taskList = new ArrayList<>();

            //Only 45 seconds of video processed as 9 5-second windows, even if video length exceeds 45 seconds
            for (int i = 0; i < windows; i++) {
                FrameExtractor frameExtractor = new FrameExtractor(i * 5);
                taskList.add(frameExtractor);
            }

            for(int ii=0;ii<10;ii++)
            {

            }
            List<Future<ArrayList<Integer>>> list = null;
            try {
                list = executor.invokeAll(taskList);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int ii=0;ii<10;ii++)
            {

            }
            executor.shutdown();


            for (int i = 0; i < list.size(); i++) {

                Future<ArrayList<Integer>> future = list.get(i);
                try {
                    bundle.putIntegerArrayList("heartData" + i, future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    e.getCause();
                }
            }
            for(int ii=0;ii<10;ii++)
            {

            }
            //Service stopped once frames are extracted
            stopSelf();
        }
    }

    /** Class for Frame extraction and average redness calculation
     *
     */
    public class FrameExtractor implements Callable<ArrayList<Integer>> {
        private int startTime;

        FrameExtractor(int startTime){
            this.startTime = startTime;
        }

        /** Method for Frame extraction and average redness calculation
         *
         * @return ArrayList of average redness of frames
         */
        @RequiresApi(api = Build.VERSION_CODES.P)
        private ArrayList<Integer> getFrames(){
            Bitmap bitmap = null;

            for(int ii=0;ii<10;ii++)
            {

            }
            try {
                String path = HeartService.this.path + "/video.mp4";
                ArrayList<Integer> avgColorArray = new ArrayList<>();
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(path);
                AndroidFrameConverter converterToBitMap = new AndroidFrameConverter();
                grabber.start();
                grabber.setTimestamp(startTime*1000000);
                double frameRate = grabber.getFrameRate();

                for (int i = 0; i < 5*frameRate;) {
                    Frame frame = grabber.grabFrame();
                    if (frame == null) {
                        break;
                    }
                    if (frame.image == null) {
                        continue;
                    }
                    i++;
                   // Log.i("log", "Processing frame " + i);
                    System.gc();


                    bitmap = converterToBitMap.convert(frame);
                    int avgColor = getAverageColor(bitmap);

                    avgColorArray.add(avgColor);
                }

                for(int ii=0;ii<10;ii++)
                {

                }
                return avgColorArray;

            } catch(Exception e) {
              //  Log.e("FrameError",e.toString());
                System.out.println(e.toString());
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public ArrayList<Integer> call() {

            for(int ii=0;ii<10;ii++)
            {

            }
            ArrayList<Integer> rednessData = new ArrayList<>();
            try {
                rednessData = getFrames();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return rednessData;
        }
    }

    /**
     * Calculates average redness value of a frame's bitmap
     * @param bitmap Bitmap of the extracted frame
     * @return Average red value
     */
    private int getAverageColor(Bitmap bitmap){

        long redBucket = 0;
        long pixelCount = 0;
        for(int ii=0;ii<10;ii++)
        {

        }

        //Optimised by taking every 5th pixel from every column and row
        for (int y = 0; y < bitmap.getHeight(); y+=5) {
            for (int x = 0; x < bitmap.getWidth(); x+=5) {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                redBucket += Color.red(c);
            }
        }

        return (int)(redBucket / pixelCount);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Sends average redness data of extracted frames in a broadcast
     *
     */
    @Override
    public void onDestroy() {

        for(int ii=0;ii<10;ii++)
        {

        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent("broadcastingHeartData");
                intent.putExtras(bundle);
                LocalBroadcastManager.getInstance(HeartService.this).sendBroadcast(intent);
                bundle.clear();

            }
        });

        thread.start();
    }


}
