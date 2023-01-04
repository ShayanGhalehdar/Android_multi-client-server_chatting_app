package com.example.mychatapp;


public class SocketMessageHandler {

    private String message = null;
    public SocketMessageHandler() {

    }

    public String listenAndSend() {

        while (message == null) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {

            }
        }

        String message_copy = this.message;
        this.message = null;
        return message_copy;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
