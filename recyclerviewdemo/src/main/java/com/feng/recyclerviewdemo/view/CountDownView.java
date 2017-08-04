package com.feng.recyclerviewdemo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.feng.recyclerviewdemo.R;

/**
 * Created by OneDay on 2017/8/1.
 *
 * 自定义倒计时View
 *
 */

public class CountDownView extends View{

    private static final String TAG = "CountDownView";

    private int layout_height = 0;
    private int layout_width = 0;
    private int fullRadius = 100;
    private int circleRadius = 80;
    private int barLength = 60;
    private int barWidth = 20;
    private int rimWidth = 20;
    private int textSize = 20;
    private float contourSize = 0;

    //Padding (with defaults)
    private int paddingTop = 5;
    private int paddingBottom = 5;
    private int paddingLeft = 5;
    private int paddingRight = 5;

    //Rectangles
    private RectF rectBounds = new RectF();
    private RectF circleBounds = new RectF();
    private RectF circleOuterContour = new RectF();
    private RectF circleInnerContour = new RectF();

    //Paints
    private Paint barPaint = new Paint();
    private Paint circlePaint = new Paint();
    private Paint rimPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint contourPaint = new Paint();

    //Colors (with defaults)
    private int barColor = 0xAA000000;
    private int contourColor = 0xAA000000;
    private int circleColor = 0x00000000;
    private int rimColor = 0xAADDDDDD;
    private int textColor = 0xFF000000;

    //Animation
    //The amount of pixels to move the bar by on each draw
    private int spinSpeed = 2;
    //The number of milliseconds to wait inbetween each draw
    private int delayMillis = 0;

    int progress = 0;
    boolean isSpinning = false;

    //Other
    private String text = "";
    private String[] splitText = {};

    private Handler spinHandler = new Handler() {
        /**
         * This is the code that will increment the progress variable
         * and so spin the wheel
         */
        @Override
        public void handleMessage(Message msg) {
            invalidate();
            if (isSpinning) {
                progress += spinSpeed;
                if (progress > 360) {
                    progress = 0;
                }
                spinHandler.sendEmptyMessageDelayed(0, delayMillis);
            }
            //super.handleMessage(msg);
        }
    };

    private long millsInFuture = 5000;
    private long countDownInterval = 100;
    private long count = millsInFuture / countDownInterval;

    private CountDownTimer timer = new CountDownTimer(millsInFuture, countDownInterval) {
        @Override
        public void onTick(long millisUntilFinished) {
            long curCount = millisUntilFinished / countDownInterval;
        }

        @Override
        public void onFinish() {

        }
    };


