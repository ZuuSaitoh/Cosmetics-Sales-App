package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CustomerProfileActivity extends AppCompatActivity {
    EditText edtEmail, edtPhone, edtAddress;
    Button btnSave, btnCancel;
    ImageButton btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        btnEdit.setOnClickListener(v -> {
            edtEmail.setEnabled(true);
            edtPhone.setEnabled(true);
            edtAddress.setEnabled(true);
            btnSave.setEnabled(true);
            btnCancel.setEnabled(true);
        });

        btnCancel.setOnClickListener(v -> {
            edtEmail.setEnabled(false);
            edtPhone.setEnabled(false);
            edtAddress.setEnabled(false);
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
        });

        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Profile updated (frontend only)", Toast.LENGTH_SHORT).show();
            edtEmail.setEnabled(false);
            edtPhone.setEnabled(false);
            edtAddress.setEnabled(false);
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
        });
    }
}
