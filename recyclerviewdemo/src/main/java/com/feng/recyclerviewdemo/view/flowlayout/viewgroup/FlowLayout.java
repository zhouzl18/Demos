package com.feng.recyclerviewdemo.view.flowlayout.viewgroup;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OneDay on 2017/7/12.
 *
 * 流式布局：控件根据ViewGroup的宽，自动的往右添加，如果当前行剩余空间不足，则自动添加到下一行
 *
 */

public class FlowLayout extends ViewGroup{

    private static final String TAG = "FlowLayout";

    /**
     * 保存所有的childView
     */
    private List<List<View>> mAllViews = new ArrayList<>();

    /**
     * 保存每行的高度
     */
    private List<Integer> mLineHeightList = new ArrayList<>();

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mAllViews.clear();
        mLineHeightList.clear();
        //获取父容器指定的宽度和高度以及测量模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        Log.i(TAG, "onMeasure: widthSize = " + widthSize + ", heightSize = " + heightSize);

        //如果是wrap_content模式，记录宽和高
        int width = 0;
        int height = 0;

        //记录每行的宽度，width根据行宽取最大值
        int lineWidth = 0;
        //记录每行行的高度，将行高累加至height
        int lineHeight = 0;

        List<View> lineViews = new ArrayList<>();
        int cCount = getChildCount();

        for(int i=0; i<cCount; i++){
            View child = getChildAt(i);
            //测量child的尺寸
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            MarginLayoutParams childParams = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + childParams.leftMargin + childParams.rightMargin;
            if(childWidth > widthSize){
                childWidth = widthSize;
            }
            int childHeight = child.getMeasuredHeight() + childParams.topMargin + childParams.bottomMargin;

            if(lineWidth + childWidth <= widthSize){
                //当前的宽度小于测量的宽度
                //lineViews.add(child);
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }else{
                //当前宽度大于测量的宽度,需要换行

                //添加行views
                mAllViews.add(lineViews);
                //创建新行
                lineViews = new ArrayList<>();
                //添加行高
                mLineHeightList.add(lineHeight);

                //记录最大宽度
                width = Math.max(lineWidth, childWidth);
                //叠加高度
                height += lineHeight;


                //记录新的一行的宽度
                lineWidth = childWidth;
                //记录新的一行的高度
                lineHeight = childHeight;
            }

            lineViews.add(child);

            //最后一行
            if(i == cCount - 1){
                //添加行
                mAllViews.add(lineViews);
                //添加行高
                mLineHeightList.add(lineHeight);
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }

        }

        //最终的尺寸
        //如果为确切的值，则使用父容器传入的宽和高，否则使用计算的宽和高
        width = widthMode == MeasureSpec.EXACTLY ? widthSize : width;
        //如果存储的宽度大于容器的宽度，将最大宽度限定在父容器指定的最大宽度内
        if(width > widthSize){
            width = widthSize;
        }
        height = heightMode == MeasureSpec.EXACTLY ? heightSize : height;

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG, "onLayout: changed = " + changed + " [" + l + ", " + t + ", " + r + ", " + b + "]");

        int width = getWidth();
        Log.w(TAG, "onLayout: width = " + width);

        int left = 0;
        int top = 0;
        //总行数
        int lineNum = mAllViews.size();
        for(int i=0; i<lineNum; i++){
            //当前行的所有Views
            List<View> lineViews = mAllViews.get(i);
            //当前行的高度
            int lineHeight = mLineHeightList.get(i);

            //遍历行的所有view
            for(int j = 0; j < lineViews.size(); j++){
                TextView childView = (TextView) lineViews.get(j);
                Log.d(TAG, "onLayout: text = " + childView.getText());
                if(childView.getVisibility() == View.GONE) continue;

                MarginLayoutParams cParams = (MarginLayoutParams) childView.getLayoutParams();
                //计算childView的left, top, right, bottom
                int cl = left + cParams.leftMargin;
                int ct = top + cParams.topMargin;
                int tempChildWidth = childView.getMeasuredWidth();
                if(tempChildWidth == width){
                    tempChildWidth = tempChildWidth - cParams.leftMargin - cParams.rightMargin;
                }
                int cr = cl + tempChildWidth;
                int cb = ct + childView.getMeasuredHeight();

                childView.layout(cl, ct, cr, cb);
                left = cr + cParams.rightMargin;
            }

            //重置left
            left = 0;
            top += lineHeight;
        }
    }
}
