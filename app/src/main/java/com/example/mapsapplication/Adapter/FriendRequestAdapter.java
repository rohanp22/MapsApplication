package com.example.mapsapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.mapsapplication.Model.FriendRequest;
import com.example.mapsapplication.Model.UserFirebase;
import com.example.mapsapplication.Others.URLs;
import com.example.mapsapplication.Others.VolleySingleton;
import com.example.mapsapplication.ProfileFragment;
import com.example.mapsapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private Context mContext;
    private List<FriendRequest> mUsers;
    FragmentManager fragmentManager;

    public FriendRequestAdapter(Context mContext, List<FriendRequest> mUsers, FragmentManager fragmentManager) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friend_request_layout, parent, false);
        return new FriendRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d("sender", mUsers.get(position).getSender());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mUsers.get(position).getSender());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Snapshot", snapshot.toString());
                UserFirebase userFirebase = snapshot.getValue(UserFirebase.class);
                holder.username.setText(userFirebase.getUsername());
                if (userFirebase.getImageURL().equals("default")){
                    holder.profile_image.setImageResource(R.drawable.ic_baseline_account_circle_24);
                } else {
                    Glide.with(mContext).load(userFirebase.getImageURL()).into(holder.profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final AppCompatActivity appCompatActivity = (AppCompatActivity) mContext;
        final FragmentManager manager = ((AppCompatActivity) mContext).getSupportFragmentManager();

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("id", mUsers.get(position).getSender());
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, profileFragment).addToBackStack(null).commit();
            }
        });

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("id", mUsers.get(position).getSender());
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, profileFragment).addToBackStack(null).commit();
            }
        });

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.accept.getText().toString().equals("Accept")) {
                    accept(mUsers.get(position).getFriendid());
                    holder.reject.setVisibility(View.GONE);
                    holder.accept.setText("Chat now");
                } else if(holder.accept.getText().toString().equals("Chat now")){
                    Intent intent = new Intent(mContext, MessageActivity.class);
                    intent.putExtra("userid", mUsers.get(position).getSender());
                    mContext.startActivity(intent);
                }
            }
        });

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.reject.getText().toString().equals("Reject")) {
                    reject(mUsers.get(position).getFriendid());
                    holder.accept.setVisibility(View.GONE);
                    holder.reject.setText("Rejected");
                }
            }
        });

    }

    public void accept(String fid){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_ACCEPT + "?friendid=" + fid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray userJson = obj.getJSONArray("user");
                            JSONObject jsonObject = userJson.getJSONObject(0);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        VolleySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void reject(String  fid){
        Log.d("URL", URLs.URL_REJEcT + "?friendid=" + fid);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_REJEcT + "?friendid=" + fid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        VolleySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public CircleImageView profile_image;
        public TextView accept;
        public TextView reject;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.image);
            accept = itemView.findViewById(R.id.accept);
            reject = itemView.findViewById(R.id.reject);
        }
    }
}
