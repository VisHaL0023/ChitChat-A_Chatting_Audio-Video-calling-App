package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.whatsappclone.databinding.ActivityOtpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

public class otpActivity extends AppCompatActivity {

    //Variables
    ActivityOtpBinding binding;
    FirebaseAuth auth;
    String verificationId;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.phoneLable.setText("Verify " + phoneNumber);

        getSupportActionBar().hide();
        //keyboard popup
        binding.otpView.requestFocus();
        //Progress Dialog
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();
        //FireBase Authentication
        auth = FirebaseAuth.getInstance();
        //Verification using Phone Number
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(otpActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        dialog.dismiss();
                        verificationId = verifyId;
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        //to check that the Entered OTP matches the Arrived OTP
        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(otpActivity.this, setUpProfileActivity.class);
                            startActivity(intent);
                            //it finishes all the previously opened Activities
                            finishAffinity();
                        } else {
                            Toast.makeText(otpActivity.this, "Failed to SignIn", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}