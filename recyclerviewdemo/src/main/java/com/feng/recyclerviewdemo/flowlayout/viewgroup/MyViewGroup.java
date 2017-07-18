package com.feng.recyclerviewdemo.flowlayout.viewgroup;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by OneDay on 2017/7/12.
 *
 * 简单的自定义ViewGroup
 * 总共有4个childView: 分别在左上、右上、左下、右下四个方向上
 *
 */

public class MyViewGroup extends ViewGroup {

    public MyViewGroup(Context context) {
        super(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取父容器的测量模式以及推荐的宽高
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        //计算所有的child的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;

        int childCount = getChildCount();
        int childWidth = 0;
        int childHeight = 0;

        //定义4个变量，分别表示上面、下面的宽度，左边、右边的高度
        int topWidth = 0;
        int bottomWidth = 0;
        int leftHeight = 0;
        int rightHeight = 0;

        //计算当前容器的宽和高
        for(int i=0; i<childCount; i++){
            View childView = getChildAt(i);
            MarginLayoutParams childParams = (MarginLayoutParams) childView.getLayoutParams();
            childWidth = childView.getMeasuredWidth();
            childHeight = childView.getMeasuredHeight();

            //计算上边的宽度
            if(i == 0 || i == 1){
                topWidth += childWidth + childParams.leftMargin + childParams.rightMargin;
            }

            //计算下边的宽度
            if(i == 2 || i == 3){
                bottomWidth += childWidth + childParams.leftMargin + childParams.rightMargin;
            }

            //计算左边的高度
            if(i == 0 || i == 2){
                leftHeight += childHeight + childParams.topMargin + childParams.bottomMargin;
            }

            //计算右边的高度
            if(i == 1 || i == 3){
                rightHeight += childHeight + childParams.topMargin + childParams.bottomMargin;
            }

        }

        //取最大值
        width = Math.max(topWidth, bottomWidth);
        height = Math.max(leftHeight, rightHeight);

        //根据测量模式设置宽高
        width = wMode == MeasureSpec.EXACTLY ? wSize : width;
        height = hMode == MeasureSpec.EXACTLY ? hSize : height;

        //设置测量的尺寸
        setMeasuredDimension(width, height);

    }

    /**
     * 对所有的childView进行定位(设置childView的绘制区域)
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int height = getHeight();

        int cWidth = 0;
        int cHeight = 0;

        for(int i=0; i<getChildCount(); i++){
            View cView = getChildAt(i);
            MarginLayoutParams cParams = (MarginLayoutParams) cView.getLayoutParams();
            cWidth = cView.getMeasuredWidth();
            cHeight = cView.getMeasuredHeight();

            int cl = 0;
            int ct = 0;
            switch (i){
                case 0:
                    cl = cParams.leftMargin;
                    ct = cParams.topMargin;
                    break;

                case 1:
                    cl = width - cParams.rightMargin - cWidth;
                    ct = cParams.topMargin;
                    break;

                case 2:
                    cl = cParams.leftMargin;
                    ct = height - cParams.bottomMargin - cWidth;
                    break;

                case 3:
                    cl = width - cParams.rightMargin - cWidth;
                    ct = height - cParams.bottomMargin - cHeight;
                    break;
            }
            int cr = cl + cWidth;
            int cb = ct + cHeight;
            cView.layout(cl, ct, cr, cb);
        }
    }
}
