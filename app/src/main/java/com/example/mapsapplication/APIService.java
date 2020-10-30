package com.example.mapsapplication;

import com.example.mapsapplication.Notifications.MyResponse;
import com.example.mapsapplication.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAARnwSw_0:APA91bEpSYoGffkdT0vJFuXZg5Q7Z-gSxsZ8mSvhP_M0LbtRok4ldy7i-xe8HQduAA8RAGQJ6SDUS-yCxDqPfkobxAgDmL5MZTam7ZgT5i5bEHIFG8V7FR62VfMEKXaoXUAfv18GDH0d"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}