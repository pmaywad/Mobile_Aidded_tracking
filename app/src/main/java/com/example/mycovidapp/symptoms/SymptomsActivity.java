package com.example.mycovidapp.symptoms;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;

import com.example.mycovidapp.database.User;
import com.example.mycovidapp.gps.AlarmReceiver;
import com.example.mycovidapp.newdatabase.DbHelper;
import com.example.mycovidapp.newdatabase.SetterGetter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import mycovidapp.R;

public class SymptomsActivity extends AppCompatActivity {

     float[] ratings = new float[10];
     Spinner spin;
     RatingBar ratingBar;
     //ApplicationDataBase db;
     DbHelper dbHelper;
     User userData = new User();
    FusedLocationProviderClient locationProviderClient;
    long latitude;
    long longitude;
    String timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_screen);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        Button udBtn = (Button) findViewById(R.id.button2);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        spin = (Spinner) findViewById(R.id.symptoms_spinner);

        int t=0;
        for(int i=0;i<10;i++)
        {
            t++;
        }
        String[] val=new String[]{"Nausea","Headache","Diarrhea","Soar Throat","Fever","Muscle Ache","Loss of Smell or Taste","Cough","Shortness of Breath","Feeling Tired"};
        ArrayAdapter<CharSequence> adpt = ArrayAdapter.createFromResource(this, R.array.symptoms_array, android.R.layout.simple_spinner_item);
        adpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        callmethod();
        spin.setAdapter(adpt);

        //Gets app database
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                   // db = ApplicationDataBase.getInstance(getApplicationContext());
                    dbHelper=DbHelper.getInstance(getApplicationContext());
                } catch (Exception e) {
                   Log.i("Inside exception","Exception is here");
                }
            }
        });

        thread.start();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                int i = spin.getSelectedItemPosition();
                ratings[i] = v;
            }
        });

        udBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                getLocation();
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ssZ");
                formatter.setTimeZone(TimeZone.getTimeZone("MST"));
                String currentDate = formatter.format(new Date(System
                        .currentTimeMillis()));

                userData.Fever = ratings[0];
                userData.cough = ratings[1];
                userData.tired = ratings[2];
                userData.shortnessOfBreath = ratings[3];
                userData.MuscleAche = ratings[4];
                userData.Diarrhea = ratings[5];
                userData.soarThroat = ratings[6];
                userData.lossOfSmell = ratings[7];
                userData.Headache = ratings[8];
                userData.Nausea = ratings[9];
              //  Date date=new Date(System.currentTimeMillis());
                userData.date = currentDate;
                userData.locationTimeStamp= currentDate;
                userData.latitude=latitude;
                userData.longitude=longitude;

                boolean upSClicked = getIntent().getExtras().getBoolean("upSClicked");

                //If new row created by Upload Signs button then update that row
                // else create a new row with empty signs and new symptom ratings
                if(upSClicked == true) {
                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                           // User latestData = DbHelper.getLatest();
//
//                            userData.rateHeart = SetterGetter.heartRate;
//                            userData.rateBreathing = SetterGetter.respRate;
//                            userData.id = SetterGetter.id;
//                            db.userInfoDao().update(userData);
//                            userData.rateHeart = latestData.rateHeart;
//                            userData.rateBreathing = latestData.rateBreathing;
//                            userData.id = latestData.id;

                            DbHelper.getInstance(SymptomsActivity.this).InsertData(userData);
                           // db.userInfoDao().update(userData);
                        }
                    });
                    thread.start();

                } else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            SharedPreferences sp= getSharedPreferences("Username", Context.MODE_PRIVATE);
                            String password= sp.getString("password","1234");

                            DbHelper.password=password;


                            DbHelper.getInstance(SymptomsActivity.this).InsertData(userData);

                          //  db.userInfoDao().insert(userData);
                        }
                    });
                    thread.start();
                }
                Toast.makeText(SymptomsActivity.this, "Upload done", Toast.LENGTH_SHORT).show();
            }
        });


        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ratingBar.setRating(ratings[i]);
                for(int ii=0;ii<10;ii++)
                {
                    Log.i("Loging","In adapter");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
    public void callmethod()
    {
        Log.i("Value added","Values are being added");
    }

    @SuppressLint("MissingPermission")
    public void getLocation()
    {
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            public void onSuccess(Location location) {


                //Starting the service and adding alarm manager
                AlarmReceiver alarm = new AlarmReceiver();
                alarm.setAlarm(SymptomsActivity.this);



                //Toast.makeText(MainActivity.this,"The latitude is "+Latitude+" The longitude is "+Longitude,Toast.LENGTH_SHORT).show();

                //  Log.e("Over here", "The lat " + Latitude + " The long " + Longitude);
                //System.out.println(longitude);
                if (location != null) {
                    // Logic to handle location object
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ssZ");
                    formatter.setTimeZone(TimeZone.getTimeZone("MST"));
                    String currentDate = formatter.format(new Date(System
                            .currentTimeMillis()));

                    latitude = (long) location.getLatitude();
                    longitude = (long) location.getLongitude();
                    timestamp=currentDate;
                    //LocationText.setText("The latitude is " + latitude + " The longitude is " + longitude);

                }
            }
        });
    }
}