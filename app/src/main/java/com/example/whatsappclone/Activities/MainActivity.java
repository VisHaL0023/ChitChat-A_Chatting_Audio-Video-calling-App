package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.whatsappclone.Adapters.topStatusAdapter;
import com.example.whatsappclone.Fragments.callFragment;
import com.example.whatsappclone.Fragments.chatFragment;
import com.example.whatsappclone.Models.Status;
import com.example.whatsappclone.Models.UserStatus;
import com.example.whatsappclone.R;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //Variable Declaration
    ActivityMainBinding binding;
    FirebaseDatabase database;
    String currentId;
    topStatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    ProgressDialog dialog;
    Users user;

    //for saving state of frag
    Fragment activeFrag;
    final Fragment chatFragment = new chatFragment();
    final Fragment callFragment = new callFragment();
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


      //for preventing again again reloading of frags
        fragmentManager.beginTransaction().add(binding.activitymainLinearLayout.getId(), chatFragment).commit();
        activeFrag = chatFragment;
        fragmentManager.beginTransaction().add(binding.activitymainLinearLayout.getId(), callFragment).hide(callFragment).commit();


        currentId = FirebaseAuth.getInstance().getUid();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Status...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();

        userStatuses = new ArrayList<>();
        statusAdapter = new topStatusAdapter(this, userStatuses);

        //Getting Users Data from Firebase (for status Purpose information of the person uploading status)(OWN id info) we will load it and use it later
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

//        we can Do this in Both XML and here in Java choice is Ours
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusView.setAdapter(statusAdapter);
        binding.statusView.setLayoutManager(linearLayoutManager);

//        //for the Loading ScreenAt the staring of App
        binding.statusView.showShimmerAdapter();

//
        //displaying the uploded Status to view
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //so that it doesnt make extra copy
                    userStatuses.clear();
                    for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                        //*******************
                        //for taking all the Status of That particular User
                        ArrayList<Status> statuses = new ArrayList<>();
                        for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }
                        status.setStatuses(statuses);
//*****************************
                        userStatuses.add(status);

                    }
                    binding.statusView.hideShimmerAdapter();
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //selecting option from Bottom NAV

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                if (binding.bottomNavigationView.getSelectedItemId() != item.getItemId()) {

                    switch (item.getItemId()) {
                        case R.id.chats:
                            fragmentManager.beginTransaction().hide(activeFrag).show(chatFragment).commit();
                            activeFrag = chatFragment;
                            break;
                        case R.id.status:
                            fragmentManager.beginTransaction().show(activeFrag).commit();
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intent, 75);
                            return false;


                        case R.id.calls:
                            fragmentManager.beginTransaction().hide(activeFrag).show(callFragment).commit();
                            activeFrag = callFragment;
                            break;

                    }
                }
                return true;
            }
        });

        //For Firebase cloud Messaging Token Generation
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Token generation Failed", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("FCM").setValue(token);
                    }
                });
    }


    //TO upload Image on Status
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data.getData() != null) {
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("status")
                        .child(date.getTime() + "");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //uploading Status to firebase
                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getProfileImage());
                                    userStatus.setLastUpdated(date.getTime());

                                    //hashmap for status Purpose
                                    HashMap<String, Object> obj = new HashMap<>();
                                    obj.put("name", userStatus.getName());
                                    obj.put("profileImage", userStatus.getProfileImage());
                                    obj.put("lastUpdated", userStatus.getLastUpdated());

                                    String imageUrl = uri.toString();
                                    Status status = new Status(imageUrl, userStatus.getLastUpdated());

                                    //for uploading the last Status
                                    database.getReference().child("stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .updateChildren(obj);
                                    //for uploading status to database
                                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid())
                                            .child("statuses").push().setValue(status);
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    //for showing the Online Status of the User
    @Override
    protected void onResume() {
        super.onResume();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    //For the Options In the 3 dot
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return true;
    }

    //for Checking Which option is Selected in 3 dot
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:

                break;
            case R.id.settings:
                Toast.makeText(getApplicationContext(), "Setting", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite:

                break;
            case R.id.logout:
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                            .child("FCM").setValue(null);
                    auth.signOut();
                    Intent intent = new Intent(MainActivity.this, PhoneNumberActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}