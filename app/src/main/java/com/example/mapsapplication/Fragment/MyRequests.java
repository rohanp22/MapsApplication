package com.example.mapsapplication.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mapsapplication.Adapter.FriendRequestAdapter;
import com.example.mapsapplication.Model.FriendRequest;
import com.example.mapsapplication.Others.SharedPrefManager;
import com.example.mapsapplication.Others.URLs;
import com.example.mapsapplication.Others.VolleySingleton;
import com.example.mapsapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyRequests extends Fragment {

    public ArrayList<FriendRequest> friendRequests;
    public RecyclerView recyclerView;
    FragmentManager fragmentManager;

    public MyRequests(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_my_requests, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        friendRequests = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_REQUESTS + "?id=" + SharedPrefManager.getInstance(view.getContext()).getUser().getId(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            friendRequests.clear();
                            JSONObject obj = new JSONObject(response);
                            JSONArray heroArray = obj.getJSONArray("requests");
                            Log.d("output", response);
                            for (int i = 0; i < heroArray.length(); i++) {
                                JSONObject heroObject = heroArray.getJSONObject(i);
                                FriendRequest c = new FriendRequest(
                                        heroObject.getString("friendid"),
                                        heroObject.getString("sender"),
                                        heroObject.getString("receiver"),
                                        heroObject.getString("status")
                                );
                                friendRequests.add(c);
                            }
                            FriendRequestAdapter friendRequestAdapter = new FriendRequestAdapter(view.getContext(), friendRequests, fragmentManager);
                            recyclerView.setAdapter(friendRequestAdapter);

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
        VolleySingleton.getInstance(view.getContext()).addToRequestQueue(stringRequest);

        return view;
    }
}
