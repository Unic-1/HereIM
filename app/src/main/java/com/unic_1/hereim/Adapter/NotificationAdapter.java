package com.unic_1.hereim.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.Model.Request;
import com.unic_1.hereim.R;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by unic-1 on 25/8/17.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    ArrayList<Request> notificationList;

    public NotificationAdapter(ArrayList<Request> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_view, parent, false);

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        holder.setItem(position);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    interface OnItemClickListener {
        public void onClick();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvSender, tvTime;
        Button bSend, bReject, bView;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            tvSender = (TextView) itemView.findViewById(R.id.tvSender);
            tvTime = (TextView) itemView.findViewById(R.id.timetv);
            bSend = (Button) itemView.findViewById(R.id.bSend);
            bReject = (Button) itemView.findViewById(R.id.bReject);
            bView = (Button) itemView.findViewById(R.id.bView);
        }

        public void setMessage(String message) {
            tvMessage.setText(message);
        }

        public void setSender(String sender) {
            tvSender.setText(sender);
        }

        public void setTime(String time) {
            tvTime.setText(time);
        }

        public void setItem(int position) {
            String message = null;
            String senderORreceiver = null;
            String time = null;

            Request req = notificationList.get(position);

            Constant.Actions action = req.getAction();

            if (action == Constant.Actions.REQUEST_SENT) {
                message = "Your reqeust has been sent";
                senderORreceiver = req.getTo();
            } else if (action == Constant.Actions.REQUEST_RECEIVED) {
                message = "You have received a request";
                senderORreceiver = req.getFrom();
            } else if (action == Constant.Actions.LOCATION_SENT) {
                message = "Your location has been sent";
                senderORreceiver = req.getTo();
            } else if (action == Constant.Actions.LOCATION_RECEIVED) {
                message = "You have received the location";
                senderORreceiver = req.getFrom();
            } else if (action == Constant.Actions.REQEUST_DECLINED) {
                message = "Request is declined";
                senderORreceiver = req.getTo();
            }


            Long span = (new Date().getTime() - req.getTimestamp()) / (1000 * 60);

            if (span <= 59) {
                time = span + " m";
            } else if (span > 59 && span < 1439) {
                time = span / 60 + " h";
            } else if (span > 1439) {
                time = span / (60 * 24) + " d";
            }


            setMessage(message);
            setSender(senderORreceiver);
            setTime(time);
        }


    }
}
