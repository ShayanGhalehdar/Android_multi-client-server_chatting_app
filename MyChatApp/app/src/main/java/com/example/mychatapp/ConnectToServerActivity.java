package com.example.mychatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConnectToServerActivity extends MyBaseActivity {

    public static ExecutorService executorService = Executors.newFixedThreadPool(6);
    GoToAuthenticationActivity goToAuthenticationActivity = new GoToAuthenticationActivity(this);
    public static ClientSocket clientSocket;
    static SocketMessageHandler socketMessageSender = new SocketMessageHandler();
    static SocketMessageHandler socketMessageReceiver = new SocketMessageHandler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_server);
    }

    public void sendConnectionRequest(View v) throws InterruptedException {
        EditText editText_hostIP = findViewById(R.id.Auth_HostIP);
        String hostIP = editText_hostIP.getText().toString();

        clientSocket = new ClientSocket(hostIP, this, socketMessageSender, socketMessageReceiver);
        executorService.execute(clientSocket);

    }

    public class GoToAuthenticationActivity implements Runnable {

        ConnectToServerActivity connectToServer;
        public GoToAuthenticationActivity(ConnectToServerActivity connectToServer) {
            this.connectToServer = connectToServer;
        }

        @Override
        public void run() {
            Intent intent = new Intent(this.connectToServer, AuthenticationActivity.class);
            startActivity(intent);
        }
    }

}