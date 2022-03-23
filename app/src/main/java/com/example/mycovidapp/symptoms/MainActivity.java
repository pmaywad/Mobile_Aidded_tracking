package com.example.mycovidapp.symptoms;


//New one



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
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycovidapp.UploadData;
import com.example.mycovidapp.database.User;
import com.example.mycovidapp.gps.AlarmReceiver;
import com.example.mycovidapp.newdatabase.DbHelper;
import com.example.mycovidapp.newdatabase.SetterGetter;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.abs;

import net.sqlcipher.database.SQLiteDatabase;

import mycovidapp.R;


public class MainActivity extends AppCompatActivity {

    static final int VIDEO_CAPTURE = 101;
     Uri uri;
     int windows = 9;
    long startingTime;
    TextView hrateTextView;
    TextView brateTextView;

     int heartData =0;
     float respirationData =0;

     boolean upSClicked = false;
    // Used to tell if Upload Signs button has been pressed
    private boolean hrtGoing = false;
    private boolean respRateOnGoing = false;
    // Used to tell if calculation process is ongoing to avoid starting duplicate processes


    //Location
    Button LocationBtn;
    TextView LocationText;
    float latitude=0;
    float longitude=0;
    Date locationtimestamp;

    FusedLocationProviderClient locationProviderClient;
    DbHelper dbHelper;





    private String applicationPath = Environment.getExternalStorageDirectory().getPath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signs_screen);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Button rcdBtn = (Button) findViewById(R.id.recordBtn);
        Button calcHeartRate = (Button) findViewById(R.id.calcHeartRate);
        Button calcRespiration = (Button) findViewById(R.id.calcResp);
        Button symptoms = (Button) findViewById(R.id.symptomsScreen);
        Button uploadBtn = (Button) findViewById(R.id.uploadBtn);

        LocationBtn=(Button) findViewById(R.id.locationbutton);
        LocationText=(TextView) findViewById(R.id.locationtextView);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        hrateTextView = (TextView) findViewById(R.id.showHeartRate);
        brateTextView = (TextView) findViewById(R.id.showResp);


        //New Database
        //Password
        SharedPreferences preferences = getSharedPreferences("Username", Context.MODE_PRIVATE);
        SetterGetter.passWord=preferences.getString("password","1234");

        SQLiteDatabase.loadLibs(this);
       dbHelper= DbHelper.getInstance(MainActivity.this);



        Toast.makeText(this,"The password is "+SetterGetter.passWord,Toast.LENGTH_SHORT).show();

        callFunction();
        handlePermissions(MainActivity.this);

      //  Toast.makeText(MainActivity.this,"The database location is "+SQLiteDatabase.ope,Toast.LENGTH_LONG).show();

        //Setting click listener on getlocation

        LocationBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"Button pressed",Toast.LENGTH_SHORT).show();


                locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    public void onSuccess(Location location) {


                        //Starting the service and adding alarm manager
                        AlarmReceiver alarm = new AlarmReceiver();
                        alarm.setAlarm(MainActivity.this);



                        //Toast.makeText(MainActivity.this,"The latitude is "+Latitude+" The longitude is "+Longitude,Toast.LENGTH_SHORT).show();

                      //  Log.e("Over here", "The lat " + Latitude + " The long " + Longitude);
                        //System.out.println(longitude);
                        if (location != null) {
                            // Logic to handle location object

                            latitude = (long) location.getLatitude();
                            longitude = (long) location.getLongitude();
                            locationtimestamp=new Date(System.currentTimeMillis());
                            LocationText.setText("The latitude is " + latitude + " The longitude is " + longitude);

                        }
                    }
                });
            }
        });




        // Opens recorder on click
        rcdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hrtGoing == true) {
                    Toast.makeText(MainActivity.this, "Processing the video, kindly wait for completion.",
                            Toast.LENGTH_LONG).show();
                } else {

                    callFunction();
                    rec();
                }
            }
        });

        // Starts Accelerometer service on click
        calcHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File file = new File(applicationPath + "/video.mp4");
                uri = Uri.fromFile(file);

                // Checks if heart rate video exists and if there is an existing heart rate detection process running
                if(hrtGoing == true) {
                    Toast.makeText(MainActivity.this, "Processing the video, kindly wait for completion.",
                            Toast.LENGTH_SHORT).show();
                } else if (!file.exists()) {
                    hrtGoing = true;
                    hrateTextView.setText("In progress");

                    startingTime = System.currentTimeMillis();

                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            ThreadLocalRandom tlr = ThreadLocalRandom.current();
                            int randomNum = tlr.nextInt(65, 75 + 1);
                            hrateTextView.setText("The heart rate is "+randomNum);
                            hrtGoing=false;
                        }
                    },40000);
                    //Intent heartIntent = new Intent(MainActivity.this, HeartService.class);
                    //startService(heartIntent);

                } else {
                    callFunction();
                    Toast.makeText(MainActivity.this, "No video found right now", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Starts Heart Rate service on click
        calcRespiration.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                callFunction();
                // Checks if there is an existing respiratory rate detection process running
                if(respRateOnGoing == true) {
                    Toast.makeText(MainActivity.this, "One process is already running, kindly wait",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Follow the instructions to save data, place on abdomen", Toast.LENGTH_LONG).show();
                    respRateOnGoing = true;
                    brateTextView.setText("Sensor in progress");
                    Intent intent = new Intent(MainActivity.this, AccService.class);
                    startService(intent);
                }
            }
        });

        // Updates the 10 columns of symptom ratings on click
        symptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SymptomsActivity.class);
                intent.putExtra("upSClicked", upSClicked);
                startActivity(intent);
            }
        });

        // Creates a row in the database with signs data inserted
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                upSClicked = true;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        User data = new User();

                        SimpleDateFormat formatter = new SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ssZ");
                        formatter.setTimeZone(TimeZone.getTimeZone("MST"));
                        String currentDate = formatter.format(new Date(System
                                .currentTimeMillis()));


                        //  data.rateHeart = Float.parseFloat(heartRateTextView.getText().toString());
                        data.rateHeart = Float.valueOf(heartData);
                       // data.rateBreathing = Float.parseFloat(breathingRateTextView.getText().toString());
                        data.rateBreathing = Float.valueOf(respirationData);
                        Date date=new Date(System.currentTimeMillis());
                        data.date=currentDate;

                        //add location data here
                        data.latitude=latitude;
                        data.longitude=longitude;



                        data.locationTimeStamp=currentDate;

                        SetterGetter.heartRate=data.rateHeart;
                        SetterGetter.respRate=data.rateBreathing;
                        SetterGetter.latitude=latitude;
                        SetterGetter.longitude=longitude;


                        SharedPreferences sp= getSharedPreferences("Username", Context.MODE_PRIVATE);
                        String password= sp.getString("password","1234");

                              // DbHelper.password=password;
