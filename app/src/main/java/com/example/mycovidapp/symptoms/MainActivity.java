package com.example.symptomTrackerApp.symptoms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Environment;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.symptomTrackerApp.UploadData;
import com.example.symptomTrackerApp.dbAdapter.User;
import com.example.symptomTrackerApp.gps.AlarmReceiver;
import com.example.symptomTrackerApp.dbAdapter.DbHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;

import static java.lang.Math.abs;

import net.sqlcipher.database.SQLiteDatabase;

import mycovidapp.R;


public class MainActivity extends AppCompatActivity {

     static final int VIDEO_CAPTURE = 101;
     Uri uri;
     int windows = 9;
     long startingTime;
     TextView heartRateTextView;
     TextView respiratoryRateTextView;

     int heartData = 0;
     float respirationData = 0;
     boolean uploadSymptomsClicked = false;

     private boolean heartRateProcessing = false;
     private boolean respRateOnGoing = false;



    //Location
    Button gpsButton;
    TextView gpsTextView;
    Button cameraRecordButton;
    Button calculateHeartRate;
    Button calculateRespiratoryRate;
    Button uploadSymptoms;
    Button uploadButton;
    long latitude=0;
    long longitude=0;
    Date locationTimeStamp;

    FusedLocationProviderClient locationProviderClient;



