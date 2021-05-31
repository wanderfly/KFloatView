package com.kevin.kfloatview;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;


/**
 * @author Kevin  2020/12/10
 */
public final class DisplayUtil {
    private static final boolean DEBUG = true;
    private static final String TAG = "DisplayUtil";

    private DisplayUtil() {
        //no instance
    }

    /**
     * 获取状态栏高度
     *
     * @param context 上下文
     * @return 状态栏高度 单位:px
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        if (DEBUG) Log.e(TAG, "getStatusBarHeight: 状态栏高度为:" + height);
        return height;
    }

    /**
     * dip和px转换
     *
     * @param context 上下文
     * @param dpValue dp值
     * @return dp对应的像素值
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * dip转px
     *
     * @see #dip2px(Context, float)
     * @see TypedValue#applyDimension(int, float, DisplayMetrics) 方法里面有其它类型的转换
     */
    public static float dp2px(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
