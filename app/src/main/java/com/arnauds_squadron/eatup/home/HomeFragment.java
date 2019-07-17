package com.arnauds_squadron.eatup.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arnauds_squadron.eatup.R;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private HomeAdapter homeAdapter;
    ArrayList<Home> agenda;

    @BindView(R.id.rvAgenda)
    RecyclerView rvAgenda;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        RecyclerView rvAgenda = (RecyclerView) view.findViewById(R.id.rvAgenda);
        rvAgenda.setLayoutManager(new LinearLayoutManager(getActivity()));
        homeAdapter = new HomeAdapter(agenda);
        rvAgenda.setAdapter(homeAdapter);
        return view;
    }
}
