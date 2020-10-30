package com.example.mapsapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.mapsapplication.Adapter.MessageActivity;
import com.example.mapsapplication.Adapter.MyAdapter;
import com.example.mapsapplication.Model.UserFirebase;
import com.example.mapsapplication.Others.SharedPrefManager;
import com.example.mapsapplication.Others.URLs;
import com.example.mapsapplication.Others.VolleySingleton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile2 extends Fragment {

    LinearLayout chatNow;
    LinearLayout addFriend;
    String userid = "";
    CircleImageView image_profile;
    TextView profileName;
    TextView profileEmail;
    TabLayout tabLayout;
    TextView fstatus;
    ViewPager viewPager;
    Context context;
    ImageView image;
    String myid = SharedPrefManager.getInstance(context).getUser().getId() + "";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_profile, container, false);

        context = root.getContext();
        fstatus = root.findViewById(R.id.fstatus);
        addFriend = root.findViewById(R.id.addFriend);
        addFriend.setVisibility(View.GONE);
        image = root.findViewById(R.id.image);
        chatNow = root.findViewById(R.id.chatNow);
        profileName = root.findViewById(R.id.myProfileName);
        profileEmail = root.findViewById(R.id.myProfileEmail);
        image_profile = root.findViewById(R.id.myProfileImage);

        Bundle b = getArguments();
        userid = b.getString("id");

        loadInfo();

//        chatNow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(root.getContext(), MessageActivity.class);
//                intent.putExtra("userid", userid);
//                root.getContext().startActivity(intent);
//            }
//        });

        tabLayout = root.findViewById(R.id.tablayout);
        viewPager = root.findViewById(R.id.viewPager);
        tabLayout.addTab(tabLayout.newTab().setText("My Places"));
        tabLayout.addTab(tabLayout.newTab().setText("Gallery"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        MyAdapter adapter = new MyAdapter(root.getContext(), getChildFragmentManager(),
                tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        isFriend();

        //tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().onBackPressed();
//            }
//        });
        System.out.println("Userid : " + userid);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserFirebase userFirebase = dataSnapshot.getValue(UserFirebase.class);
                profileName.setText(userFirebase.getUsername());
                if (userFirebase.getImageURL().equals("default")){
                    image_profile.setImageResource(R.drawable.ic_baseline_account_circle_24);
                } else {
                    Glide.with(root.getContext()).load(userFirebase.getImageURL()).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return root;
    }

    public void isFriend(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_ISFRIEND + "?id=" + myid + "&id2=" + userid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            Log.d("Relation", response);
                            JSONArray userJson = obj.getJSONArray("re");
                            Log.d("userJson", userJson.length() + "");
                            if(userJson.length() != 0) {
                                JSONObject jsonObject = userJson.getJSONObject(userJson.length() - 1);
                                String sender = jsonObject.getString("sender");
                                final String fid = jsonObject.getString("friendid");
                                String receiver = jsonObject.getString("receiver");
                                String status = jsonObject.getString("status");

                                if (status.equals("SENT")) {
                                    if (sender.equals(myid)) {
                                        fstatus.setText("Request sent");
                                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_person_add_24));
                                    } else if (receiver.equals(myid)) {
                                        fstatus.setText("Accept request");
                                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_person_add_24));
                                    }
                                } else if (status.equals("FRIENDS")) {
                                    fstatus.setText("Chat now");
                                    image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_chat_24));
                                } else {
                                    fstatus.setText("Add friend");
                                    image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_person_add_24));
                                }

                                addFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        switch (fstatus.getText().toString()) {
                                            case "Request sent":
                                                break;
                                            case "Chat now":
                                                Intent intent = new Intent(context, MessageActivity.class);
                                                intent.putExtra("userid", userid);
                                                context.startActivity(intent);
                                                break;
                                            case "Accept request":
                                                acceptFriendRequest(fid);
                                                break;
                                            case "Add friend":
                                                sendFriendRequest();
                                                break;
                                        }
                                    }
                                });
                            } else {
                                fstatus.setText("Add Friend");
                                image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_person_add_24));
                                addFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sendFriendRequest();
                                    }
                                });
                            }

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
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    public void sendFriendRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_SENDREQUEST + "?sender=" + SharedPrefManager.getInstance(context).getUser().getId() + "&receiver=" + userid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        fstatus.setText("Request sent");
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_person_add_24));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    public void acceptFriendRequest(String fid) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_ACCEPT + "?friendid=" + fid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        fstatus.setText("Chat now");
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_chat_24));
                        addFriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, MessageActivity.class);
                                intent.putExtra("userid", userid);
                                context.startActivity(intent);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    public void loadInfo() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_INFO + "?id=" + userid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray userJson = obj.getJSONArray("user");
                            JSONObject jsonObject = userJson.getJSONObject(0);
                            profileEmail.setText(jsonObject.getString("email"));

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
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }
}
