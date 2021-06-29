package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.databinding.RowConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.whatsappclone.Activities.*;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    Context context;
    ArrayList<Users> users;


    public UsersAdapter(Context context, ArrayList<Users> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UsersViewHolder holder, int position) {
        Users user = users.get(position);
//***********************************************************
        //for Last msg and time
        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();
        //retrieving the data from data base(lst msg) and set it to UI
        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            //for Formatting the Time
                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh : mm a");

                            holder.binding.chatTime.setText(dateFormat.format(new Date(time)));
                            holder.binding.lastMessage.setText(lastMsg);
                        } else {
                            holder.binding.lastMessage.setText("Tap to chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
//********************************************************************
        holder.binding.userName.setText(user.getName());

        //As The Profile Pic is In string Form bit the SetImageResource Take Args as int So we cannot
        //set The Image thus we need Library such as Glide or Picasso
        //holder.binding.profileImg.setImageResource(user.getProfileImage()); wrong xxx

        //Load UserImage to MainActivity List
        Glide.with(context).load(user.getProfileImage()).placeholder(R.drawable.avatar).into(holder.binding.profileImg);

        //when Clicked on Any Chat open Chat Activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, chatActivity.class);
                intent.putExtra("name", user.getName());
                intent.putExtra("image",user.getProfileImage());
                intent.putExtra("uid", user.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
