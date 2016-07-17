package com.ttwishing.photodialog.library;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 重写DialogFragment类，使实现{@link BaseDialogForFragment},接收back和touch事件
 */
public abstract class BaseDialogFragment extends DialogFragment {

    public static <T extends BaseDialogFragment> T showDialog(Activity activity, Class<T> cls, String tag, Bundle args) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        T dialogFragment = (T) fragmentManager.findFragmentByTag(tag);
        if (dialogFragment == null) {
            dialogFragment = newDialogFragment(cls, args);
            if (dialogFragment == null)
                throw new IllegalStateException("Couldn't show fragment " + cls.getCanonicalName());
            dialogFragment.show(fragmentManager, tag);
        }
        return dialogFragment;
    }

    public static <T extends BaseDialogFragment> T newDialogFragment(Class<T> cls, Bundle args) {
        T dialogFragment = null;
        try {
            dialogFragment = cls.newInstance();
            dialogFragment.setArguments(args);
        } catch (Throwable t) {
            //Couldn't create instance for fragment
        }
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_DialogTransparent_NoAnim);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getContentViewResId(), container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BaseDialogForFragment(getActivity(), getTheme());
    }

    @Override
    public void dismiss() {
        dismissAllowingStateLoss();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    public View findViewById(int resId) {
        if (getView() == null)
            return null;
        return getView().findViewById(resId);
    }

    protected abstract int getContentViewResId();

    public boolean handleBackPressed() {
        return false;
    }

    class BaseDialogForFragment extends Dialog {

        public BaseDialogForFragment(Context context, int themeResId) {
            super(context, themeResId);
        }

        @Override
        public void onBackPressed() {
            if (!handleBackPressed())
                super.onBackPressed();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (BaseDialogFragment.this.onTouchEvent(event) || super.onTouchEvent(event)) {
                return true;
            }
            return false;
        }
    }
}
