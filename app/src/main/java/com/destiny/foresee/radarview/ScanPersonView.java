package com.destiny.foresee.radarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 仿探探雷达发现点击波纹
 */
public class ScanPersonView extends View {

    //默认的主题颜色
    private int DEFAULT_COLOR = Color.parseColor("#91D7F4");
    // 圆圈和交叉线的颜色
    private int mCircleColor = DEFAULT_COLOR;
    //圆圈的数量 不能小于1
    private int mCircleNum = 1;
    //扫描的颜色 RadarView会对这个颜色做渐变透明处理
    private int mSweepColor = DEFAULT_COLOR;
    //扫描的转速，表示几秒转一圈
    private float mSpeed = 3.0f;

    private float mDegrees; //扫描时的扫描旋转角度。
    private boolean isScanning = false;//是否扫描
    private boolean isRipple = false;//是否开启波纹

    private Paint mCirclePaint;// 圆的画笔
    private Paint mSweepPaint; //扫描效果的画笔
    private Paint bgPaint;
    Rect src = new Rect();
    private int min;
    private Bitmap bitmap;
    private int radius = 0;
    private List<Integer> proViews = new ArrayList<>();

    public ScanPersonView(Context context) {
        super(context);
        init();
    }

    public ScanPersonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);
        init();
    }

    public ScanPersonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(context, attrs);
        init();
    }

    /**
     * 获取自定义属性值
     *
     * @param context
     * @param attrs
     */
    private void getAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanPersonView);
            mCircleColor = mTypedArray.getColor(R.styleable.ScanPersonView_scanCircleColor, DEFAULT_COLOR);
            mCircleNum = mTypedArray.getInt(R.styleable.ScanPersonView_scabCircleNum, mCircleNum);
            if (mCircleNum < 1) {
                mCircleNum = 1;
            }
            mSweepColor = mTypedArray.getColor(R.styleable.ScanPersonView_scanSweepColor, DEFAULT_COLOR);
            mSpeed = mTypedArray.getFloat(R.styleable.ScanPersonView_scanSpeed, mSpeed);
            if (mSpeed <= 0) {
                mSpeed = 3;
            }
            bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.map_bg);
            mTypedArray.recycle();
        }
    }


    /**
     * 初始化
     */
    private void init() {
        // 初始化画笔
        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStrokeWidth(1);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setAntiAlias(true);

        mSweepPaint = new Paint();
        mSweepPaint.setAntiAlias(true);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置宽高,默认330dp
        int defaultSize = dp2px(getContext(), 330);
        int w = measureWidth(widthMeasureSpec, defaultSize);
        int h = measureHeight(heightMeasureSpec, defaultSize);
        min = Math.min(w, h);
        setMeasuredDimension(min, min);
    }

    /**
     * 测量宽
     *
     * @param measureSpec
     * @param defaultSize
     * @return
     */
    private int measureWidth(int measureSpec, int defaultSize) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumWidth());
        return result;
    }

    /**
     * 测量高
     *
     * @param measureSpec
     * @param defaultSize
     * @return
     */
    private int measureHeight(int measureSpec, int defaultSize) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumHeight());
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        src.top = 0;
        src.left = 0;
        src.right = w;
        src.bottom = h;
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
//      view w h scale bitmap
        if (min < bitmap.getWidth()) {
            bitmap = getNewBitmap(bitmap, min, min);
        }
        Matrix matrix = new Matrix();
        matrix.postTranslate(-bitmap.getWidth() * 1.0f / 2, -bitmap.getHeight() * 1.0f / 2);
        matrix.postTranslate(src.width() * 1.0f / 2, src.height() * 1.0f / 2);
        canvas.drawBitmap(bitmap, matrix, bgPaint);

        //画最外圆
        drawCircle(canvas, src.centerX(), src.centerY(), bitmap.getWidth() / 2);

        //画水波纹
        for (int i = 0; i < proViews.size(); i++) {
            Log.e("mDegrees", proViews.size() + "");
            int radius = proViews.get(i);
            if (isRipple && bitmap.getWidth() / 2 > radius) {
                radius++;
                drawCircle(canvas, src.centerX(), src.centerY(), radius);
                proViews.set(i, radius);
                invalidate();
            } else {
                proViews.remove(i);
            }
        }


        //正在扫描
        if (isScanning) {
            drawSweep(canvas, src.centerX(), src.centerY(), bitmap.getWidth() / 2);
            //计算雷达扫描的旋转角度
            mDegrees = (mDegrees + (360 / mSpeed / 60)) % 360;
            //触发View重新绘制，通过不断的绘制View的扫描动画效果
            invalidate();
        }
    }


    /**
     * 画扫描效果
     */
    private void drawSweep(Canvas canvas, int cx, int cy, int radius) {
        //扇形的透明的渐变效果
        SweepGradient sweepGradient = new SweepGradient(cx, cy,
                new int[]{Color.TRANSPARENT, changeAlpha(mSweepColor, 0), changeAlpha(mSweepColor, 168),
                        changeAlpha(mSweepColor, 255), changeAlpha(mSweepColor, 255)
                }, new float[]{0.0f, 0.5f, 1f, 1.6f, 2.2f});
        mSweepPaint.setShader(sweepGradient);
        //先旋转画布，再绘制扫描的颜色渲染，实现扫描时的旋转效果。
        canvas.rotate(-90 + mDegrees, cx, cy);
        canvas.drawCircle(cx, cy, radius, mSweepPaint);
    }


    /**
     * 画圆
     */
    private void drawCircle(Canvas canvas, int cx, int cy, int radius) {
        //画mCircleNum个半径不等的圆圈。
        for (int i = 0; i < mCircleNum; i++) {
            canvas.drawCircle(cx, cy, radius - (radius / mCircleNum * i), mCirclePaint);
        }
    }


    /**
     * dp转px
     */
    private static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }


    /**
     * a
     * 改变颜色的透明度
     *
     * @param color
     * @param alpha
     * @return
     */
    private static int changeAlpha(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }


    /**
     * 开始扫描
     */
    public void start() {
        if (!isScanning) {
            isScanning = true;
            invalidate();
        }

    }

    /**
     * 停止扫描
     */
    public void stop() {
        if (isScanning) {
            isScanning = false;
            mDegrees = 0.0f;
        }
    }

    /**
     * 开始水波纹
     */
    public void startRipple() {
        isRipple = true;
        proViews.add(129);
        invalidate();
    }


    public void stopRipple() {
        if (isRipple) {
            isRipple = false;
        }
    }


    public Bitmap getNewBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        // 获得图片的宽高.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBitmap;
    }
}
