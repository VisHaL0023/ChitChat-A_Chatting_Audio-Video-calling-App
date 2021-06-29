package com.example.whatsappclone.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Models.Message;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ItemReceivedBinding;
import com.example.whatsappclone.databinding.ItemSentBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//when more than one ViewHolder are present we don't need to extend with any Viewholder just extent using RecyclerView
public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;


    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    String senderRoom;
    String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    //ViewHolder For Recycler View
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    //for Getting whether the Message is Sent or received
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);


        //if current LoggedIn userId is Same as Sender Id
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int[] reactions = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        //Feeling Or Reaction On Message
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        //Building Pop Up
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            //When ANy Reaction Clicked Set The Image Resource on Text
            if (holder.getClass() == SentViewHolder.class) {
                SentViewHolder viewHolder = (SentViewHolder) holder;
                if (pos >= 0) {
                    viewHolder.binding.feelingSent.setImageResource(reactions[pos]);
                    viewHolder.binding.feelingSent.setVisibility(View.VISIBLE);
                }

            } else {
                ReceivedViewHolder viewHolder = (ReceivedViewHolder) holder;
                if (pos >= 0) {
                    viewHolder.binding.feelingReceived.setImageResource(reactions[pos]);
                    viewHolder.binding.feelingReceived.setVisibility(View.VISIBLE);
                }

            }
//update the value of feeling and then Upload it to the Database
            message.setFeeling(pos);

            //updating feelings to Database(sender)
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);

            // (receiver)
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);


            return true; // true is closing popup, false is requesting a new selection


        });
        //Setting the Message to View
        if (holder.getClass() == SentViewHolder.class) {
            SentViewHolder viewHolder = (SentViewHolder) holder;
            viewHolder.binding.msgSent.setText(message.getMessage());

            //for message seen or not
//**************************************************************************************************
            if (position == messages.size() - 1) {
                if (message.isSeen()) {
                    viewHolder.binding.seenTextView.setVisibility(View.VISIBLE);
                    viewHolder.binding.seenTextView.setText("Seen");
                } else {
                    viewHolder.binding.seenTextView.setVisibility(View.VISIBLE);
                    viewHolder.binding.seenTextView.setText("Sent");
                }
            } else {
                viewHolder.binding.seenTextView.setVisibility(View.GONE);

            }


//**************************************************************************************************
            //Setting the Image sent to UI
            if (message.getMessage().equals("Photo")) {
                viewHolder.binding.imageSent.setVisibility(View.VISIBLE);
                //we will make the test message gone
                viewHolder.binding.msgSent.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.img_placeholder).into(viewHolder.binding.imageSent);
            }

            //updating the UI of the chat Using Adapter
            if (message.getFeeling() >= 0) {
                viewHolder.binding.feelingSent.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feelingSent.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feelingSent.setVisibility(View.GONE);
            }
            //On Sent Msg Touched Popup
            viewHolder.binding.msgSent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
            //On ImageSent FeelingPopup
            viewHolder.binding.imageSent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        } else {
            ReceivedViewHolder viewHolder = (ReceivedViewHolder) holder;
            viewHolder.binding.msgReceived.setText(message.getMessage());


            //Receiver side showing the sent Image
            if (message.getMessage().equals("Photo")) {
                viewHolder.binding.imageReceived.setVisibility(View.VISIBLE);
                //we will make the test message gone
                viewHolder.binding.msgReceived.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.img_placeholder).into(viewHolder.binding.imageReceived);
            }

            //Updating The UI of chat using Adapter
            if (message.getFeeling() >= 0) {
                //message.setFeeling(reactions[message.getFeeling()]);
                viewHolder.binding.feelingReceived.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feelingReceived.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feelingReceived.setVisibility(View.GONE);
            }
            //On Received Msg Touch Popup
            viewHolder.binding.msgReceived.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
            //On ImageReceived Feeling Popup
            viewHolder.binding.imageReceived.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSentBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);

        }
    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder {
        ItemReceivedBinding binding;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceivedBinding.bind(itemView);
        }
    }

}
