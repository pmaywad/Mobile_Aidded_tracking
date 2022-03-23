package com.example.mycovidapp.gps;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.example.mycovidapp.database.User;
import com.example.mycovidapp.newdatabase.DbHelper;
import com.example.mycovidapp.newdatabase.SetterGetter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import net.sqlcipher.database.SQLiteDatabase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class GpsService extends Service {

    FusedLocationProviderClient locationProviderClient;
    private String applicationPath = Environment.getExternalStorageDirectory().getPath();

    public long latitude;
    public long longitude;

    // This method run only one time. At the first time of service created and running
    @Override
    public void onCreate() {
        SQLiteDatabase.loadLibs(this);
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Log.d("onCreate()", "After service created");
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Here is the source of the TOASTS :D
        Toast.makeText(this, "Freshly Made toast!", Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = getSharedPreferences("Username", Context.MODE_PRIVATE);
        SetterGetter.passWord=preferences.getString("password","1234");

        final Context context=this;
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//           // ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            return Service.START_NOT_STICKY;
//        }
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            public void onSuccess(Location location) {


                //System.out.println(longitude);
                if (location != null) {
                    // Logic to handle location object
                    latitude = (long) location.getLatitude();
                    longitude = (long) location.getLongitude();

                    //Adding to database


                    Thread thread=new Thread(new Runnable() {
                        @Override
                        public void run() {

                            SimpleDateFormat formatter = new SimpleDateFormat(
                                    "yyyy-MM-dd'T'HH:mm:ssZ");
                            formatter.setTimeZone(TimeZone.getTimeZone("MST"));
                            String currentDate = formatter.format(new Date(System
                                    .currentTimeMillis()));

                            User user=new User();
                            user.latitude=latitude;
                            user.longitude=longitude;
                            user.date=currentDate;
                          //  Date d=new Date(System.currentTimeMillis());
                            user.locationTimeStamp=currentDate;

//                            User latestData = DbHelper.getLatest();
//                            user.id=latestData.id;

                            DbHelper.getInstance(context).InsertData(user);

                            //db.userInfoDao().insert(user);
                        }
                    });
                    thread.start();
                    Toast.makeText(context,"The latitude is "+latitude+" The longitude is "+longitude,Toast.LENGTH_SHORT).show();

                    Log.e("Over here","The lat "+latitude+" The long "+longitude);

                }
            }
        });
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding
        return null;
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            String latitude = "Latitude: " + loc.getLatitude();


        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

}