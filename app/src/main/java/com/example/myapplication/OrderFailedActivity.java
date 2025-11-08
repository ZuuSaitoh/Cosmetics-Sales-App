package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OrderFailedActivity extends AppCompatActivity {

    private int orderId;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_failed);

        Intent intent = getIntent();
        orderId = intent.getIntExtra("orderId", 0);
        amount = intent.getDoubleExtra("amount", 0.0);

        Button btnPayAgain = findViewById(R.id.btn_pay_again);
        Button btnBackToHome = findViewById(R.id.btn_back_to_home);

        if (orderId != 0 && amount != 0.0) {
            btnPayAgain.setVisibility(View.VISIBLE);
            btnPayAgain.setOnClickListener(v -> {
                Intent vnPayIntent = new Intent(OrderFailedActivity.this, VNPayActivity.class);
                vnPayIntent.putExtra("orderId", orderId);
                vnPayIntent.putExtra("amount", amount);
                startActivity(vnPayIntent);
                finish();
            });
        } else {
            btnPayAgain.setVisibility(View.GONE);
        }

        btnBackToHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(OrderFailedActivity.this, ActivityMain.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }
}
