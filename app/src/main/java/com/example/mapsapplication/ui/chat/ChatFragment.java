package com.example.mapsapplication.ui.chat;

import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapsapplication.Adapter.UserAdapter;
import com.example.mapsapplication.Model.ChatList;
import com.example.mapsapplication.Model.UserFirebase;
import com.example.mapsapplication.Others.SharedPrefManager;
import com.example.mapsapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.example.mapsapplication.Model.Token;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<UserFirebase> mUsers;

    DatabaseReference reference;

    private List<ChatList> usersList;
    EditText editTextSearch;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        editTextSearch = view.findViewById(R.id.search);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //after the change calling the method and passing the search input
                filter(editable.toString());
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersList = new ArrayList<ChatList>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(SharedPrefManager.getInstance(getContext()).getUser().getId() + "");
        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatList chatlist = snapshot.getValue(ChatList.class);
                    usersList.add(chatlist);
                }
                usersList.sort(new sortTime());
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());
        return view;
    }

    class sortTime implements Comparator<ChatList> {
        public int compare(ChatList a, ChatList b) {
            long a1 = 0, b1 = 0;
            a1 = Long.parseLong(a.getTime());
            b1 = Long.parseLong(b.getTime());

            return (int)(b1 - a1);
        }
    }

    private void filter(String text) {
        //new array list that will hold the filtered data
        ArrayList<UserFirebase> filterdNames = new ArrayList<>();

        if(!text.equals("")) {
            //looping through existing elements
            for (UserFirebase s : mUsers) {
                //if the existing elements contains the search input
                if (s.getSearch().toLowerCase().contains(text.toLowerCase())) {
                    Log.d("Names", s.getSearch());
                    //adding the element to filtered list
                    filterdNames.add(s);
                }
            }

            //calling a method of the adapter class and passing the filtered list
            userAdapter.filterList(filterdNames);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.chat_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_icon);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search chat");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(SharedPrefManager.getInstance(getContext()).getUser().getId()+"").setValue(token1);
    }

    private void chatList() {
        mUsers = new ArrayList<UserFirebase>();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserFirebase user = snapshot.getValue(UserFirebase.class);
                    for (ChatList chatlist : usersList){
                        if (user.getId().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    }
                }
                ArrayList<UserFirebase> userFirebases2 = copyOrder();
                userAdapter = new UserAdapter(getContext(), userFirebases2, true);
                recyclerView.setAdapter(userAdapter); }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public ArrayList<UserFirebase> copyOrder(){
        ArrayList<UserFirebase> users2 = new ArrayList<>();
        for(int i = 0; i < usersList.size(); i++){
            for(int j = 0; j < mUsers.size(); j++){
                if(usersList.get(i).getId().equals(mUsers.get(j).getId())){
                    users2.add(mUsers.get(j));
                }
            }
        }
        return users2;
    }
}