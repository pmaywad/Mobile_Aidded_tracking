package com.example.mycovidapp.newdatabase;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;


import com.example.mycovidapp.database.User;
//
//import net.sqlcipher.database.SQLiteDatabase;
//import net.sqlcipher.database.SQLiteOpenHelper;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.apache.commons.lang3.builder.EqualsExclude;

import java.io.File;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper instance;
    public static final int DATABASE_VER=1;
    public static final String DATABASE_NAME="dixit.db"; //Can also change it according to username
    File file;
    public static final String TABLE_NAME="UserData";

    public static String password=SetterGetter.passWord;

    private static final String SQL_TABLE_QUERY =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "id" + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "date" + " TEXT, " +
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
                    "locationTimeStamp" + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    static public synchronized DbHelper getInstance(Context context)
    {
        if(instance==null)
        {
           //instance=SQLiteDatabase.openOrCreateDatabase(new File("data/data"))
            instance=new DbHelper(context);
        }

        return instance;
    }

    public static void getPassword()
    {
       // SharedPreferences preferences = getSharedPreferences("Username", Context.MODE_PRIVATE);
    }

    public static User getLatest()
    {
        User user=new User();

        //SQLiteDatabase db= instance.getReadableDatabase(password.toCharArray());
        SQLiteDatabase database=instance.getReadableDatabase(password.toCharArray());
        String q= "SELECT MAX(id) FROM "+TABLE_NAME;

        Cursor c= database.rawQuery(q,null);
        c.moveToFirst();
        if(c!=null) {
            int heartRate = c.getColumnIndex("rateHeart");
            System.out.println("The indexxxxxxxxxx is " + heartRate);

            user.rateHeart = c.getFloat(heartRate);

            int respRate = c.getColumnIndex("rateBreathing");
            user.rateBreathing = c.getFloat(respRate);

            int latitude = c.getColumnIndex("latitude");
            user.latitude = c.getFloat(latitude);

            int longitude = c.getColumnIndex("longitude");
            user.longitude = c.getFloat(longitude);

            int id = c.getColumnIndex("id");
            user.id = c.getInt(id) + 1;
        }
        return user;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        System.out.println("Helllllllooooooooooo");
    }

    public void InsertData(User user)
    {
        //SQLiteDatabase db= this.getWritableDatabase(password.toCharArray());

//        SQLiteDatabase database=this.getReadableDatabase();
//        String q= "SELECT MAX(id) FROM "+TABLE_NAME;
//        Cursor c= database.rawQuery(q,null);
//
//        int id=1;
////        if(c.moveToFirst())
////            id=c.getInt(0);
//        c.moveToFirst();
//        id=c.getInt(0)+1;
//
//        System.out.println("The id is "+id);


//        c.close();
//        database.close();

        SQLiteDatabase db= this.getWritableDatabase(password.toCharArray());
        ContentValues values=new ContentValues();
        //values.put("id",user.id);
        values.put("date",String.valueOf(user.date));
        values.put("Headache",user.Headache);
        values.put("Nausea",user.Nausea);
        values.put("rateHeart",user.rateHeart);

        values.put("rateBreathing",user.rateBreathing);
        values.put("Fever",user.Fever);
        values.put("cough",user.cough);

        values.put("tired",user.tired);
        values.put("shortnessOfBreath",user.shortnessOfBreath);
        values.put("MuscleAche",user.MuscleAche);

        values.put("Diarrhea",user.Diarrhea);
        values.put("soarThroat",user.soarThroat);
        values.put("lossOfSmell",user.lossOfSmell);

        values.put("latitude",user.latitude);
        values.put("longitude",user.longitude);
        values.put("locationTimeStamp",String.valueOf(user.locationTimeStamp));


        db.insert(TABLE_NAME,null,values);
        db.close();
    }

//    @Override
//    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//
//
//        sqLiteDatabase.execSQL(SQL_TABLE_QUERY);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
//        System.out.println("Helllllllooooooooooo");
//    }
}
