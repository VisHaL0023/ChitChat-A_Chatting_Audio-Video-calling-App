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
        //This is to keep you SignIn One logged In even When App is Closed until you logout
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(PhoneNumberActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhoneNumberActivity.this, otpActivity.class);
                String number = binding.phoneBox.getText().toString();
                if(number.length() < 13)
                {
                    binding.phoneBox.setError("Enter a Valid Phone Number(Make Sure to Add +91 also)");
                }
                else
                {
                    intent.putExtra("phoneNumber", number);
                    startActivity(intent);
                }

            }
        });
    }
}