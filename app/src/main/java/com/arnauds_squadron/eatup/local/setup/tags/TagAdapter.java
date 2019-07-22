package com.arnauds_squadron.eatup.local.setup.tags;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;

import java.util.List;

import butterknife.ButterKnife;

public class TagAdapter extends ArrayAdapter<String> {

    private List<String> tags;

    TagAdapter(Context context, List<String> tags) {
        super(context, 0, tags);
        this.tags = tags;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        ButterKnife.bind(this, convertView);

        String tag = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tag,
                    parent, false);
        }
        TextView tvName = convertView.findViewById(R.id.tvName);
        ImageButton ibClose = convertView.findViewById(R.id.ibClose);

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tags.remove(position);
                notifyDataSetChanged();
            }
        });

        tvName.setText(tag);
        return convertView;
    }
}
