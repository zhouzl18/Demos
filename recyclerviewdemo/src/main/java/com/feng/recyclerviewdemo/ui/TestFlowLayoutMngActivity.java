package com.feng.recyclerviewdemo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.feng.recyclerviewdemo.R;
import com.feng.recyclerviewdemo.adapter.FlowLayoutAdapter;
import com.feng.recyclerviewdemo.view.flowlayout.layoutmanager.FlowLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by OneDay on 2017/7/18.
 *
 * 测试FlowLayoutManager
 *
 */

public class TestFlowLayoutMngActivity extends BaseActivity{

    List<String> mData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_layout_mng);

        init();
    }

    private void init() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.flow_layout_recycler);
        FlowLayoutManager manager = new FlowLayoutManager();
        recyclerView.setLayoutManager(manager);
        FlowLayoutAdapter adapter = new FlowLayoutAdapter(this, getData());
        recyclerView.setAdapter(adapter);
    }

    private List<String> getData(){
        if(mData == null){
            mData = new ArrayList<>();
        }
        mData.clear();
        String[] temp = getResources().getStringArray(R.array.flow_tag);
        mData.addAll(Arrays.asList(temp));
        return mData;
    }
}
