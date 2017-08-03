package com.feng.recyclerviewdemo.view.flowlayout.layoutmanager;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by OneDay on 2017/7/11.
 *
 * 流式布局管理器
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

    private SparseArray<Rect> mItemRect;

    public FlowLayoutManager() {
        setAutoMeasureEnabled(true);
        mItemRect = new SparseArray<>();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        Log.i(TAG, "generateDefaultLayoutParams: ");
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.i(TAG, "onLayoutChildren: childCount = " + getChildCount());
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

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.i(TAG, "scrollVerticallyBy: dy = " + dy);
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

    /**
     * 1.回收屏幕不可见的view
     * 2.layout所有可见的子view
     * @param recycler
     * @param state
     */
    /**
     * 初始化时调用 填充childView
     *
     * @param recycler
     * @param state
     */
    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        fill(recycler, state, 0);
    }

    /**
     * 填充childView的核心方法,应该先填充，再移动。
     * 在填充时，预先计算dy的在内，如果View越界，回收掉。
     * 一般情况是返回dy，如果出现View数量不足，则返回修正后的dy.
     *
     * @param recycler
     * @param state
     * @param dy       RecyclerView给我们的位移量,+,显示底端， -，显示头部
     * @return 修正以后真正的dy（可能剩余空间不够移动那么多了 所以return <|dy|）
     */
    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {

        int topOffset = getPaddingTop();

        //回收越界子View
        if (getChildCount() > 0) {//滑动时进来的
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (dy > 0) {//需要回收当前屏幕，上越界的View
                    if (getDecoratedBottom(child) - dy < topOffset) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiPos++;
                        continue;
                    }
                } else if (dy < 0) {//回收当前屏幕，下越界的View
                    if (getDecoratedTop(child) - dy > getHeight() - getPaddingBottom()) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiPos--;
                        continue;
                    }
                }
            }
            //detachAndScrapAttachedViews(recycler);
        }

        int leftOffset = getPaddingLeft();
        int lineMaxHeight = 0;
        //布局子View阶段
        if (dy >= 0) {
            int minPos = mFirstVisiPos;
            mLastVisiPos = getItemCount() - 1;
            if (getChildCount() > 0) {
                View lastView = getChildAt(getChildCount() - 1);
                minPos = getPosition(lastView) + 1;//从最后一个View+1开始吧
                topOffset = getDecoratedTop(lastView);
                leftOffset = getDecoratedRight(lastView);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(lastView));
            }
            //顺序addChildView
            for (int i = minPos; i <= mLastVisiPos; i++) {
                //找recycler要一个childItemView,我们不管它是从scrap里取，还是从RecyclerViewPool里取，亦或是onCreateViewHolder里拿。
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, 0, 0);
                //计算宽度 包括margin
                if (leftOffset + getDecoratedMeasurementHorizontal(child) <= getHorizontalSpace()) {//当前行还排列的下
                    layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child));

                    //保存Rect供逆序layout用
                    Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                    mItemRect.put(i, rect);

                    //改变 left  lineHeight
                    leftOffset += getDecoratedMeasurementHorizontal(child);
                    lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                } else {//当前行排列不下
                    //改变top  left  lineHeight
                    leftOffset = getPaddingLeft();
                    topOffset += lineMaxHeight;
                    lineMaxHeight = 0;

                    //新起一行的时候要判断一下边界
                    if (topOffset - dy > getHeight() - getPaddingBottom()) {
                        //越界了 就回收
                        removeAndRecycleView(child, recycler);
                        mLastVisiPos = i - 1;
                    } else {
                        layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child));

                        //保存Rect供逆序layout用
                        Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                        mItemRect.put(i, rect);

                        //改变 left  lineHeight
                        leftOffset += getDecoratedMeasurementHorizontal(child);
                        lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                    }
                }
            }
            //添加完后，判断是否已经没有更多的ItemView，并且此时屏幕仍有空白，则需要修正dy
            View lastChild = getChildAt(getChildCount() - 1);
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    dy -= gap;
                }

            }

        } else {
            /**
             * ##  利用Rect保存子View边界
             正序排列时，保存每个子View的Rect，逆序时，直接拿出来layout。
             */
            int maxPos = getItemCount() - 1;
            mFirstVisiPos = 0;
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                maxPos = getPosition(firstView) - 1;
            }
            for (int i = maxPos; i >= mFirstVisiPos; i--) {
                Rect rect = mItemRect.get(i);

                if (rect.bottom - mVerticalOffset - dy < getPaddingTop()) {
                    mFirstVisiPos = i + 1;
                    break;
                } else {
                    View child = recycler.getViewForPosition(i);
                    addView(child, 0);//将View添加至RecyclerView中，childIndex为1，但是View的位置还是由layout的位置决定
                    measureChildWithMargins(child, 0, 0);

                    layoutDecoratedWithMargins(child, rect.left, rect.top - mVerticalOffset, rect.right, rect.bottom - mVerticalOffset);
                }
            }
        }


        //Log.d("TAG", "count= [" + getChildCount() + "]" + ",[recycler.getScrapList().size():" + recycler.getScrapList().size() + ", dy:" + dy + ",  mVerticalOffset" + mVerticalOffset + ", ");

        return dy;
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

}
