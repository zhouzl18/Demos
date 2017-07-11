package com.feng.recyclerviewdemo.layoutmanager;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by OneDay on 2017/7/11.
 *
 * 流式布局
 *
 */

public class FlowLayoutManager extends RecyclerView.LayoutManager {

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }



}
