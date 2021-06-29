package com.example.whatsappclone.Network;

import com.example.whatsappclone.Models.Users;

public interface
UsersListener {
    void initiateVideoMeeting(Users user);

    void initiateAudioMeeting(Users user);
}
