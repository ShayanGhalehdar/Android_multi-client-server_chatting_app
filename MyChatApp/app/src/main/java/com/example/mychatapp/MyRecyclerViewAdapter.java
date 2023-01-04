package com.example.mychatapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.BaseMessageHolder> {

    private List<String> mData = new LinkedList<>();
    private List<Boolean> meOther = new LinkedList<>();   // 0 means me is sender, 1 means other is sender
    private LayoutInflater mInflater;
    private String otherUsername;

    public void addMessage(String msg, Boolean meOther) {
        this.mData.add(msg);
        this.meOther.add(meOther);
        notifyItemInserted(getItemCount());
    }

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, String otherUsername) {
        this.mInflater = LayoutInflater.from(context);
        this.otherUsername = otherUsername;
    }

    @Override
    public int getItemViewType(int position) {

        if (!meOther.get(position))
            return 0;
        else
            return 1;
    }

    // inflates the row layout from xml when needed
    @Override
    public BaseMessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = mInflater.inflate(R.layout.item_chat_me, parent, false);
            return new SentMessageHolder(view);
        }
        else {
            View view = mInflater.inflate(R.layout.item_chat_other, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(BaseMessageHolder holder, int position) {

        String msg = this.mData.get(position);
        boolean meOther = this.meOther.get(position);
        if (!meOther) {
            ((SentMessageHolder) holder).textView.setText(msg);
        }
        else {
            ((ReceivedMessageHolder) holder).textView.setText(msg);
            ((ReceivedMessageHolder) holder).userView.setText(this.otherUsername);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class BaseMessageHolder extends RecyclerView.ViewHolder {

        BaseMessageHolder(View itemView) {
            super(itemView);
        }

    }

    // stores and recycles views as they are scrolled off screen
    public class SentMessageHolder extends BaseMessageHolder {
        TextView textView;

        SentMessageHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
        }

    }

    public class ReceivedMessageHolder extends BaseMessageHolder {
        TextView textView;
        TextView userView;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            userView = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
        }
    }

}
