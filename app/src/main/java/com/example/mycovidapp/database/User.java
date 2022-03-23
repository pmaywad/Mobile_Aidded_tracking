package com.example.symptomTrackerApp.dbAdapter;

import androidx.room.PrimaryKey;
import androidx.room.Entity;
import java.util.Date;

@Entity
public class User {
    @PrimaryKey (autoGenerate = true)
    public int id;
    public Date dateTime;
    public float headache;//done
    public float nausea;//done
    public float rateHeart;
    public float rateBreathing;
    public float fever; //done
    public float cough;//done
    public float tired;//done
    public float shortnessOfBreath;//done
    public float MuscleAche;//done
    public float diarrhea;//done
    public float soarThroat;//done
    public float lossOfSmell;//done

    //Location Data
    public long latitude;
    public long longitude;
    public Date locationTimeStamp;



    public User() {
        //Initializing all values to 0 so that when we save, the ones that are not inputed are turned to 0
        soarThroat = 0;
        lossOfSmell = 0;
        headache = 0;
        fever = 0;
        cough = 0;
        tired = 0;
        shortnessOfBreath = 0;
        MuscleAche = 0;
        diarrhea = 0;
        nausea = 0;
        latitude=0;
        longitude=0;

    }
}