    public CountDownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context.obtainStyledAttributes(attrs, R.styleable.CountDownWheel));
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttributes(context.obtainStyledAttributes(attrs, R.styleable.CountDownWheel));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        parseAttributes(context.obtainStyledAttributes(attrs, R.styleable.CountDownWheel));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure: before supper");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "onMeasure: after supper");
        int size = 0;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int widthInner = width - getPaddingLeft() - getPaddingRight();
        int heightInner = height - getPaddingTop() - getPaddingBottom();

        if(widthInner > heightInner){
            size = heightInner;
        }else{
            size = widthInner;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "onSizeChanged: [w=" + w + ", h=" + h + "], [oldw=" + oldw + ", oldh=" + oldh + "]");

        layout_width = w;
        layout_height = h;

        setupBounds();
        setupPaints();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Draw circle
        canvas.drawArc(circleBounds, 0, 360, false, circlePaint);
        //Draw rim
        canvas.drawArc(circleBounds, 0, 360, false, rimPaint);

        canvas.drawArc(circleInnerContour, 0, 360, false, contourPaint);
        canvas.drawArc(circleOuterContour, 0, 360, false, contourPaint);

        if(isSpinning){
            canvas.drawArc(circleBounds, progress - 90, barLength, false, barPaint);
        }else{
            canvas.drawArc(circleBounds, -90, progress, false, barPaint);
        }

        //Draw the text (attempts to center it horizontally and vertically)
        float textHeight = textPaint.descent() - textPaint.ascent();
        float verticalTextOffset = (textHeight / 2) - textPaint.descent();

        for (String s : splitText) {
            float horizontalTextOffset = textPaint.measureText(s) / 2;
            canvas.drawText(s, this.getWidth() / 2 - horizontalTextOffset,
                    this.getHeight() / 2 + verticalTextOffset, textPaint);
        }
    }

    /**
     * 设置边界
     */
    private void setupBounds() {
        int minValue = Math.min(layout_width, layout_height);

        int xOffset = layout_width - minValue;
        int yOffset = layout_height - minValue;

        paddingLeft = getPaddingLeft() + (xOffset / 2);
        paddingTop = getPaddingTop() + (yOffset / 2);
        paddingRight = getPaddingRight() + (xOffset / 2);
        paddingBottom = getPaddingBottom() + (yOffset / 2);

        int width = getWidth(); //this.getLayoutParams().width;
        int height = getHeight(); //this.getLayoutParams().height;

        rectBounds.set(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom);
        circleBounds.set(rectBounds.left + barWidth, rectBounds.top + barWidth,
                rectBounds.right - barWidth, rectBounds.bottom - barWidth);

        float halfRim = rimWidth / 2.0f;
        float halfContour = contourSize / 2.0f;
        circleInnerContour.set(circleBounds.left + halfRim + halfContour,
                circleBounds.top + halfRim + halfContour,
                circleBounds.right - halfRim - halfContour,
                circleBounds.bottom - halfRim - halfContour);

        circleOuterContour.set(circleBounds.left - halfRim - halfContour,
                circleBounds.top - halfRim - halfContour,
                circleBounds.right + halfRim + halfContour,
                circleBounds.bottom + halfRim + halfContour);

        fullRadius = (width - paddingRight - barWidth) / 2;
        circleRadius = (fullRadius - barWidth) + 1;
    }

    /**
     * 设置画笔
     */
    private void setupPaints() {
        circlePaint.setColor(circleColor);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.FILL);

        barPaint.setColor(barColor);
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Paint.Style.STROKE);
        barPaint.setStrokeWidth(barWidth);

        rimPaint.setColor(rimColor);
        rimPaint.setAntiAlias(true);
        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(rimWidth);

        contourPaint.setColor(contourColor);
        contourPaint.setAntiAlias(true);
        contourPaint.setStyle(Paint.Style.STROKE);
        contourPaint.setStrokeWidth(contourSize);

        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);
    }

    /**
     * 解析视图的属性
     * @param a
     */
    private void parseAttributes(TypedArray a){
        barWidth = (int) a.getDimension(R.styleable.CountDownWheel_barWidth,
                barWidth);

        rimWidth = (int) a.getDimension(R.styleable.CountDownWheel_rimWidth,
                rimWidth);

        spinSpeed = (int) a.getDimension(R.styleable.CountDownWheel_spinSpeed,
                spinSpeed);

        delayMillis = a.getInteger(R.styleable.CountDownWheel_delayMillis,
                delayMillis);
        if (delayMillis < 0) {
            delayMillis = 0;
        }

        barColor = a.getColor(R.styleable.CountDownWheel_barColor, barColor);

        barLength = (int) a.getDimension(R.styleable.CountDownWheel_barLength,
                barLength);

        textSize = (int) a.getDimension(R.styleable.CountDownWheel_textSize,
                textSize);

        textColor = (int) a.getColor(R.styleable.CountDownWheel_textColor,
                textColor);

        //if the text is empty , so ignore it
        if (a.hasValue(R.styleable.CountDownWheel_text)) {
            setText(a.getString(R.styleable.CountDownWheel_text));
        }

        rimColor = (int) a.getColor(R.styleable.CountDownWheel_rimColor,
                rimColor);

        circleColor = (int) a.getColor(R.styleable.CountDownWheel_circleColor,
                circleColor);

        contourColor = a.getColor(R.styleable.CountDownWheel_contourColor, contourColor);
        contourSize = a.getDimension(R.styleable.CountDownWheel_contourSize, contourSize);

        // Recycle
        a.recycle();
    }

    /**
     * Set the text in the progress bar
     * Doesn't invalidate the view
     *
     * @param text the text to show ('\n' constitutes a new line)
     */
    public void setText(String text) {
        this.text = text;
        splitText = this.text.split("\n");
    }

    /**
     * Turn off spin mode
     */
    public void stopSpinning() {
        isSpinning = false;
        progress = 0;
        spinHandler.removeMessages(0);
    }


    /**
     * Puts the view on spin mode
     */
    public void startSpinning() {
        isSpinning = true;
        spinHandler.sendEmptyMessage(0);
    }

    /**
     * Set the progress to a specific value
     */
    public void setProgress(int i) {
        isSpinning = false;
        progress = i;
        spinHandler.sendEmptyMessage(0);
    }

    public void setMillsInFuture(long millsInFuture) {
        this.millsInFuture = millsInFuture;
    }

    public void setCountDownInterval(long countDownInterval) {
        if(countDownInterval <= 0){
            this.countDownInterval = 1;
        }else{
            this.countDownInterval = countDownInterval;
        }
    }

    public void start(){
        timer.start();
    }

    public void cancel(){
        timer.cancel();
    }

}
