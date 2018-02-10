package com.unic_1.hereim.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.unic_1.hereim.Constants.Constant;
import com.unic_1.hereim.LandingActivity;
import com.unic_1.hereim.Model.LocationCoordinates;
import com.unic_1.hereim.Model.Request;
import com.unic_1.hereim.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by unic-1 on 25/8/17.
 */


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private static int countPosition = 0;
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
        Log.i(TAG, "getItemCount: " + notificationList.size());
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

        private NotificationViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvTime = itemView.findViewById(R.id.timetv);
            bSend = itemView.findViewById(R.id.bSend);
            bReject = itemView.findViewById(R.id.bReject);
            bView = itemView.findViewById(R.id.bView);

            bView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: View clicked with position " + pos);
                    Request req1 = notificationList.get(pos);
                    /*
                    * This code triggers the map within the app
                    * For now we will work with the default map

                    Intent i = new Intent(context, MapsActivity.class);

                    Bundle basket = new Bundle();
                    basket.putDouble("latitude", req1.getLocation().latitude);
                    basket.putDouble("longitude", req1.getLocation().longitude);

                    i.putExtras(basket);

                    context.startActivity(i);


                    */

                    // This triggers the default system map
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f", req1.getLocation().latitude, req1.getLocation().longitude);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    context.startActivity(intent);

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
                            req.getUserRequestReference().getRequest_reference(),
                            req.getUserRequestReference().getRequestID(),
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
                            req.getUserRequestReference().getRequest_reference(),
                            req.getUserRequestReference().getRequestID(),
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
            //countPosition++;

            Request req = notificationList.get(pos);

            int action = req.getUserRequestReference().getAction();

            if (action == Constant.Actions.REQUEST_SENT.value) {
                message = "Your reqeust has been sent";
                senderORreceiver = req.getTo();
                bSend.setVisibility(View.GONE);
                bReject.setVisibility(View.GONE);
                bView.setVisibility(View.GONE);
            } else if (action == Constant.Actions.REQUEST_RECEIVED.value) {
                message = "You have received a request";
                senderORreceiver = req.getFrom();
                bView.setVisibility(View.GONE);
            } else if (action == Constant.Actions.LOCATION_SENT.value) {
                message = "Your location has been sent";
                senderORreceiver = req.getFrom();
                bSend.setVisibility(View.GONE);
                bReject.setVisibility(View.GONE);
                bView.setVisibility(View.GONE);
            } else if (action == Constant.Actions.LOCATION_RECEIVED.value) {
                message = "You have received the location";
                senderORreceiver = req.getTo();
                bSend.setVisibility(View.GONE);
                bReject.setVisibility(View.GONE);
                bView.setVisibility(View.VISIBLE);
            } else if (action == Constant.Actions.REQEUST_DECLINED.value) {
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
    }
}
