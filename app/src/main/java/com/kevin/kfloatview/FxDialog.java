package com.kevin.kfloatview;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;


/**
 * @author Kevin  2020/9/24
 * 自定义dialog
 */

public class FxDialog extends AppCompatDialog {


    private TextView mTvTitle;  //显示的标题
    private TextView mTvContent;//显示的消息
    private Button mBtnNegative, mBtnPositive;//确认和取消按钮

    private String content;
    private String title;
    private String positive, negative;
    private boolean negativeVisibility = true;

    public FxDialog(Context context) {
        super(context, R.style.CustomDialog);
    }


    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_base);
        setCanceledOnTouchOutside(false);//按空白处不能取消动画
        initView();   //初始化界面控件
        refreshView();//初始化界面数据
        initEvent();  //初始化界面控件的事件


    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        mBtnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener != null) {
                    onClickBottomListener.onPositiveClick();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        mBtnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener != null) {
                    onClickBottomListener.onNegativeClick();
                }
            }
        });
    }

    /**
     * 刷新界面控件
     */
    private void refreshView() {
        //如果用户自定了title和message
        if (!TextUtils.isEmpty(title)) {
            mTvTitle.setText(title);
            mTvTitle.setVisibility(View.VISIBLE);
        } else {
            mTvTitle.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(content)) {
            mTvContent.setText(content);
        }
        //如果设置按钮的文字
        if (!TextUtils.isEmpty(positive)) {
            mBtnPositive.setText(positive);
        } else {
            mBtnPositive.setText("确定");
        }
        if (!TextUtils.isEmpty(negative)) {
            mBtnNegative.setText(negative);
        } else {
            mBtnNegative.setText("取消");
        }
        if (negativeVisibility)
            mBtnNegative.setVisibility(View.VISIBLE);
        else
            mBtnNegative.setVisibility(View.INVISIBLE);

    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        mBtnNegative = findViewById(R.id.fx_dialog_negative);
        mBtnPositive = findViewById(R.id.fx_dialog_positive);
        mTvTitle = findViewById(R.id.fx_dialog_title);
        mTvContent = findViewById(R.id.fx_dialog_content);
    }

    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;

    public FxDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }

    public interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        void onPositiveClick();

        /**
         * 点击取消按钮事件
         */
        void onNegativeClick();
    }

    public String getContent() {
        return this.content;
    }

    public FxDialog setContent(String content) {
        if (mTvContent != null)
            mTvContent.setText(content);
        this.content = content;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public FxDialog setTitle(String title) {
        if (mTvTitle != null)
            mTvTitle.setText(title);
        this.title = title;
        return this;
    }

    public String getPositive() {
        return positive;
    }

    public FxDialog setPositive(String positive) {
        if (mBtnPositive != null)
            mBtnPositive.setText(positive);
        this.positive = positive;
        return this;
    }

    public FxDialog setNegativeVisibility(boolean visibility) {
        if (mBtnNegative != null)
            mBtnNegative.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
        this.negativeVisibility = visibility;
        return this;
    }

    public String getNegative() {
        return negative;
    }

    public FxDialog setNegative(String negative) {
        if (mBtnNegative != null)
            mBtnNegative.setText(negative);
        this.negative = negative;
        return this;
    }

    /**
     * 设置是否可以通过返回键取消对话框
     */
    public FxDialog setBackCancelable(boolean flag) {
        this.setCancelable(flag);
        return this;

    }


    @SuppressLint("StaticFieldLeak")
    public static FxDialog mBaseDialog;
    public static final String TITLE_EXIT_LOGIN = "退出登录";
    public static final String TITLE_LOGIN_STATUS = "登录状态";
    public static final String TITLE_CHECK_UPDATE = "检查更新";

    public static final String CONTENT_LOGIN_INVALID = "登录失效，请重新登录";
    public static final String CONTENT_NOT_LOGIN = "当前未登录，前往登录页面?";
    public static final String CONTENT_EXIT_LOGIN = "确认退出登录?";
    public static final String CONTENT_CHECKED_NEW_VERSION = "检查到新版本，现在更新?";
    public static final String CONTENT_IS_LATEST_VERSION = "当前已经是最新版本";

    public static void initLoginInvalid(final Activity activity) {
        initLoginStatus(activity, TITLE_LOGIN_STATUS, CONTENT_LOGIN_INVALID);
    }

    public static void initNotLogin(final Activity activity) {
        initLoginStatus(activity, TITLE_LOGIN_STATUS, CONTENT_NOT_LOGIN);
    }

    public static void initFxExitDialog(final Activity activity) {
        initLoginStatus(activity, TITLE_EXIT_LOGIN, CONTENT_EXIT_LOGIN);
    }

    public static void initLoginStatus(final Activity activity, @NonNull String title, final String content) {
        initLoginStatus(activity, title, content, true);
    }

    public static void initLoginStatus(final Activity activity, @NonNull String title, final String content, boolean negativeVisibility) {
        if (activity.isDestroyed())
            return;

        if (mBaseDialog == null)
            mBaseDialog = new FxDialog(activity);

        mBaseDialog.setTitle(title)
                .setContent(content)
                .setNegativeVisibility(negativeVisibility)
                .setOnClickBottomListener(new OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        mBaseDialog.dismiss();
                        //HomeActivity.clearDataAndToLoginActivity(activity);
                    }

                    @Override
                    public void onNegativeClick() {
                        mBaseDialog.dismiss();
                    }
                }).show();
    }


}
