package com.example.symptomTrackerApp.dbAdapter;

import android.content.ContentValues;
import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper instance;
    public static final int DATABASE_VER=1;
    public static final String DATABASE_NAME="dixit.db"; //Can also change it according to username

    public static final String TABLE_NAME="UserData";

    public static String password="1234";

    private static final String SQL_TABLE_QUERY =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "id" + " INTEGER PRIMARY KEY," +
                    "date" + " REAL, " +
                    "Headache" + " TEXT, " +
                    "Nausea" + " REAL," +
                    "rateHeart" + " REAL, " +
                    "rateBreathing" + " REAL, " +
                    "Fever" + " REAL, " +
                    "cough" + " REAL, " +
                    "tired" + " REAL, " +
                    "shortnessOfBreath" + " REAL, " +
                    "MuscleAche" + " REAL, " +
                    "Diarrhea" + " REAL, " +
                    "soarThroat" + " REAL, " +
                    "lossOfSmell" + " REAL, " +
                    "latitude" + " REAL, " +
                    "longitude" + " REAL, " +
                    "locationTimeStamp" + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    static public synchronized DbHelper getInstance(Context context)
    {
        if(instance==null)
        {
            instance=new DbHelper(context);
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void InsertData(User user)
    {
        SQLiteDatabase db= instance.getWritableDatabase(password.toCharArray());
        ContentValues values=new ContentValues();
        values.put("id",user.id);
        values.put("dateTime",String.valueOf(user.dateTime));
        values.put("headache",user.headache);
        values.put("nausea",user.nausea);
        values.put("rateHeart",user.rateHeart);

        values.put("rateBreathing",user.rateBreathing);
        values.put("fever",user.fever);
        values.put("cough",user.cough);

        values.put("tired",user.tired);
        values.put("shortnessOfBreath",user.shortnessOfBreath);
        values.put("MuscleAche",user.MuscleAche);



        values.put("diarrhea",user.diarrhea);
        values.put("soarThroat",user.soarThroat);
        values.put("lossOfSmell",user.lossOfSmell);

        values.put("latitude",user.latitude);
        values.put("longitude",user.longitude);
        values.put("locationTimeStamp",String.valueOf(user.locationTimeStamp));

        db.insert(TABLE_NAME,null,values);
        db.close();



    }
}
