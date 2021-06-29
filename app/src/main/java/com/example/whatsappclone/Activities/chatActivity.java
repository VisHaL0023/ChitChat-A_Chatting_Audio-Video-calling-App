package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Adapters.MessagesAdapter;
import com.example.whatsappclone.Models.Message;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class chatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;
    String senderUid, receiverUid;
    FirebaseDatabase database;
    //for photos send to chat
    FirebaseStorage storage;
    ProgressDialog dialog;
    ValueEventListener seenListner;
    DatabaseReference reference;
    FirebaseUser fuser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();


        //for photos send to chat
        storage = FirebaseStorage.getInstance();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        //for setting the ExtraTool bar in Chat
        setSupportActionBar(binding.toolbar);
        //for hiding the Name of app on TOolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending Image");
        dialog.setCancelable(false);

        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        //for Updating the Online UI
        database.getReference().child("presence").child(receiverUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String OnOffstatus = snapshot.getValue(String.class);
                            if (!OnOffstatus.isEmpty()) {
                                if (OnOffstatus.equals("Offline")) {
                                    binding.OnOffstatus.setVisibility(View.GONE);
                                } else {
                                    binding.OnOffstatus.setText(OnOffstatus);
                                    binding.OnOffstatus.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        //to move automatically the recycler view to end of message list
        linearLayoutManager.setStackFromEnd(true);

        binding.recyclerViewChat.setLayoutManager(linearLayoutManager);

        binding.recyclerViewChat.setAdapter(adapter);


        String name = getIntent().getStringExtra("name");
        // For getting the Profile Image for toolBar
        String profilePic = getIntent().getStringExtra("image");
        //setting the Name, Image to toolbar
        binding.name.setText(name);
        Glide.with(chatActivity.this).load(profilePic).placeholder(R.drawable.avatar).into(binding.profilePic);

        //for retrieving the messages from FirebaseDatabase

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        binding.recyclerViewChat.scrollToPosition(binding.recyclerViewChat.getAdapter().getItemCount() - 1);
                        adapter.notifyDataSetChanged();
                    }


                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();
                Date date = new Date();
                Message message = new Message(messageTxt, senderUid, date.getTime(), receiverUid, false);

                //to Empty the box After Message Sent
                binding.messageBox.setText("");

                //message id of send or received msg (one which is shown on sender side and other same msg on receiver side )must have same id in the database
                //so that we can easily set feeling on one and it get reflected on other side (lets create a common random key )
                String randomKey = database.getReference().push().getKey();

                //For Storing The Last Msg and Time
                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());

                //uploading last message to database
                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                //Adding the Messages to Firebase one for receiver and other for sender
                database.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference()
                                .child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
                    }
                });

            }
        });


        //BAck Navigation Arrow
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //  For sending The Images to Chat
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });


        //for displaying Name on the action Bar
        //getSupportActionBar().setTitle(name);
        //for back navigation arrow
        //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //for typing indication
        final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");

                }
            };
        });

        seenMessage(receiverUid);


    }

    private void seenMessage(String userID) {
        //Toast.makeText(chatActivity.this, "SeenMessageCalled", Toast.LENGTH_SHORT).show();
        reference = FirebaseDatabase.getInstance().getReference().child("chats")
                .child(senderRoom)
                .child("messages");
        seenListner = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                    Message message = snapshot2.getValue(Message.class);
                    if (message.getReceiverId().equals(fuser.getUid()) && message.getSenderId().equals(userID)) {
                        // Toast.makeText(chatActivity.this, "True", Toast.LENGTH_SHORT).show();
                        message.setSeen(true);
                        database.getReference()
                                .child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(snapshot2.getKey())
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference()
                                        .child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(snapshot2.getKey())
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    //FOr showing Online and Stroing in Database
    @Override
    protected void onResume() {
        super.onResume();
        //Updating Data on Online Offline in Database
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
        reference.removeEventListener(seenListner);
    }

    //for image sent in Chats
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 25) {
            if (data != null) {
                if (data.getData() != null) {

                    Uri selectedImage = data.getData();
                    //Calender Is Used here for getting unique Id as time in milisecond
                    //will be unique
                    Calendar calendar = Calendar.getInstance();
                    // Creating Storage Reference
                    StorageReference reference = storage.getReference()
                            .child("chats")
                            .child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    // uplading the file
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //it is the filePath of sent Image on chat
                                        String filePath = uri.toString();

                                        String messageTxt = binding.messageBox.getText().toString();
                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid, date.getTime(), receiverUid, false);

                                        //setting the Image path to Message Molder class or obj so that it can get updated in UI
                                        message.setImageUrl(filePath);
                                        message.setMessage("Photo");
                                        //to Empty the box After Message Sent
                                        binding.messageBox.setText("");

                                        //message id of send or received msg (one which is shown on sender side and other same msg on receiver side )must have same id in the database
                                        //so that we can easily set feeling on one and it get reflected on other side (lets create a common random key )
                                        String randomKey = database.getReference().push().getKey();

                                        //For Storing The Last Msg and Time
                                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg", message.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());

                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                        database.getReference()
                                                .child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                database.getReference()
                                                        .child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                    }
                                                });
                                            }
                                        });


                                    }
                                });
                            }
                        }
                    });
                }
            }
        }

    }


}