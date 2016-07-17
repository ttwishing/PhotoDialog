package com.ttwishing.photodialog.library.util;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Created by kurt on 11/13/15.
 */
public class ViewUtils {

    private static final int[] contentLocation = new int[2];

    /**
     * 显示屏幕宽度
     * @param context
     * @return
     */
    public static int getDisplayWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 显示屏幕高度
     * @param context
     * @return
     */
    public static int getDisplayHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static Rect getViewBounds(View view, boolean justParent, boolean includeMargin, Rect rect, int[] viewLocation) {
        if (rect == null) {
            rect = new Rect();
        }
        ViewGroup.MarginLayoutParams lp = null;
        if (includeMargin && view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        }
        int[] location = getViewLocation(view, justParent, viewLocation);

        int x = location[0];
        int leftMargin = 0;
        if (lp != null) {
            leftMargin = lp.leftMargin;
        }
        rect.left = x - leftMargin;

        int y = location[1];
        int topMargin = 0;
        if (lp != null) {
            topMargin = lp.topMargin;
        }
        rect.top = y - topMargin;

        int rightMargin = 0;
        if (lp != null) {
            rightMargin = lp.rightMargin;
        }
        rect.right = rect.left + view.getWidth()+rightMargin;

        int bottomMargin = 0;
        if (lp != null) {
            bottomMargin = lp.bottomMargin;
        }
        rect.bottom = rect.bottom + view.getHeight() + bottomMargin;
        return rect;
    }

    /**
     *
     * @param view
     * @param justParent 是否相对与父布局,还是整个布局
     * @param location
     * @return
     */
    public static int[] getViewLocation(View view, boolean justParent, int[] location) {
        if (location == null || location.length < 2) {
            location = new int[2];
        }
        view.getLocationOnScreen(location);
        if (!justParent) {
            //content布局
            View contentView = getContentView(view);
            if (contentView != null) {
                contentView.getLocationOnScreen(contentLocation);
                location[0] -= contentLocation[0];
                location[1] -= contentLocation[1];
            }
        }
        return location;
    }

    /**
     * content_view
     * @param view
     * @return
     */
    public static View getContentView(View view) {
        View contentView = view.getRootView().findViewById(Window.ID_ANDROID_CONTENT);
        if (contentView != null)
            return ((ViewGroup) contentView).getChildAt(0);
        return null;
    }

}
