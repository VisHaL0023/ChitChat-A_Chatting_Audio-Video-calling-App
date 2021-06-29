package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.example.whatsappclone.Adapters.CallAdapter;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.databinding.ActivityCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

public class CallActivity extends AppCompatActivity {


    ActivityCallBinding binding;
    FirebaseDatabase database;
    ArrayList<Users> users;
    FirebaseAuth auth;
    CallAdapter callAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        users = new ArrayList<>();

        //**************************************************************************************************
       // callAdapter = new CallAdapter(this,users);
        //********************************************************************************************i have commmented it

        binding.callRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.callRecyclerView.setLayoutManager(linearLayoutManager);
        binding.callRecyclerView.setAdapter(callAdapter);


        binding.callRecyclerView.showShimmerAdapter();

        //getting the data from the firebase
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Users user = snapshot1.getValue(Users.class);
                    //to hide the person whose account is current logged in
                    if (!user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                        users.add(user);
                    }
                }
                binding.callRecyclerView.hideShimmerAdapter();
              callAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}