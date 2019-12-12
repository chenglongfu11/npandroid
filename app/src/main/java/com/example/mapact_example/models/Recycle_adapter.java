package com.example.mapact_example.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mapact_example.R;

import java.util.List;


public class Recycle_adapter extends RecyclerView.Adapter<Recycle_adapter.MyViewHolder> {
    private List<Message> mDataset;
    private MessageListClickListener mClickListener;


    public Recycle_adapter(List<Message> myDataset, MessageListClickListener messageListClickListener) {
        mDataset = myDataset;
        mClickListener = messageListClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public Recycle_adapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_message_list, parent, false);

        MyViewHolder vh = new MyViewHolder(v,mClickListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.username.setText(mDataset.get(position).getUsername());
        holder.datatime.setText(mDataset.get(position).getDate().toString());
        holder.messages.setText(mDataset.get(position).getMsg());
        holder.locations.setText(mDataset.get(position).getLocation().toString());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public interface MessageListClickListener{
        void onClicked(int position);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        TextView username,datatime,messages,locations;
        MessageListClickListener messageListClickListener;
        public MyViewHolder(View v, MessageListClickListener messageListClickListener) {
            super(v);
            username = v.findViewById(R.id.username);
            datatime = v.findViewById(R.id.txt_datetime);
            messages = v.findViewById(R.id.messages);
            locations = v.findViewById(R.id.txt_locations);
            this.messageListClickListener = messageListClickListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            messageListClickListener.onClicked(getAdapterPosition());

        }
    }

}