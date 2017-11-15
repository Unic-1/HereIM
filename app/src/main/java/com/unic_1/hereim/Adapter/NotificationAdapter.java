package com.unic_1.hereim.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.LandingActivity;
import com.unic_1.hereim.MapsActivity;
import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;
import com.unic_1.hereim.R;

import java.util.ArrayList;
import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by unic-1 on 25/8/17.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    ArrayList<Request> notificationList;
    Context context;

    public NotificationAdapter(ArrayList<Request> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
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
        int pos = 0;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            tvSender = (TextView) itemView.findViewById(R.id.tvSender);
            tvTime = (TextView) itemView.findViewById(R.id.timetv);
            bSend = (Button) itemView.findViewById(R.id.bSend);
            bReject = (Button) itemView.findViewById(R.id.bReject);
            bView = (Button) itemView.findViewById(R.id.bView);
            
            bView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: View clicked with position "+pos);
                    Request req1 = notificationList.get(pos);
                    Intent i = new Intent(context, MapsActivity.class);

                    Bundle basket = new Bundle();
                    basket.putDouble("latitude", req1.getLocation().latitude);
                    basket.putDouble("longitude", req1.getLocation().longitude);

                    i.putExtras(basket);

                    context.startActivity(i);

                }
            });

            bSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Request req = notificationList.get(pos);
                    /*FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("Users").child(req.getTo()).child("request_list").child("").child("action");
                    DatabaseReference ref1 = database.getReference("Users").child(req.getFrom()).child(req.getTo()).child("action");

                    ref.setValue(Constant.Actions.LOCATION_SENT);
                    ref1.setValue(Constant.Actions.LOCATION_RECEIVED);

                    DatabaseReference reqref = database.getReference("Request").child(req.getRequestId());*/

                    new RequestAdapter().updateRequest(
                            req.getFrom(),
                            req.getTo(),
                            req.getRequestId(),
                            Constant.Actions.LOCATION_SENT.value,
                            new LocationCoordinates(
                                    LandingActivity.location.getLatitude(),
                                    LandingActivity.location.getLongitude()
                            )
                    );
                }
            });

            bReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Request req = notificationList.get(pos);
                    /*FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("Users").child(req.getTo()).child(req.getFrom()).child("action");
                    DatabaseReference ref1 = database.getReference("Users").child(req.getFrom()).child(req.getTo()).child("action");

                    ref.setValue(Constant.Actions.REQEUST_DECLINED);
                    ref1.setValue(Constant.Actions.REQEUST_DECLINED);*/

                    new RequestAdapter().updateRequest(
                            req.getFrom(),
                            req.getTo(),
                            req.getRequestId(),
                            Constant.Actions.REQEUST_DECLINED.value,
                            null
                    );
                }
            });
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

            pos = position;

            Request req = notificationList.get(position);

            Constant.Actions action = req.getAction();

            if (action == Constant.Actions.REQUEST_SENT) {
                message = "Your reqeust has been sent";
                senderORreceiver = req.getTo();
                bSend.setVisibility(View.GONE);
                bReject.setVisibility(View.GONE);
                bView.setVisibility(View.GONE);
            } else if (action == Constant.Actions.REQUEST_RECEIVED) {
                message = "You have received a request";
                senderORreceiver = req.getFrom();
                bView.setVisibility(View.GONE);
            } else if (action == Constant.Actions.LOCATION_SENT) {
                message = "Your location has been sent";
                senderORreceiver = req.getTo();
                bSend.setVisibility(View.GONE);
                bReject.setVisibility(View.GONE);
                bView.setVisibility(View.GONE);
            } else if (action == Constant.Actions.LOCATION_RECEIVED) {
                message = "You have received the location";
                senderORreceiver = req.getFrom();
                bSend.setVisibility(View.GONE);
                bReject.setVisibility(View.GONE);
                bView.setVisibility(View.VISIBLE);
            } else if (action == Constant.Actions.REQEUST_DECLINED) {
                message = "Request is declined";
                senderORreceiver = req.getTo();
                bSend.setVisibility(View.GONE);
                bReject.setVisibility(View.GONE);
                bView.setVisibility(View.GONE);
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


        //@Override
        public void onClick(View v) {
            Log.i(TAG, "onClick: button clicked");
            switch (v.getId()) {
                case R.id.bSend:
                    Request req = notificationList.get(pos);
                    /*new RequestAdapter().updateRequest(
                            req.getFrom(),
                            req.getTo(),
                            req.get, //// TODO: 30/10/17 add child id as the data member in Request class for updating the data
                            Constant.Actions.LOCATION_SENT,
                            LandingActivity.location
                            );*/
                    break;
                case R.id.bReject:
                    break;
                case R.id.bView:
                    Log.i(TAG, "onClick: View clicked with position "+pos);
                    Request req1 = notificationList.get(pos);
                    Intent i = new Intent(context, MapsActivity.class);

                    Bundle basket = new Bundle();
                    basket.putDouble("latitude", req1.getLocation().latitude);
                    basket.putDouble("longitude", req1.getLocation().longitude);

                    i.putExtras(basket);

                    context.startActivity(i);
                    break;
            }
        }
    }
}
