package com.example.mychatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class AuthenticationActivity extends MyBaseActivity {

    SocketMessageHandler socketMessageSender = ConnectToServerActivity.socketMessageSender;
    SocketMessageHandler socketMessageReceiver = ConnectToServerActivity.socketMessageReceiver;

    public String username_ = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.username_ != null) {
            socketMessageSender.setMessage("[logout] " + this.username_);
            this.username_ = null;
        }
        setContentView(R.layout.activity_login);
    }

    public void signInHandler(View v) {
        EditText userText = findViewById(R.id.Login_UserName);
        EditText passText = findViewById(R.id.Login_Password);

        String username = userText.getText().toString();
        String password = passText.getText().toString();

        socketMessageSender.setMessage("[login] " + username + " " + password);
        String ack = socketMessageReceiver.listenAndSend();
        MessageDecoder ackDecoded = new MessageDecoder(ack);
        String ackCommand = ackDecoded.getCommand();
        Log.i("Decode",ackCommand);
        Log.i("Decode",ack);

        if (ackCommand.equals("trueIn") || ackCommand.equals("trueUp")) {
            Toast.makeText(this, "Sign " + ackCommand.substring(4).toLowerCase(Locale.ROOT) + " successful!", Toast.LENGTH_LONG).show();
            this.username_ = username;

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else if (ackCommand.equals("weakPass")) {
            Toast.makeText(this, "weak password!", Toast.LENGTH_LONG).show();
        }
        else if(ackCommand.equals("userTaken")) {
            Toast.makeText(this, "enter another username!", Toast.LENGTH_LONG).show();
        }
        else if (ackCommand.equals("wrongPass")) {
            Toast.makeText(this, "wrong password!", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }

    }


}