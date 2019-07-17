package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;

import java.util.ArrayList;

import butterknife.BindView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    ArrayList<Home> mAgenda;
    Context context;

    public HomeAdapter(ArrayList<Home> mAgenda) {
        this.mAgenda = mAgenda;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_agenda, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Home home = mAgenda.get(i);

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivProfile)
        ImageView ivProfile;
        @BindView(R.id.btnCancel)
        Button btnCancel;
        @BindView(R.id.tvDate)
        TextView tvDate;
        @BindView(R.id.tvPerson)
        TextView tvPerson;
        @BindView(R.id.tvPlace)
        TextView tvPlace;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
