package com.unic_1.hereim.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.unic_1.hereim.Model.NotificationModel;
import com.unic_1.hereim.R;

import java.util.ArrayList;

/**
 * Created by unic-1 on 25/8/17.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    ArrayList<NotificationModel> notificationList;

    public NotificationAdapter(ArrayList<NotificationModel> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_view, parent, false);

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        holder.setMessage(notificationList.get(position).getMessage());
        holder.setSender(notificationList.get(position).getSender());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvSender;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            tvSender = (TextView) itemView.findViewById(R.id.tvSender);
        }

        public void setMessage(String message) {
            tvMessage.setText(message);
        }

        public void setSender(String sender) {
            tvSender.setText(sender);
        }
    }
}
