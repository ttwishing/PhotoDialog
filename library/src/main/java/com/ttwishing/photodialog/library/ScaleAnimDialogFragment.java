package com.ttwishing.photodialog.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;


import com.ttwishing.photodialog.library.util.ViewUtils;

import java.lang.ref.WeakReference;

/**
 * Created by kurt on 2/1/16.
 */
public abstract class ScaleAnimDialogFragment extends BaseDialogFragment {

    protected Handler handler = new Handler();

    private WeakReference<View> viewRef;

    private Bitmap bitmap;
    private Drawable bitmapDrawable;

    private float bitmapRatio = 1.0F;
    private Rect oriBitmapBounds = new Rect();

    private Rect animatingViewRect = new Rect();
    private float animatingViewRatio = 1.0F;
    private final Rect dstBitmapBounds = new Rect();

    private ContainerLayout containerLayout;
    private View animatingView;//要执行bounds动画的

    private boolean animConfigured = false;
    private boolean isAnimating;//是否在执行动画

    //进入动画参数
    private long enterAnimDDelay = 50L;
    private int enterAnimDuration;
    private Interpolator enterAnimInterpolator;

    //退出动画参数
    private long outAnimDelay = 100L;
    private int outAnimDuration;
    private Interpolator outAnimInterpolator;


    private final TypeEvaluator<Rect> boundsEvaluator = new TypeEvaluator<Rect>() {
        @Override
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(
                    getValue(startValue.left, endValue.left, fraction),
                    getValue(startValue.top, endValue.top, fraction),
                    getValue(startValue.right, endValue.right, fraction),
                    getValue(startValue.bottom, endValue.bottom, fraction)
            );
        }

