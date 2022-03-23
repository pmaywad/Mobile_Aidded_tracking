package com.example.mycovidapp.database;

import androidx.room.PrimaryKey;
import androidx.room.Entity;
import java.util.Date;

@Entity
public class User {
    @PrimaryKey (autoGenerate = true)
    public int id;
    public String date;
    public float Headache;//done
    public float Nausea;//done
    public float rateHeart;
    public float rateBreathing;
    public float Fever; //done
    public float cough;//done
    public float tired;//done
    public float shortnessOfBreath;//done
    public float MuscleAche;//done
    public float Diarrhea;//done
    public float soarThroat;//done
    public float lossOfSmell;//done

    //Location Data
    public float latitude;
    public float longitude;
    public String locationTimeStamp;



    public User() {
        //Initializing all values to 0 so that when we save, the ones that are not inputed are turned to 0
        soarThroat = 0;
        lossOfSmell = 0;
        Headache = 0;
        Fever = 0;
        cough = 0;
        tired = 0;
        shortnessOfBreath = 0;
        MuscleAche = 0;
        Diarrhea = 0;
        Nausea = 0;
        latitude=0;
        longitude=0;

    }
}
