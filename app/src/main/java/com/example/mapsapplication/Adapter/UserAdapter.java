package com.example.mapsapplication.Adapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapsapplication.Model.Chat;
import com.example.mapsapplication.Model.User;
import com.example.mapsapplication.Model.UserFirebase;
import com.example.mapsapplication.Others.SharedPrefManager;
import com.example.mapsapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<UserFirebase> mUsers;
    private boolean ischat;

    String theLastMessage;

    public UserAdapter(Context mContext, List<UserFirebase> mUsers, boolean ischat){
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final UserFirebase user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.drawable.ic_baseline_account_circle_24);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (ischat){
            lastMessage(user.getId(), holder.last_msg, holder.unread_count, holder);
        } else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (ischat){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    public void filterList(ArrayList<UserFirebase> filterdNames) {
        this.mUsers = filterdNames;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;
        private TextView unread_count;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            unread_count = itemView.findViewById(R.id.unread_count);
        }
    }

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg, final TextView unread_count, final ViewHolder holder){
        theLastMessage = "default";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        final User firebaseUser = SharedPrefManager.getInstance(mContext).getUser();

        Query query = FirebaseDatabase.getInstance().getReference("Chats").child(firebaseUser.getId()+"").orderByChild("time");

        Log.d("Sender", firebaseUser.getId()+"");
        Log.d("Reciever", userid+"");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getId()+"") && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getId()+"")) {

                            Log.d("time", chat.getTime() +": "+ chat.getMessage());
                            if(chat.getSender().equals(firebaseUser.getId()+"")) {
                                theLastMessage = "You: "+chat.getMessage();
                            } else {
                                if(!chat.isIsseen()){
                                    last_msg.setTypeface(null, Typeface.BOLD);
                                    last_msg.setTextColor(Color.BLACK);
                                    unread_count.setVisibility(View.VISIBLE);
                                }
                                theLastMessage = chat.getMessage();
                            }
                        }
                    }
                }

                switch (theLastMessage){
                    case  "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Chat chat = snapshot.getValue(Chat.class);
//                    if (firebaseUser != null && chat != null) {
//                        if (chat.getReceiver().equals(firebaseUser.getId()+"") && chat.getSender().equals(userid) ||
//                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getId()+"")) {
//                            if(chat.getSender().equals(firebaseUser.getId()+"")) {
//                                theLastMessage = "You: "+chat.getMessage();
//                            } else {
//                                if(!chat.isIsseen()){
//                                    last_msg.setTypeface(null, Typeface.BOLD);
//                                    last_msg.setTextColor(Color.BLACK);
//                                    unread_count.setVisibility(View.VISIBLE);
//                                }
//                                theLastMessage = chat.getMessage();
//                            }
//                        }
//                    }
//                }
//
//                switch (theLastMessage){
//                    case  "default":
//                        last_msg.setText("No Message");
//                        break;
//
//                    default:
//                        last_msg.setText(theLastMessage);
//                        break;
//                }
//
//                theLastMessage = "default";
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    public void swapItems(int a, int b) {
        UserFirebase itemA = mUsers.get(a);
        UserFirebase itemB = mUsers.get(b);
        mUsers.set(a, itemB);
        mUsers.set(b, itemA);

        notifyDataSetChanged(); //This will trigger onBindViewHolder method from the adapter.
    }
}