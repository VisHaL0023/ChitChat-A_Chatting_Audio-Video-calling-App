package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
//import com.example.whatsappclone.Activities.CallActivity;
import com.example.whatsappclone.Fragments.callFragment;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.Network.UsersListener;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.CallLayoutBinding;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.CallViewHolder>{

    Context context;
    ArrayList<Users> users;
    UsersListener usersListener;





    public CallAdapter(Context context, ArrayList<Users> users, UsersListener usersListener) {
        this.context = context;
        this.users = users;
        this.usersListener = usersListener;
    }

    @NonNull
    @NotNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.call_layout,parent,false);
        return new CallViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CallAdapter.CallViewHolder holder, int position) {
             Users user = users.get(position);

             holder.binding.calleruserName.setText(user.getName());
        Glide.with(context).load(user.getProfileImage()).placeholder(R.drawable.avatar).into(holder.binding.callerprofileImg);

    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class CallViewHolder extends RecyclerView.ViewHolder
    {
        CallLayoutBinding binding;
        public CallViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = CallLayoutBinding.bind(itemView);

            //setting call on button
            binding.callImageButtom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     //for getting the user current in adapter
                    Users user = users.get(getAdapterPosition());
                    //for calling the method of CallActivity from this Activity

                       usersListener.initiateAudioMeeting(user);
                }
            });

            binding.videoCallImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Users user = users.get(getAdapterPosition());
                    usersListener.initiateVideoMeeting(user);
                }
            });

        }
    }
}
