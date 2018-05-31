package com.dev.kostento.radioonline;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<RadioStation> mDataset;
    private SelectStationListener selectStationListener;

    public void addItem(String m_textName, String m_textuUrl) {
        mDataset.add(new RadioStation(m_textName,m_textuUrl));
        notifyItemChanged(mDataset.size()-1);
    }

    public ArrayList<RadioStation> getData() {
        return mDataset;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public ImageView mImageView;
        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.txtTitle);
            mImageView= v.findViewById(R.id.imgIcon);
        }
    }

    public RecyclerViewAdapter(ArrayList<RadioStation> mDataset,SelectStationListener selectStationListener) {
        this.mDataset = mDataset;
        this.selectStationListener= selectStationListener;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,  int viewType) {

        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder,final int position) {

        holder.mTextView.setText(mDataset.get(position).getStantion());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStationListener.onSelect(mDataset.get(position));
            }
        });

    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    interface SelectStationListener{

          void onSelect(RadioStation radioStation);
    }
}