package com.example.hotroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ChatFragment extends Fragment {

    public ChatFragment() {
        // Constructor público vacío obligatorio
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout que usabas en ChatUser
        return inflater.inflate(R.layout.user_chat, container, false);
    }
}
