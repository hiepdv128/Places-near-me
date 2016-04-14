package com.truongpq.placesnearme.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.truongpq.placesnearme.R;
import com.truongpq.placesnearme.models.Place;

import java.util.List;

/**
 * Created by TruongPQ on 4/11/16.
 */
public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder>{

    private List<Place> places;

    public PlacesAdapter(List<Place> places) {
        this.places = places;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View placeView = inflater.inflate(R.layout.item_place, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(placeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Place place = places.get(position);

        TextView tvName = holder.tvName;
        tvName.setText(place.getName());
        TextView tvVicinity = holder.tvVicinity;
        tvVicinity.setText(place.getVicinity());

    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvVicinity;

        public ViewHolder(View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvVicinity = (TextView) itemView.findViewById(R.id.tv_vicinity);
        }
    }
}
