package com.example.whatsappclone.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivitySplashScreenBinding;
import com.google.firebase.auth.FirebaseAuth;

public class splashScreen extends AppCompatActivity {

    FirebaseAuth auth;
    //for animation
    ActivitySplashScreenBinding binding;
    Animation topAnim, bottomAnim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        //for hiding title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //for anim
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        binding.loginAnimation.setAnimation(topAnim);
        binding.appName.setAnimation(bottomAnim);
        binding.tagline.setAnimation(bottomAnim);


        auth = FirebaseAuth.getInstance();
        //For giving some delay then Open next Activity
        Thread t1 = new Thread() {

            public void run() {
                try {
                    sleep(1900);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (auth.getCurrentUser() != null) {
                        Intent intent = new Intent(splashScreen.this, MainActivity.class);
                        startActivity(intent);

                    } else {
                        Intent intent = new Intent(splashScreen.this, PhoneNumberActivity.class);
                        startActivity(intent);

                    }
                    finish();

                }
            }
        };
        t1.start();
    }
}