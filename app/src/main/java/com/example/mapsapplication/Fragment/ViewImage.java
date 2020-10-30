package com.example.mapsapplication.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mapsapplication.R;

public class ViewImage extends Fragment{

    String url;
    ImageView image;
    AppCompatActivity app;
    FragmentManager fragmentManager;
    View view;

    public ViewImage(FragmentManager fragmentManager){
        this.fragmentManager = fragmentManager;
    }

    public ViewImage(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_image, container, false);

        final AppCompatActivity appCompatActivity = (AppCompatActivity) view.getContext();
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        image = view.findViewById(R.id.image);

        app = (AppCompatActivity) view.getContext();

        Bundle b = getArguments();
        url = b.getString("url");

        if (url.equals("default")){
            image.setImageResource(R.drawable.ic_baseline_account_circle_24);
        } else {
            if(getContext() != null) {
                Glide.with(getContext()).load(url).into(image);
            }
        }

        app.setSupportActionBar(toolbar);
        app.getSupportActionBar().setTitle("");
        app.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appCompatActivity.onBackPressed();
            }
        });

        return view;
    }
}
