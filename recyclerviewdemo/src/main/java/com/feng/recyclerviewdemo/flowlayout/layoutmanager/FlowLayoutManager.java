package com.feng.recyclerviewdemo.flowlayout.layoutmanager;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by OneDay on 2017/7/11.
 *
 * 流式布局
 *
 */

public class FlowLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "FlowLayoutManager";

    //垂直偏移量，每次换行时，根据这个偏移量判断
    private int mVerticalOffset;
    //第一个可见的child
    private int mFirstVisiPos;
    //最后一个可见的child
    private int mLastVisiPos;

    private SparseArray<Rect> mItemRects = new SparseArray<>();

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        Log.i(TAG, "generateDefaultLayoutParams: ");
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //super.onLayoutChildren(recycler, state);
        //如果没有item,界面就空着
        if(getItemCount() == 0){
            detachAndScrapAttachedViews(recycler);
            return;
        }

        //state.isPreLayout是支持动画的
        if(getChildCount() == 0 && state.isPreLayout()){
            return;
        }

        //onLayoutChildren在RecyclerView初始化时会调用两次
        detachAndScrapAttachedViews(recycler);

        mVerticalOffset = 0;
        mFirstVisiPos = 0;
        mLastVisiPos = getItemCount();

        //填充childView
        fill(recycler, state);
    }

    /**
     * 1.回收屏幕不可见的view
     * 2.layout所有可见的子view
     * @param recycler
     * @param state
     */
    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //布局时的左偏移
        int leftOffset = getPaddingLeft();
        //布局时的上偏移
        int topOffset = getPaddingTop();
        //行高
        int lineMaxHeight = 0;

        //初始化时我们并不知道要layout多少个childView,假设从0到getItemCount()-1;
        int minPos = mFirstVisiPos;
        mLastVisiPos = getItemCount() - 1;

        for(int i = minPos; i < mLastVisiPos; i++){
            //找recycler要一个childItemView,我们不管它是从scrap里面取，还是RecyclerViewPool里面取，还是onCreateViewHolder里面拿
            View child = recycler.getViewForPosition(i);
            addView(child);
            measureChildWithMargins(child, 0, 0);
            if(leftOffset + getDecoratedMeasurementHorizontal(child) <= getHorizontalSpace()){
                //当前行还能排列下child
                layoutDecoratedWithMargins(child, leftOffset, topOffset,
                        leftOffset + getDecoratedMeasurementHorizontal(child),
                        topOffset + getDecoratedMeasurementVertical(child));
                leftOffset += getDecoratedMeasurementHorizontal(child);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
            }else{
                //当前行排不下
                leftOffset = getPaddingLeft();
                topOffset += lineMaxHeight;
                lineMaxHeight = 0;

                //新行要判断下边界
                int dy = 0;
                if(topOffset - dy > getHeight() - getPaddingBottom()){
                    //越界了就回收
                    removeAndRecycleView(child, recycler);
                    mLastVisiPos = i - 1;
                }else{
                    //可以layout
                    layoutDecoratedWithMargins(child, leftOffset, topOffset,
                            leftOffset + getDecoratedMeasurementHorizontal(child),
                            topOffset + getDecoratedMeasurementVertical(child));
                    leftOffset += getDecoratedMeasurementHorizontal(child);
                    lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                }
            }

        }
    }

    /**
     *
     * @param recycler
     * @param state
     * @param dy    recyclerView给我们的位移量, dy>0,显示底部；dy<0,显示顶部
     * @return  返回修正的dy（可能剩余空间不够移动那么多了，所以return < |dy|）
     */
    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {
        int topOffset = getPaddingTop();

        //回收越界子view
        if(getChildCount() > 0){
            //滑动时进来的子view
            for(int i = getChildCount() - 1; i >= 0; i--){
                View child = getChildAt(i);
                if(dy > 0){
                    if(getDecoratedBottom(child) - dy < topOffset){
                        //回收当前屏幕上越界的子view
                        removeAndRecycleView(child, recycler);
                        mFirstVisiPos++;
                        continue;
                    }
                }else if(dy < 0){
                    if(getDecoratedTop(child) - dy > getHeight() - getPaddingBottom()){
                        //回收当前屏幕下越界的子view
                        removeAndRecycleView(child, recycler);
                        mLastVisiPos--;
                        continue;
                    }
                }
            }
        }

        int leftOffset = getPaddingLeft();
        int lineMaxHeight = 0;

        //布局子view阶段
        if(dy >= 0){
            int minPos = mFirstVisiPos;
            mLastVisiPos = getItemCount() - 1;
            if(getChildCount() > 0){
                View lastView = getChildAt(getChildCount() - 1);
                //从最后一个view + 1开始
                minPos = getPosition(lastView) + 1;
                topOffset = getDecoratedTop(lastView);
                leftOffset = getDecoratedRight(lastView);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(lastView));
            }

            // TODO: 2017/7/18
            //顺序添加子view
            for(int i = minPos; i < mLastVisiPos; i++){
                //找recycler要一个childItemView,我们不管它是从scrap里取，还是从RecyclerViewPool里取，亦或是onCreateViewHolder里拿。
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, 0, 0);
                //计算宽度包括margin
                if(leftOffset + getDecoratedMeasurementHorizontal(child) <= getHorizontalSpace()){
                    layoutDecoratedWithMargins(child, leftOffset, topOffset,
                            leftOffset + getDecoratedMeasurementHorizontal(child),
                            topOffset + getDecoratedMeasurementVertical(child));

                    //保存Rect共逆序layout用
                    Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset,
                            leftOffset + getDecoratedMeasurementHorizontal(child),
                            topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                    mItemRects.put(i, rect);

                    //改变left lineMaxHeight
                    leftOffset += getDecoratedMeasurementHorizontal(child);
                    lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                }else{
                    //当前行排列不下，需要换行
                    // TODO: 2017/7/18  
                }
            }
        }

        return 0 ;
    }

    /**
     * 获取某个childView在水平方向上所占的空间
     * @param view  child
     * @return  水平距离
     */
    public int getDecoratedMeasurementHorizontal(View view){
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
    }

    /**
     * 获取某个childView在垂直方向上所占的空间
     * @param view  child
     * @return  垂直距离
     */
    public int getDecoratedMeasurementVertical(View view){
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
    }

    /**
     * 父容器的内宽度
     * @return 宽度
     */
    public int getHorizontalSpace(){
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    /**
     * 父容器的内高度
     * @return
     */
    public int getVerticalSpace(){
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //没有位移、没有子view，不移动
        if(dy == 0 || getChildCount() == 0){
            return 0;
        }
        //实际滑动的距离，可能会在边界处修复
        int realOffset = dy;
        if(mVerticalOffset + realOffset < 0){
            //上边界
            realOffset = -mVerticalOffset;
        }else if(realOffset > 0){
            //下边界
            //利用最后一个子view比较修正
            View lastChild = getChildAt(getChildCount() - 1);
            if(getPosition(lastChild) == getItemCount() - 1){
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if(gap > 0){
                    realOffset = -gap;
                }else if(gap == 0){
                    realOffset = 0;
                }else{
                    realOffset = Math.min(realOffset, -gap);
                }
            }
        }

        //先填充，再位移
        realOffset = fill(recycler, state, realOffset);

        //累加实际滑动距离
        mVerticalOffset += realOffset;

        offsetChildrenVertical(-realOffset);
        return realOffset;
    }


}
