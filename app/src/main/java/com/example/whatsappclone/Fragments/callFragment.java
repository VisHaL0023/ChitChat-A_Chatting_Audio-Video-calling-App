package com.example.whatsappclone.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.whatsappclone.Activities.OutgoingInvitationActivity;
import com.example.whatsappclone.Adapters.CallAdapter;
import com.example.whatsappclone.Models.Users;

import com.example.whatsappclone.Network.UsersListener;
import com.example.whatsappclone.databinding.FragmentCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class callFragment extends Fragment implements UsersListener {


    public callFragment() {
        // Required empty public constructor
    }


    FragmentCallBinding binding;
    FirebaseDatabase database;
    ArrayList<Users> users;
    FirebaseAuth auth;
    Users currentUser;
    CallAdapter callAdapter;


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCallBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        users = new ArrayList<>();
        callAdapter = new CallAdapter(getContext(), users, this);

        binding.callRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
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
                    } else {
                        //for storing the current user info
                        currentUser = user;
                    }
                }
                binding.callRecyclerView.hideShimmerAdapter();
                callAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }


//UserListener Methods

    @Override
    public void initiateVideoMeeting(Users user) {

        if (user.getFCM() == null && user.getFCM().trim().isEmpty()) {
            Toast.makeText(getContext(), user.getName() + " is Not Available For meeting", Toast.LENGTH_SHORT).show();

        } else {
            Intent intent = new Intent(getContext(), OutgoingInvitationActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("phoneNo", user.getPhoneNumber());
            intent.putExtra("type", "video");
            intent.putExtra("receiverToken", user.getFCM());
            intent.putExtra("currentUserinfo", currentUser);
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(Users user) {

        if (user.getFCM() == null || user.getFCM().trim().isEmpty()) {
            Toast.makeText(getContext(), user.getName() + " is Not Available For meeting", Toast.LENGTH_SHORT).show();

        } else {
            Intent intent = new Intent(getContext(), OutgoingInvitationActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("phoneNo", user.getPhoneNumber());
            intent.putExtra("type", "audio");
            intent.putExtra("receiverToken", user.getFCM());
            intent.putExtra("currentUserinfo", currentUser);
            startActivity(intent);

        }

    }
}