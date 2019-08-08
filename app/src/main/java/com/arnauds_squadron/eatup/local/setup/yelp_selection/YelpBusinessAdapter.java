package com.arnauds_squadron.eatup.local.setup.yelp_selection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Location;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class YelpBusinessAdapter extends RecyclerView.Adapter<YelpBusinessAdapter.ViewHolder> {

    private Context context;
    private List<Business> mBusiness;
    private YelpBusinessFragment yelpBusinessFragment;

    YelpBusinessAdapter(Context context, List<Business> mBusiness, YelpBusinessFragment fragment) {
        this.context = context;
        this.mBusiness = mBusiness;
        this.yelpBusinessFragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_yelp_business, viewGroup, false);
        return new ViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        Business business = mBusiness.get(i);
        Location location = business.location;
        viewHolder.tvPlace.setText(business.name);
        String address = location.getAddress1() + " "
                + location.getCity() + ", "
                + location.getState();

        viewHolder.tvAddress.setText(address);
        Glide.with(context)
                .load(business.imageUrl)
                .into(viewHolder.ivYelpImage);
    }

    @Override
    public int getItemCount() {
        return mBusiness.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivYelpImage)
        ImageView ivYelpImage;
        @BindView(R.id.tvPlace)
        TextView tvPlace;
        @BindView(R.id.tvAddress)
        TextView tvAddress;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    notifyItemChanged(position);
                    yelpBusinessFragment.goToNextFragment(position);
                }
            });
        }
    }
}
