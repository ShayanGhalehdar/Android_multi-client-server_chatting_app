package com.example.mychatapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ClientSocket implements Runnable{

    final static int serverPort = 1234;
    private final String hostIp;
    private final ConnectToServerActivity connectToServer;
    static ClientSocket noGarbageClass;
    SocketMessageHandler socketMessageSender;
    SocketMessageHandler socketMessageReceiver;

    public ClientSocket(String hostIp, ConnectToServerActivity connectToServer, SocketMessageHandler socketMessageSender, SocketMessageHandler socketMessageReceiver) {
        this.hostIp = hostIp;
        this.connectToServer = connectToServer;
        this.socketMessageSender = socketMessageSender;
        this.socketMessageReceiver = socketMessageReceiver;
    }

    @Override
    public void run() {
        try {
            //InetAddress ip = InetAddress.getByName(hostIp);
            //Log.i("hostIP", ip.toString());
            Log.i("hostIPStr", hostIp);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(hostIp, serverPort), 2000);

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // handling sending messages to server
            SendMessage sm = new SendMessage(socket, socketMessageSender, dos);
            Thread sendMessage = new Thread(sm);

            // handling receiving messages from server
            ReadMessage rm = new ReadMessage(socket, socketMessageReceiver, dis);
            Thread readMessage = new Thread(rm);

            sendMessage.start();
            readMessage.start();

            ((MyChatApp)this.connectToServer.getApplicationContext()).getCurrentActivity().runOnUiThread(new ConnectionToast(true, this.connectToServer));
            this.connectToServer.runOnUiThread(this.connectToServer.goToAuthenticationActivity);

            Log.i("validation", "connected");
        } catch (IOException e) {
            Log.i("validation", "connection failed");

            ((MyChatApp)this.connectToServer.getApplicationContext()).getCurrentActivity().runOnUiThread(new ConnectionToast(false, this.connectToServer));
            return;
        }
    }

    @Override
    protected void finalize() {
        noGarbageClass = this;
    }

    class SendMessage implements Runnable{

        final DataOutputStream dos;
        SocketMessageHandler scn;
        Socket s;

        SendMessage(Socket s, SocketMessageHandler scn, DataOutputStream dos) {
            this.scn = scn;
            this.dos = dos;
            this.s = s;
        }

        @Override
        public void run() {

            while (true) {

                String message = scn.listenAndSend();

                try {
                    dos.writeUTF(message);
                }
                // handling server disconnection
                catch (IOException e) {
                    try {
                        Log.i("ClientConnection", "disconnected");
                        dos.close();
                        s.close();
                        break;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    class ReadMessage implements Runnable{

        final DataInputStream dis;
        SocketMessageHandler res;
        Socket s;

        ReadMessage(Socket s, SocketMessageHandler res, DataInputStream dis) {
            this.res = res;
            this.dis = dis;
            this.s = s;
        }

        @Override
        public void run() {

            while (true) {
                try {
                    String message = dis.readUTF();
                    res.setMessage(message);
                }
                // handling server disconnection
                catch (IOException e) {
                    try {
                        System.out.println("Disconnected!");
                        dis.close();
                        s.close();
                        break;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private class ConnectionToast implements Runnable {

        boolean success;
        ConnectToServerActivity connectToServer;

        public ConnectionToast(boolean success, ConnectToServerActivity connectToServer) {
            this.success = success;
            this.connectToServer = connectToServer;
        }

        @Override
        public void run() {
            String message = (this.success)? "Successfully connected!":"Host not found!";
            Toast.makeText(this.connectToServer.getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

}