        public int getValue(int startValue, int endValue, float fraction) {
            return (int) (startValue + fraction * (endValue - startValue));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.containerLayout = new ContainerLayout(getActivity());
        this.containerLayout.addView(super.onCreateView(inflater, container, savedInstanceState));
        return this.containerLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handleViewCreated();
        this.enterAnimDuration = getEnterAnimDuration();
        this.outAnimDuration = getOutAnimDuration();
        this.enterAnimInterpolator = getEnterAnimInterpolator();
        this.outAnimInterpolator = getOutAnimInterpolator();

        this.animatingView = findViewById(getImageViewResId());
        if (this.animatingView == null) {
            throw new IllegalStateException("animatingView must be valid!");
        }

        if (this.oriBitmapBounds != null && this.bitmapDrawable != null && savedInstanceState == null) {//cond_1
            this.animatingView.setVisibility(View.INVISIBLE);
            this.animatingView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    animatingView.getViewTreeObserver().removeOnPreDrawListener(this);

                    //初始化和执行动画
                    animatingViewRect = ViewUtils.getViewBounds(animatingView, false, false, animatingViewRect, null);
                    animatingViewRatio = (float) animatingViewRect.width() / animatingViewRect.height();

                    configureAnim();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animEnter();
                        }
                    }, enterAnimDDelay);

                    return false;
                }
            });
            initView(view, savedInstanceState);
        } else {
            initView(view, savedInstanceState);
            animEnter();
        }
    }

    public void initView(View view, Bundle savedInstanceState) {
    }

    public void setActivityAndView(Activity activity, View view) {
        if (activity != null) {
            this.viewRef = new WeakReference(view);
            this.oriBitmapBounds = getOriBounds(view);
        }
    }

    public void setBitmap(BitmapDrawable bitmapDrawable) {
        if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
            this.bitmap = bitmapDrawable.getBitmap();
            this.bitmapRatio = (float) bitmap.getWidth() / bitmap.getHeight();
            this.bitmapDrawable = bitmapDrawable;
        }
    }

    protected Bitmap getBitmap() {
        return this.bitmap;
    }

    /**
     * 配置动画的一些参数
     */
    private void configureAnim() {
        //按原图的比例显示
        if (this.bitmapRatio >= this.animatingViewRatio) {
            this.dstBitmapBounds.set(
                    this.animatingViewRect.left,
                    this.animatingViewRect.top,
                    this.animatingViewRect.left + this.animatingViewRect.width(),
                    this.animatingViewRect.top + (int) (this.animatingViewRect.width() / this.bitmapRatio)
            );
        } else {
            this.dstBitmapBounds.set(
                    this.animatingViewRect.left,
                    this.animatingViewRect.top,
                    this.animatingViewRect.left + (int) (this.animatingViewRect.height() * this.bitmapRatio),
                    this.animatingViewRect.top + this.animatingViewRect.height()
            );
        }
        int dx = (this.animatingViewRect.width() - this.dstBitmapBounds.width()) / 2;
        int dy = (this.animatingViewRect.height() - this.dstBitmapBounds.height()) / 2;

        //显示比例变化,需要偏移
        this.dstBitmapBounds.offset(dx, dy);
        int offset = (int) (1.0D / this.dstBitmapBounds.width() * this.oriBitmapBounds.width() * this.dstBitmapBounds.height()) - this.oriBitmapBounds.height();

        //顶部和底部再偏移
        this.oriBitmapBounds.top -= offset / 2;
        this.oriBitmapBounds.bottom += offset / 2;
        this.animConfigured = true;
    }


    /**
     * 显示动画
     */
    private void animEnter() {
        Drawable backgroundDrawable = getBackgroundDrawable();
        if (backgroundDrawable != null) {
            getView().setBackgroundDrawable(backgroundDrawable);
        }
        getView().setLayerType(2, null);
        if (this.animConfigured) {
            getView().setAlpha(0.0F);

            //初始位置
            this.bitmapDrawable.setBounds(this.oriBitmapBounds);
            this.containerLayout.invalidate();

            this.isAnimating = true;

            //布局透明度
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(getView(), "alpha", new float[]{1.0F});

            Object[] values = new Object[]{
                    this.oriBitmapBounds, this.dstBitmapBounds
            };

            //更新变化
            ObjectAnimator boundsAnimator = ObjectAnimator.ofObject(this.bitmapDrawable, "bounds", this.boundsEvaluator, values);
            boundsAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    containerLayout.invalidate();
                }
            });

            boundsAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (getView() == null) {
                        return;
                    }
                    isAnimating = false;
                    animatingView.setVisibility(View.VISIBLE);
                    containerLayout.invalidate();
                    getView().setLayerType(0, null);
                    enterAnimFinish();
                }
            });
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(new Animator[]{alphaAnimator, boundsAnimator});
            animatorSet.setDuration(this.enterAnimDuration);
            animatorSet.setInterpolator(this.enterAnimInterpolator);
            animatorSet.start();
        } else {
            getView().setAlpha(0.0F);
            getView().animate().alpha(1.0F).setDuration(this.enterAnimDuration).setInterpolator(this.enterAnimInterpolator).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (getView() == null) {
                        return;
                    }

                    getView().setLayerType(0, null);
                    enterAnimFinish();
                }
            });
        }
    }

    protected void enterAnimFinish() {
    }

    /**
     * 退出动画
     */
    private void animOut() {
        getView().setLayerType(2, null);

        if (this.animConfigured && isActivated()) {
            this.isAnimating = true;
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(getView(), "alpha", new float[]{0.0F});
            alphaAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isAnimating = false;
                            outAnimFinish();
                            dismissAllowingStateLoss();
                        }
                    }, outAnimDelay);
                }
            });

            //bounds动画
            Object[] values = new Object[]{this.dstBitmapBounds, this.oriBitmapBounds};
            ObjectAnimator boundsAnimator = ObjectAnimator.ofObject(this.bitmapDrawable, "bounds", boundsEvaluator, values);
            boundsAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    containerLayout.invalidate();
                }
            });
            boundsAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    containerLayout.invalidate();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animatingView.setVisibility(View.INVISIBLE);
                }
            });
            AnimatorSet animatorSet = new AnimatorSet();

            //依次进行 先移动,后显示全局
            animatorSet.playSequentially(new Animator[]{boundsAnimator, alphaAnimator});
            animatorSet.setDuration(outAnimDuration);
            animatorSet.setInterpolator(this.outAnimInterpolator);
            animatorSet.start();
        } else {
            getView().animate().alpha(0.0F).setDuration(this.outAnimDuration).setInterpolator(this.outAnimInterpolator).setListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    outAnimFinish();
                    dismissAllowingStateLoss();
                }
            });
        }
    }

    protected void outAnimFinish() {
    }

    protected abstract Drawable getBackgroundDrawable();

    /**
     * view在原布局结构显示的位置
     *
     * @param view
     * @return
     */
    public static Rect getOriBounds(View view) {
        Rect rect = ViewUtils.getViewBounds(view, true, false, null, null);
        //去除padding
        rect.left += view.getPaddingLeft();
        rect.right -= view.getPaddingRight();
        rect.top += view.getPaddingTop();
        rect.bottom -= view.getPaddingBottom();

        return rect;
    }

    protected abstract int getImageViewResId();

    protected void handleViewCreated() {
    }

    protected int getEnterAnimDuration() {
        return getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    protected int getOutAnimDuration() {
        return getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    protected Interpolator getEnterAnimInterpolator() {
        return new LinearInterpolator();
    }

    protected Interpolator getOutAnimInterpolator() {
        return new LinearInterpolator();
    }

    @Override
    public final boolean handleBackPressed() {
        handleBack();
        animOut();
        return true;
    }

    /**
     * 后续回退操作
     */
    protected void handleBack() {

    }

    /**
     * view未回收
     *
     * @return
     */
    protected boolean isActivated() {
        if (this.viewRef != null && this.viewRef.get() != null) {
            return true;
        }
        return false;
    }

    public class ContainerLayout extends FrameLayout {
        public ContainerLayout(Context context) {
            super(context);
            setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            if (bitmapDrawable != null && isAnimating) {
                //动画中不绘制子view
                bitmapDrawable.draw(canvas);
            } else {
                super.dispatchDraw(canvas);
            }
        }

        @Override
        protected void measureChild(View child, int widthMeasureSpec, int heightMeasureSpec) {
            //动画中不测量子view
            if (!isAnimating) {
                super.measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }
}
