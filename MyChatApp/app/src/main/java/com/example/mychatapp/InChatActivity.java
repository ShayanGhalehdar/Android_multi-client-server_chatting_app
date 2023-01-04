package com.example.mychatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;

public class InChatActivity extends MyBaseActivity {

    ExecutorService executorService = ConnectToServerActivity.executorService;

    SocketMessageHandler socketMessageSender = ConnectToServerActivity.socketMessageSender;
    SocketMessageHandler socketMessageReceiver = ConnectToServerActivity.socketMessageReceiver;

    MyRecyclerViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_chat);
        Intent i = getIntent();
        String targetUser = i.getStringExtra("targetUser");

        RecyclerView recyclerView = findViewById(R.id.recycler_gchat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, targetUser);
        recyclerView.setAdapter(adapter);

        ListenForMessages listenForMessages = new ListenForMessages(socketMessageReceiver, this);
        executorService.execute(listenForMessages);
    }

    public void sendMessage(View v) {
        TextView textView = findViewById(R.id.edit_gchat_message);
        String msg = textView.getText().toString();
        adapter.addMessage(msg, false);

        socketMessageSender.setMessage("[message] " + msg);
    }

    public class ListenForMessages implements Runnable {

        private SocketMessageHandler socketMessageReceiver;
        private InChatActivity inChatActivity;
        public ListenForMessages(SocketMessageHandler socketMessageReceiver, InChatActivity inChatActivity) {
            this.socketMessageReceiver = socketMessageReceiver;
            this.inChatActivity = inChatActivity;
        }

        @Override
        public void run() {
            while (true) {
                String received = socketMessageReceiver.listenAndSend();
                Log.i("Decode", "from sender: " + received);

                MessageDecoder decoded = new MessageDecoder(received);
                String command = decoded.getCommand();
                String msg = decoded.getMessage();

                if (command.equals("message")) {
                    ((MyChatApp)this.inChatActivity.getApplicationContext()).getCurrentActivity().runOnUiThread(new ReceiveAndDisplay(msg));
                }
            }
        }

        public class ReceiveAndDisplay implements Runnable{

            private String msg;
            public ReceiveAndDisplay(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                adapter.addMessage(msg, true);
                Log.i("Decode","display " + msg);
            }

        }

    }


}