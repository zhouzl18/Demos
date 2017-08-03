package com.feng.recyclerviewdemo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.feng.recyclerviewdemo.R;

import java.util.List;

/**
 * Created by OneDay on 2017/7/18.
 */

public class FlowLayoutAdapter extends RecyclerView.Adapter<FlowLayoutAdapter.FlowHolder> {

    LayoutInflater mInflater;
    List<String> mData;

    public FlowLayoutAdapter(@NonNull Context context, List<String> data){
        mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public FlowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FlowHolder(mInflater.inflate(R.layout.item_flow_layout_text, parent, false));
    }

    @Override
    public void onBindViewHolder(FlowHolder holder, int position) {
        holder.tvName.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class FlowHolder extends RecyclerView.ViewHolder{
        private TextView tvName;
        public FlowHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.name);
        }
    }
}
