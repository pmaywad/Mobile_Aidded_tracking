package com.example.mycovidapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@Database(entities = {User.class}, version = 1)
@TypeConverters(ConverterDB.class)
public abstract class ApplicationDataBase extends RoomDatabase {
    public abstract UserDao userInfoDao();
    private static ApplicationDataBase dbInstance;
    public static synchronized ApplicationDataBase getInstance(Context context){
        //Create new database with last name for a name if none exist
        if(dbInstance == null){


//            SharedPreferences sharedPref = context.getSharedPreferences("login",Context.MODE_PRIVATE);
//            String pass= sharedPref.getString("password","1234");
            //Adding passcode on the database
            String pass="1234";
            byte[] passphrase=pass.getBytes();
            final SupportFactory factory = new SupportFactory(SQLiteDatabase.getBytes(pass.toCharArray()));
            dbInstance = Room.databaseBuilder(context.getApplicationContext(), ApplicationDataBase.class, "dixit.db")
                    //.openHelperFactory(factory)
                    .build();


        }
        return dbInstance;
    }
}
