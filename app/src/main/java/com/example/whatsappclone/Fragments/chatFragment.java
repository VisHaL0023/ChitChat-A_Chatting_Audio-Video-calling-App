package com.example.whatsappclone.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.example.whatsappclone.Activities.MainActivity;
import com.example.whatsappclone.Activities.PhoneNumberActivity;
import com.example.whatsappclone.Adapters.UsersAdapter;
import com.example.whatsappclone.Adapters.topStatusAdapter;
import com.example.whatsappclone.Models.Status;
import com.example.whatsappclone.Models.UserStatus;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivityMainBinding;
import com.example.whatsappclone.databinding.FragmentChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class chatFragment extends Fragment {


    public chatFragment() {
        // Required empty public constructor
    }

    //Variable Declaration
    FirebaseDatabase database;
    ArrayList<Users> users;
    UsersAdapter usersAdapter;
    ProgressDialog dialog;
    Users user;
    FragmentChatBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Uploading Status...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();

        usersAdapter = new UsersAdapter(getContext(), users);


        //Getting Users Data from Firebase (for status Purpose information of the person uplading status)(khudki id info) we will load it and use it later
        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        user = snapshot.getValue(Users.class);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


        binding.recyclerView.setAdapter(usersAdapter);

        binding.recyclerView.setHasFixedSize(true);
        //for the Loading ScreenAt the staring of App
        binding.recyclerView.showShimmerAdapter();


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
                binding.recyclerView.hideShimmerAdapter();

                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }


}
