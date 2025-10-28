package com.example.myapplication.network;

import com.example.myapplication.model.Payment;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentService {
    @POST("/payment/create")
    Call<Void> createPayment(@Body Payment payment);
}