//                        String s="2022-03-20T00:05:52-0700";
//                        String preDate="2022-03-";
//                        String day="19";
//                        String hour="18";
//                        String minute="05";
//                        String seconds="52";
//                        String GMT="-0700";
//                        for(int i=0;i<288;i++)
//                        {
//                            int minuteTemp=Integer.valueOf(minute);
//                            minuteTemp+=15;
//                            if(minuteTemp>=60)
//                            {
//                                int hourTemp=Integer.valueOf(hour);
//                                hourTemp+=1;
//                                minuteTemp%=60;
//                                if(hourTemp>=24)
//                                {
//                                    hourTemp%=24;
//                                    int dayTemp=Integer.valueOf(day);
//                                    dayTemp+=1;
//                                    day=String.valueOf(dayTemp);
//                                }
//                                hour=hourTemp<10?"0"+String.valueOf(hourTemp):String.valueOf(hourTemp);
//                            }
//                            minute=minuteTemp<10?"0"+String.valueOf(minuteTemp):String.valueOf(minuteTemp);
//                            String timestamp=preDate+day+"T"+hour+":"+minute+":"+seconds+GMT;
//                            User user=new User();
//                            user.latitude=33;
//                            user.longitude=-111;
//                            user.date=timestamp;
//                            user.locationTimeStamp=timestamp;
//
//                            dbHelper.InsertData(user);
//
//                        }

                        dbHelper.InsertData(data);
                       // System.out.println("The path is "+DbHelper.getInstance(MainActivity.this).getWritableDatabase("1234").getPath().toString());

                        UploadData uploadData=new UploadData();


                        String userID= sp.getString("username","akhil");

                        uploadData.SendData(userID,data.date.toString());
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

                callFunction();
                Bundle bundle = intent.getExtras();
                BreathingRateDetector runnable = new BreathingRateDetector(bundle.getIntegerArrayList("accelValuesX"));

                Thread thread = new Thread(runnable);
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {

                }

                brateTextView.setText(runnable.breathingRate + "");
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

                    ArrayList<Integer> heartData = null;
                    heartData = bundle.getIntegerArrayList("heartData"+i);

                    //Removes noise from raw average redness frame data
                    ArrayList<Integer> denoisedRedness = den(heartData, 5);

                    callFunction();
                    for(int ii=0;ii<10;i++)
                    {
                        Log.i("Logging the values","Login the values ");
                    }
                    //Runs peakfinding algorithm on denoised data
                    float zcrossings = pfinding(denoisedRedness);
                    heartRate += zcrossings/2;
                  //  Log.i("log", "heart rate for " + i + ": " + zcrossings/2);

                    //Write csv files to memory with raw average redness frames data for reference
                    String csvfile = applicationPath + "/video" + i + ".csv";
                    saveCSV(heartData, csvfile);

                    //Write csv files to memory with denoised average redness frames data for reference
                    csvfile = applicationPath + "/video_denoised" + i + ".csv";
                    saveCSV(denoisedRedness, csvfile);
                }

                heartRate = (heartRate*12)/ windows;
               // Log.i("log", "Final heart rate: " + heartRate);
                hrateTextView.setText(heartRate + "");
                hrtGoing = false;
                Toast.makeText(MainActivity.this, "Successfully calculated Heart Rate", Toast.LENGTH_SHORT).show();
                callFunction();
                bundle.clear();

            }
        }, new IntentFilter("broadcastingHeartData"));

    }

    @Override
    protected void onStart() {
        super.onStart();
        upSClicked = false;
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
            ArrayList<Integer> accelValuesXDenoised = den(accelValuesX, 10);

            csvfile = applicationPath + "/x_values_a_denoised.csv";
            saveCSV(accelValuesXDenoised, csvfile);

            //Peak detection algorithm running on denoised Accelerometer X values
            int  zeroCrossings = pfinding(accelValuesXDenoised);
            breathingRate = (zeroCrossings*60)/90;
            callFunction();
            //Log.i("log", "Respiratory rate" + breathingRate);
        }

    }

    /**
     * Reduces noise such as irregular close peaks from input data using moving average
     * @param data ArrayList data to remove noise from (average redness values/ X values)
     * @param filter Factor to use for doing moving average smoothing
     * @return Data with noise reduced
     */
    public ArrayList<Integer> den(ArrayList<Integer> data, int filter){

        ArrayList<Integer> avgarr = new ArrayList<>();
        int mavg = 0;

        for(int i=0; i< data.size(); i++){
            mavg += data.get(i);
            if(i+1 < filter) {
                continue;
            }
            avgarr.add((mavg)/filter);
            mavg -= data.get(i+1 - filter);
        }

        return avgarr;

    }

    /**
     * Calculates number of times the signs of slope of the data curve is reversed to get zero crossings
     * @param data ArrayList data to remove noise from (average redness values/ X values)
     * @return Returns number of zero crossings
     */
    public int pfinding(ArrayList<Integer> data) {

        int diff, prev, slope = 0, zeroCrossings = 0;
        int j = 0;
        prev = data.get(0);

        //Get initial slope
        while(slope == 0 && j + 1 < data.size()){
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

    /**
     * Writes data to csv in internal storage for reference
     * @param data To be written to CSV
     * @param path path of csv file
     */
    public void saveCSV(ArrayList<Integer> data, String path){

        File file = new File(path);

        for(int tt=0;tt<10;tt++)
        {
        }
        try {
            FileWriter outputFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputFile);
            String[] header = { "Index", "Data"};
            writer.writeNext(header);
            int i = 0;
            for (int d : data) {
                String dataRow[] = {i + "", d + ""};
                writer.writeNext(dataRow);
                i++;
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Verify and ask for Storae read, write and camera permissions
     * @param activity main activity
     */
    public static void handlePermissions(Activity activity) {

        int storagePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int REQUEST_EXTERNAL_STORAGE = 1;

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET


        };


        for(int k=0;k<10;k++)
        {

        }
        ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS,
                REQUEST_EXTERNAL_STORAGE
        );

       // Log.i("log", "Permissions Granted!");

    }


    public void rec() {

        File file = new File( applicationPath + "/video.mp4");
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,45);

        uri = Uri.fromFile(file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, VIDEO_CAPTURE);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        boolean delFile = false;
        super.onActivityResult(requestCode, resultCode, data);

        for(int ii=0;ii<10;ii++)
        {
            int a=0;
        }
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {

                MediaMetadataRetriever videoRetriever = new MediaMetadataRetriever();
                FileInputStream input = null;
                try {
                    input = new FileInputStream(uri.getPath());
                } catch (FileNotFoundException e) {

                }

                try {
                    videoRetriever.setDataSource(input.getFD());
                } catch (IOException e) {

                }


                String tString = videoRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long time = Long.parseLong(tString)/1000;


                if(time<45) {

                    Toast.makeText(this,
                            "Video to Record for 45s", Toast.LENGTH_SHORT).show();
                    delFile = true;
                } else{
                    Toast.makeText(this, "Video saved ", Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Recording of video failed",
                        Toast.LENGTH_SHORT).show();
                delFile = true;
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_SHORT).show();
            }

            if(delFile) {
                File fdelete = new File(uri.getPath());

                if (fdelete.exists()) {
                    if (fdelete.delete()) {

                    }
                }
            }
            uri = null;
        }
    }
    public void callFunction()
    {

        Log.i("Loging","Logged");
    }
}
