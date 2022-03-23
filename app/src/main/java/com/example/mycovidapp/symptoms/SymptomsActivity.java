package com.example.symptomTrackerApp.symptoms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;

import com.example.symptomTrackerApp.dbAdapter.ApplicationDataBase;
import com.example.symptomTrackerApp.dbAdapter.User;
import com.example.symptomTrackerApp.dbAdapter.DbHelper;

import net.sqlcipher.database.SQLiteDatabase;

import java.sql.Date;

import mycovidapp.R;

public class SymptomsActivity extends AppCompatActivity {

     float[] ratings = new float[10];
     Spinner spin;
     RatingBar ratingBar;
     Button uploadButton;
     ApplicationDataBase db;
     User userData = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_screen);


        SQLiteDatabase.loadLibs(this);
        uploadButton = findViewById(R.id.uploadSymptoms);
        ratingBar = findViewById(R.id.ratingBar);
        spin = findViewById(R.id.symptomsRating);

        // symtopms list {"nausea","headache","diarrhea","Soar Throat","fever","Muscle Ache","Loss of Smell or Taste","Cough","Shortness of Breath","Feeling Tired"};
        ArrayAdapter<CharSequence> adpt = ArrayAdapter.createFromResource(this, R.array.symptoms_array, android.R.layout.simple_spinner_item);
        adpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        callmethod();
        spin.setAdapter(adpt);

        //Gets app database
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    db = ApplicationDataBase.getInstance(getApplicationContext());
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

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userData.fever = ratings[0];
                userData.cough = ratings[1];
                userData.tired = ratings[2];
                userData.shortnessOfBreath = ratings[3];
                userData.MuscleAche = ratings[4];
                userData.diarrhea = ratings[5];
                userData.soarThroat = ratings[6];
                userData.lossOfSmell = ratings[7];
                userData.headache = ratings[8];
                userData.nausea = ratings[9];
                userData.dateTime = new Date(System.currentTimeMillis());

                boolean upSClicked = getIntent().getExtras().getBoolean("uploadSymptomsClicked");

                Thread thread;
                if(upSClicked) {
                    thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            User latestData = db.userInfoDao().getLatestData();
                            userData.rateHeart = latestData.rateHeart;
                            userData.rateBreathing = latestData.rateBreathing;
                            userData.id = latestData.id;

                            SharedPreferences sp = getSharedPreferences("Username", Context.MODE_PRIVATE);

                            DbHelper.password = sp.getString("password", "1234");


                            DbHelper.getInstance(SymptomsActivity.this).InsertData(latestData);

                        }
                    });

                } else {
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences sp = getSharedPreferences("Username", Context.MODE_PRIVATE);

                            DbHelper.password = sp.getString("password", "1234");
                            DbHelper.getInstance(SymptomsActivity.this).InsertData(userData);
                        }
                    });
                }
                thread.start();
                Toast.makeText(SymptomsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
            }
        });


        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ratingBar.setRating(ratings[i]);
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

}