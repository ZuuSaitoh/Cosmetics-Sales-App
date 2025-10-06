package com.example.myapplication.network;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.auth.TokenInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static volatile Retrofit retrofitInstance;

    private ApiClient() {}

    public static Retrofit getRetrofit(Context context) {
        if (retrofitInstance == null) {
            synchronized (ApiClient.class) {
                if (retrofitInstance == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d("Api", message));
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    AuthManager authManager = new AuthManager(context.getApplicationContext());

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new TokenInterceptor(authManager))
                            .addInterceptor(logging)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build();

                    retrofitInstance = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofitInstance;
    }
}


