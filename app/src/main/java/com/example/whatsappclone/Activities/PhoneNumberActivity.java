package com.example.whatsappclone.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.whatsappclone.databinding.ActivityPhoneNumberBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneNumberActivity extends AppCompatActivity {

    //Variable Declarations
    ActivityPhoneNumberBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        //to automatically pop up keyboard
        binding.phoneBox.requestFocus();
        binding.phoneBox.setText("+91");
        binding.phoneBox.setSelection(3);

        auth = FirebaseAuth.getInstance();

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = binding.phoneBox.getText().toString();
                if(number.length() < 13)
                {
                    binding.phoneBox.setError("Enter a Valid Phone Number(Make Sure to Add +91 code)");
                }
                else
                {
                    Intent intent = new Intent(PhoneNumberActivity.this, otpActivity.class);
                    intent.putExtra("phoneNumber", number);
                    startActivity(intent);
                }

            }
        });
    }
}