    private final String applicationPath = Environment.getExternalStorageDirectory().getPath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signs_screen);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        cameraRecordButton = findViewById(R.id.cameraRecord);
        calculateHeartRate = findViewById(R.id.heartRateButton);
        calculateRespiratoryRate = findViewById(R.id.respiratoryButton);
        uploadSymptoms = findViewById(R.id.uploadSymptomsButton);
        uploadButton = findViewById(R.id.uploadButton);

        gpsButton = findViewById(R.id.gpsButton);
        gpsTextView = findViewById(R.id.gpsTextView);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        heartRateTextView = findViewById(R.id.heartRateTextView);
        respiratoryRateTextView = findViewById(R.id.respiratoryRateTextView);

        SQLiteDatabase.loadLibs(this);
        handlePermissions(MainActivity.this);

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"Button pressed",Toast.LENGTH_SHORT).show();


                locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    public void onSuccess(Location location) {


                        //Starting the service and adding alarm manager
                        AlarmReceiver alarm = new AlarmReceiver();
                        alarm.setAlarm(MainActivity.this);

                        if (location != null) {
                            // Logic to handle location object

                            latitude = (long) location.getLatitude();
                            longitude = (long) location.getLongitude();
                            locationTimeStamp =new Date(System.currentTimeMillis());
                            gpsTextView.setText("Latitude: " + latitude + "Longitude: " + longitude);

                        }
                    }
                });
            }
        });




        // Opens recorder on click
        cameraRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(heartRateProcessing) {
                    Toast.makeText(MainActivity.this, "Processing the video, kindly wait for completion.",
                            Toast.LENGTH_LONG).show();
                } else {

                    recordVideo();
                }
            }
        });

        // Starts Accelerometer service on click
        calculateHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File file = new File(applicationPath + "/video.mp4");
                uri = Uri.fromFile(file);

                // Checks if heart rate video exists and if there is an existing heart rate detection process running
                if(heartRateProcessing) {
                    Toast.makeText(MainActivity.this, "Processing the video, kindly wait for completion.",
                            Toast.LENGTH_SHORT).show();
                } else if (file.exists()) {
                    heartRateProcessing = true;
                    heartRateTextView.setText("In progress");

                    startingTime = System.currentTimeMillis();
                    Intent heartIntent = new Intent(MainActivity.this, HeartService.class);
                    startService(heartIntent);

                } else {
                    Toast.makeText(MainActivity.this, "No video found right now", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Starts Heart Rate service on click
        calculateRespiratoryRate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Checks if there is an existing respiratory rate detection process running
                if(respRateOnGoing) {
                    Toast.makeText(MainActivity.this, "One process is already running, kindly wait",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Follow the instructions to save data, place on abdomen", Toast.LENGTH_LONG).show();
                    respRateOnGoing = true;
                    respiratoryRateTextView.setText("Calculating Respiratory Rate");
                    Intent intent = new Intent(MainActivity.this, accelerometerService.class);
                    startService(intent);
                }
            }
        });

        // Updates the 10 columns of symptom ratings on click
        uploadSymptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SymptomsActivity.class);
                intent.putExtra("uploadSymptomsClicked", uploadSymptomsClicked);
                startActivity(intent);
            }
        });

        // Creates a row in the database with signs data inserted
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadSymptomsClicked = true;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        User data = new User();
                        data.rateHeart = (float) heartData;
                        data.rateBreathing = respirationData;
                        data.dateTime = new Date(System.currentTimeMillis());

                        //add location data here
                        data.latitude=latitude;
                        data.longitude=longitude;
                        data.locationTimeStamp= locationTimeStamp;

                        SharedPreferences sp= getSharedPreferences("Username", Context.MODE_PRIVATE);

                        DbHelper.password= sp.getString("password","1234");


                        DbHelper.getInstance(MainActivity.this).InsertData(data);
                        System.out.println("The path is "+ DbHelper.getInstance(MainActivity.this).getWritableDatabase("1234").getPath());

                        UploadData uploadData=new UploadData();


                        String userID= sp.getString("username","akhil");

                        uploadData.SendData(userID,data.dateTime.toString());
                       // db.userInfoDao().insert(data);
                    }
                });
                thread.start();

                Toast.makeText(MainActivity.this, "Upload Completed", Toast.LENGTH_SHORT).show();
            }

        });

        //Listens for local broadcast containing X values sent by Accelerometer service for calculation
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle bundle = intent.getExtras();
                BreathingRateDetector runnable = new BreathingRateDetector(bundle.getIntegerArrayList("accelValuesX"));

                Thread thread = new Thread(runnable);
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {

                }

                respiratoryRateTextView.setText(runnable.breathingRate + "");
                respirationData = runnable.breathingRate;
                Toast.makeText(MainActivity.this, "Processing completed", Toast.LENGTH_SHORT).show();
                respRateOnGoing = false;
                bundle.clear();


            }
        }, new IntentFilter("broadcastingAccelData"));


        //Listens for local broadcast containing average red values of extracted frames sent by Heart rate service for calculation
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle bundle = intent.getExtras();
                float heartRate = 0;
                int fail = 0;
                //Processes 9 windows of 5 second video snippets separately to calculate heart rate
                for (int i = 0; i < windows; i++) {

                    ArrayList<Integer> heartData;
                    heartData = bundle.getIntegerArrayList("heartData"+i);

                    //Removes noise from raw average redness frame data
                    ArrayList<Integer> denoisedRedness = reduceNoise(heartData, 5);

                    float zcrossings = peakDetection(denoisedRedness);
                    heartRate += zcrossings/2;
                    String csvfile = applicationPath + "/video" + i + ".csv";
                    saveCSV(heartData, csvfile);

                    csvfile = applicationPath + "/video_denoised" + i + ".csv";
                    saveCSV(denoisedRedness, csvfile);
                }

                heartRate = (heartRate*12)/ windows;

                heartRateTextView.setText(heartRate + "");
                heartRateProcessing = false;
                Toast.makeText(MainActivity.this, "Calculated Heart Rate Successfully", Toast.LENGTH_SHORT).show();
                bundle.clear();

            }
        }, new IntentFilter("broadcastingHeartData"));

    }

    @Override
    protected void onStart() {
        super.onStart();
        uploadSymptomsClicked = false;
    }

    /**
     * Class that implements runnable to process breathing rate data for calculation
     */
    public class BreathingRateDetector implements Runnable{

        public float breathingRate;
        ArrayList<Integer> accelValuesX;

        BreathingRateDetector(ArrayList<Integer> accelValuesX){
            this.accelValuesX = accelValuesX;
        }

        @Override
        public void run() {

            String csvfile = applicationPath + "/x_values_a.csv";
            saveCSV(accelValuesX, csvfile);

            //Noise reduction from Accelerometer X values
            ArrayList<Integer> accelValuesXDenoised = reduceNoise(accelValuesX, 10);

            csvfile = applicationPath + "/x_values_a_denoised.csv";
            saveCSV(accelValuesXDenoised, csvfile);

            int  zeroCrossings = peakDetection(accelValuesXDenoised);
            breathingRate = zeroCrossings*60 /90;
        }

    }


    public ArrayList<Integer> reduceNoise(ArrayList<Integer> data, int filter){

        ArrayList<Integer> averageArray = new ArrayList<>();
        int mavg = 0;

        for(int i=0; i< data.size(); i++){
            mavg += data.get(i);
            if(i+1 < filter) {
                continue;
            }
            averageArray.add((mavg)/filter);
            mavg -= data.get(i+1 - filter);
        }

        return averageArray;

    }

    public int peakDetection(ArrayList<Integer> data) {
        int slope = 0;
        int diff, prev, zeroCrossings = 0;
        int j = 0;
        prev = data.get(0);

        while( slope == 0 && j + 1 < data.size()){
            diff = data.get(j + 1) - data.get(j);
            if(diff != 0){
                slope = diff/abs(diff);
            }
            j++;
        }

        //Get total number of zero crossings in data curve
        for(int i = 1; i<data.size(); i++) {

            diff = data.get(i) - prev;
            prev = data.get(i);

            if(diff == 0) continue;

            int currSlope = diff/abs(diff);

            if(currSlope == -1* slope){
                slope *= -1;
                zeroCrossings++;
            }
        }

        return zeroCrossings;
    }


    public static void handlePermissions(Activity activity) {

        //int storagePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int REQUEST_EXTERNAL_STORAGE = 1;

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET


        };

        ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS,
                REQUEST_EXTERNAL_STORAGE
        );


    }


    public void recordVideo() {

        File file = new File( applicationPath + "/video.mp4");
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,45);

        uri = Uri.fromFile(file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, VIDEO_CAPTURE);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        boolean deleteFile = false;
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {

                MediaMetadataRetriever videoRetriever = new MediaMetadataRetriever();
                FileInputStream videoFile = null;
                try {
                    videoFile = new FileInputStream(uri.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    videoRetriever.setDataSource(videoFile.getFD());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                String tString = videoRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long time = Long.parseLong(tString)/1000;


                if(time<45) {

                    Toast.makeText(this,
                            "Video to Record for 45s", Toast.LENGTH_SHORT).show();
                    deleteFile = true;
                } else{
                    Toast.makeText(this, "Video saved", Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_SHORT).show();
                deleteFile = true;
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_SHORT).show();
            }

            if(deleteFile) {
                File filePath = new File(uri.getPath());

                if (filePath.exists()) {
                    filePath.delete();
                }
            }
            uri = null;
        }
    }

    public void saveCSV(ArrayList<Integer> data, String path){

        File file = new File(path);

        try {
            FileWriter outputFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputFile);
            String[] header = { "Index", "Data"};
            writer.writeNext(header);
            int i = 0;
            for (int d : data) {
                String[] dataRow = {i + "", d + ""};
                writer.writeNext(dataRow);
                i++;
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
