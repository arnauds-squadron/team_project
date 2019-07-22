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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter to handle adding tags to the user's event and displaying them in a ListView
 */
public class TagAdapter extends ArrayAdapter<String> {

    @BindView(R.id.tvName)
    TextView tvName;

    @BindView(R.id.ibClose)
    ImageButton ibClose;

    private List<String> tags;

    TagAdapter(Context context, List<String> tags) {
        super(context, 0, tags);
        this.tags = tags;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) { // Check to see if we can reuse the view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tag,
                    parent, false);
        }

        ButterKnife.bind(this, convertView);
        tvName.setText(getItem(position));
        removeTagOnClick(ibClose, position);

        return convertView;
    }

    /**
     * Removes an item when the image button is clicked, not using ButterKnife annotation because
     * we need the position
     * @param button The button being clicked
     * @param position The position of the view in the ListView
     */
    private void removeTagOnClick(ImageButton button, final int position) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tags.remove(position);
                notifyDataSetChanged();
            }
        });
    }
}
