package com.example.myapplication;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String username = getIntent().getStringExtra("username");
        TextView greetingText = findViewById(R.id.greetingText);
        greetingText.setText("Xin chào, " + (username != null ? username : ""));

        MaterialButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> finishAffinity());

        MaterialButton btnOpenAdmin = findViewById(R.id.btnOpenAdminDashboard);
        boolean isAdmin = username != null && "admin".equalsIgnoreCase(username);
        if (!isAdmin) {
            btnOpenAdmin.setVisibility(View.GONE);
        } else {
            btnOpenAdmin.setVisibility(View.VISIBLE);
            btnOpenAdmin.setOnClickListener(v -> {
                Intent i = new Intent(HomeScreen.this, AdminDashboardActivity.class);
                startActivity(i);
            });
        }
    }
}
