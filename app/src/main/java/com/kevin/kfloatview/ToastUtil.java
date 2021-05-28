package com.kevin.kfloatview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * @author Kevin  2020/9/15
 */
public final class ToastUtil {
    @SuppressLint("StaticFieldLeak")
    private static Context mCtx;
    //private static TextToSpeech mTTS=null;//朗读文字

    private ToastUtil() {
    }

    /**
     * 该方法只需要在Application中初始化一次
     */
    public static void init(Context context) {
        mCtx = context;
    }

    private static String recentContent = "";
    private static long recentTime;

    public static void show(@NonNull String content, long interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("interval 过滤的时间间隔应该大于0 !!!");
        }
        if (mCtx != null) {
            //Todo 过滤掉相同的内容
            if (!Objects.equals(recentContent, content) || (System.currentTimeMillis() - recentTime) > interval) {
                Toast.makeText(mCtx, content, Toast.LENGTH_SHORT).show();
                recentTime = System.currentTimeMillis();
                recentContent = content;
            }
        }
    }

    public static void show(@NonNull String content) {
        show(content, 3000);
    }

    /**
     * 清空记录的上次Toast内容和时间
     */
    public static void clearCache() {
        recentContent = "";
        recentTime = 0;
    }

    public static void release() {
        if (mCtx != null)
            mCtx = null;
    }
}
