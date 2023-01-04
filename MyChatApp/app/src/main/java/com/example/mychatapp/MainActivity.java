package com.example.mychatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class MainActivity extends MyBaseActivity {

    ExecutorService executorService = ConnectToServerActivity.executorService;
    SocketMessageHandler socketMessageSender = ConnectToServerActivity.socketMessageSender;
    SocketMessageHandler socketMessageReceiver = ConnectToServerActivity.socketMessageReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Home");

        MainActivity.ListenForMessages listenForMessages = new MainActivity.ListenForMessages(socketMessageReceiver, this);
        executorService.execute(listenForMessages);

    }

    public void chatWithUserHandler(View v) {

        EditText userText = findViewById(R.id.Main_connectTo);

        String targetUser = userText.getText().toString();
        socketMessageSender.setMessage("[connect] " + targetUser);

    }

    public class ListenForMessages implements Runnable {

        private SocketMessageHandler socketMessageReceiver;
        private MainActivity mainActivity;

        public ListenForMessages(SocketMessageHandler socketMessageReceiver, MainActivity mainActivity) {
            this.socketMessageReceiver = socketMessageReceiver;
            this.mainActivity = mainActivity;
        }

        @Override
        public void run() {
            while (true) {
                String ack = socketMessageReceiver.listenAndSend();

                MessageDecoder ackDecoded = new MessageDecoder(ack);
                String ackCommand = ackDecoded.getCommand();

                Log.i("Decode",ackCommand);
                Log.i("Decode",ack);

                ((MyChatApp) this.mainActivity.getApplicationContext()).getCurrentActivity().runOnUiThread(new ReceiveAndDo(ackDecoded, ackCommand, mainActivity));

            }
        }

        public class ReceiveAndDo implements Runnable{

            private MessageDecoder ackDecoded;
            private String ackCommand;
            private MainActivity mainActivity;
            public ReceiveAndDo(MessageDecoder ackDecoded, String ackCommand, MainActivity mainActivity) {
                this.ackDecoded = ackDecoded;
                this.ackCommand = ackCommand;
                this.mainActivity = mainActivity;
            }

            @Override
            public void run() {
                if (ackCommand.equals("ConnectedTo")) {
                    String target = ackDecoded.getMessage();
                    Toast.makeText(this.mainActivity, "Talk to " + ackDecoded.getMessage(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this.mainActivity, InChatActivity.class);
                    intent.putExtra("targetUser", target);
                    startActivity(intent);
                }
                else if (ackCommand.equals("ConnectFailSelf")) {
                    Toast.makeText(this.mainActivity, "Cannot talk to self", Toast.LENGTH_LONG).show();
                }
                else if (ackCommand.equals("ConnectFailUserNotFound")) {
                    Toast.makeText(this.mainActivity, "Username not found", Toast.LENGTH_LONG).show();
                }
                else if (ackCommand.equals("ConnectionFrom")) {
                    String targetUser = ackDecoded.getMessage();
                    Toast.makeText(this.mainActivity, "connection from " + targetUser, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this.mainActivity, InChatActivity.class);
                    intent.putExtra("targetUser", targetUser);
                    startActivity(intent);
                }

                if (ackCommand.equals("DisconnectedFrom")) {
                    String prevTarget = ackDecoded.getMessage();
                }
            }

        }
    }
}