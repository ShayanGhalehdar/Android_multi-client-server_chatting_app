package com.example.mychatapp;

public class MessageDecoder {
    private final String msg;
    private final String command;
    private final String payload;

    MessageDecoder(String msg) {
        this.msg = msg;
        this.command = msg.substring(1,msg.indexOf("]"));
        if (this.msg.length() > this.command.length() + 2)
            this.payload = msg.substring(msg.indexOf("]") + 2);
        else
            this.payload = "";
    }

    public String getCommand() {
        return this.command;
    }

    public String[] getUserPass() {
        String[] sep = this.payload.split(" ");
        return sep;
    }

    public String getMessage() {
        return this.payload;
    }
}
