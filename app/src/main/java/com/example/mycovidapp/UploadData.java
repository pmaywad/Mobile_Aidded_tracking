package com.example.symptomTrackerApp;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UploadData{

    private String url = "http://3.145.124.148:8080/api/upload";

    public void SendData(String id,String date)
    {

       // URI uri= URI.parse("/data/user/0/com.example.mycovidapp/databases/dixit.db");
        try {
            File file = new File("/data/user/0/com.example.mycovidapp/databases/dixit.db");

            if(file==null)
                System.out.println("File is not available");
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("myfile", file.getName(), RequestBody.create(MediaType.parse("application/x-sqlite3"), file))
                    .addFormDataPart("subjectID", id)
                    .addFormDataPart("dateTime",date)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();


            okHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(final Call call, final IOException e) {
                    // Handle the error
                    System.out.println("Error is "+e);
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        // Handle the error
                        System.out.println("error");
                    }
                    System.out.println("uploaded");
                }
            });
        }
        catch (Exception e)
        {
            System.out.println("The exception stack is "+e.toString());
        }

}
}
