package com.example.myapplication;

import android.os.Bundle;
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
    }
}
