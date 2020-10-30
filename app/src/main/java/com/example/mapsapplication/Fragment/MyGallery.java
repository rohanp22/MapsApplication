package com.example.mapsapplication.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.mapsapplication.R;

import java.util.ArrayList;
import java.util.List;

public class MyGallery extends Fragment {

    public MyGallery() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
//        ListView l = view.findViewById(R.id.list);
//        ArrayList<String> name = new ArrayList<>();
//
//        List<String> your_array_list = new ArrayList<String>();
//        your_array_list.add("foo");
//        your_array_list.add("bar");
//
//        for(int i = 0 ; i < 10; i++){
//            your_array_list.add(""+ i);
//        }
//
//        // This is the array adapter, it takes the context of the activity as a
//        // first parameter, the type of list view as a second parameter and your
//        // array as a third parameter.
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
//                view.getContext(),
//                android.R.layout.simple_list_item_1,
//                your_array_list );
//
//        l.setAdapter(arrayAdapter);
        return view;
    }
}