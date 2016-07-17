package com.ttwishing.photodialog.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


/**
 * Created by kurt on 2/15/16.
 */
public class PhotoDialogFragment extends ScaleAnimDialogFragment {

    protected static final int OVERLAY_HIDE_DELAY = 5000;
    protected static final int OVERLAY_SHOW_DELAY = 500;

    protected ImageView photoImageView;
    protected View upButtonView;
    protected View actionBarLayout;
    protected View bottomContainerLayout;
    protected Drawable backgroundDrawable;

    protected boolean showOverlay = false;
    protected AnimatorListenerAdapter hideAnimatorListener;
    protected AnimatorListenerAdapter showAnimatorListener;

    protected final Runnable overlayVisibilityRunnable = new Runnable() {
        @Override
        public void run() {
            setOverlayVisibility(false);
        }
    };

    public static void showDialog(final Activity activity, final Bundle args, final View view, BitmapDrawable bitmapDrawable) {
        PhotoDialogFragment dialogFragment = BaseDialogFragment.showDialog(activity, PhotoDialogFragment.class, "PHOTO", args);
        dialogFragment.setBitmap(bitmapDrawable);
        dialogFragment.setActivityAndView(activity, view);
    }


    @Override
    public void onResume() {
        super.onResume();
        handler.removeCallbacks(this.overlayVisibilityRunnable);
        handler.postDelayed(this.overlayVisibilityRunnable, OVERLAY_HIDE_DELAY);
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(this.overlayVisibilityRunnable);
        super.onPause();
    }

    @Override
    public int getTheme() {
        return R.style.DialogTransparent_Dialog;
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.photo_activity;
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);

        this.photoImageView = (ImageView) findViewById(R.id.res_photo);
        this.upButtonView = findViewById(R.id.up_button);
        this.actionBarLayout = findViewById(R.id.action_bar);
        this.bottomContainerLayout = findViewById(R.id.bottom_container);
        this.photoImageView.setImageBitmap(getBitmap());
        this.photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOverlayVisibility(true);
            }
        });
        init(view);
    }


    @Override
    protected Drawable getBackgroundDrawable() {
        if (this.backgroundDrawable == null) {
            this.backgroundDrawable = new ColorDrawable(Color.BLACK);
        }
        return this.backgroundDrawable;
    }

    @Override
    protected int getImageViewResId() {
        return R.id.res_photo;
    }


    @Override
    protected void handleBack() {
        handler.removeCallbacks(this.overlayVisibilityRunnable);
        this.actionBarLayout.setVisibility(View.GONE);
        this.bottomContainerLayout.setVisibility(View.GONE);
    }

    @Override
    protected void enterAnimFinish() {
        super.enterAnimFinish();
        setSystemUiVisibility(257);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setOverlayVisibility(true);
            }
        }, OVERLAY_SHOW_DELAY);
    }


    private void setOverlayVisibility(boolean visibility) {
        if (this.showOverlay == visibility) {
            return;
        }
        this.showOverlay = visibility;
        int systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (!visibility) {
            systemUiVisibility = 257;
        }
        setSystemUiVisibility(systemUiVisibility);
        showOverlay();
        handler.removeCallbacks(this.overlayVisibilityRunnable);
        if (visibility) {
            handler.postDelayed(this.overlayVisibilityRunnable, OVERLAY_HIDE_DELAY);
        }
    }

    private void showOverlay() {
        if (this.showOverlay) {
            this.actionBarLayout.setVisibility(View.VISIBLE);
            this.bottomContainerLayout.setVisibility(View.VISIBLE);
            this.actionBarLayout.setTranslationY(-this.actionBarLayout.getHeight());
            this.actionBarLayout.animate().translationY(0.0F).setListener(this.showAnimatorListener);
            this.bottomContainerLayout.setTranslationY(this.bottomContainerLayout.getHeight());
            this.bottomContainerLayout.animate().translationY(0.0F).setListener(this.showAnimatorListener);
        } else {
            this.actionBarLayout.animate().translationY(-this.actionBarLayout.getHeight()).setListener(this.hideAnimatorListener);
            this.bottomContainerLayout.animate().translationY(this.bottomContainerLayout.getHeight()).setListener(this.hideAnimatorListener);
        }
    }

    private void setSystemUiVisibility(int visibility) {
        View view = getView();
        if (view != null) {
            view.setSystemUiVisibility(visibility);
        }
    }

    private void init(View view) {
        view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                //TODO
            }
        });
        this.hideAnimatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                actionBarLayout.setVisibility(View.INVISIBLE);
                bottomContainerLayout.setVisibility(View.INVISIBLE);
            }
        };
        this.showAnimatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                actionBarLayout.setTranslationY(0.0F);
                bottomContainerLayout.setTranslationY(0.0F);
            }
        };
    }
}
