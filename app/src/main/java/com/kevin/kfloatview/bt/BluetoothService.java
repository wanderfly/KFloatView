package com.kevin.kfloatview.bt;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kevin.kfloatview.R;
import com.kevin.kfloatview.bt.adapter.BtSettingDeviceInfo;
import com.kevin.kfloatview.bt.floatview.EnFloatingView;
import com.tools.command.EscCommand;
import com.tools.command.LabelCommand;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

/**
 * @author Kevin  2021/5/28
 */
public class BluetoothService extends Service {
    private static final boolean DEBUG = true;
    private static final String TAG = "BluetoothService";


    public static final String ACTIVITY_EXTRA_NAME = "activity_extra_name";
    private static final String TAG_BT_MIN_LAYOUT = "tag_bt_min_layout";
    private static final String SPACE = "         ";
    private static final String STAR = "* * * * * * * * * * * * * * * * ";
    private static final byte[] code_1 = {27, 69, 0};//取消加粗模式
    private static final byte[] code_2 = {27, 69, 1};//选择加粗模式
    private static final byte[][] codes = new byte[][]{{27, 64}, {27, 77, 0}, {27, 77, 1}, {29, 33, 0}, {29, 33, 17}, {27, 69, 0}, {27, 69, 1}, {27, 123, 0}, {27, 123, 1}, {29, 66, 0}, {29, 66, 1}, {27, 86, 0}, {27, 86, 1}};
    private static final String[] codeNames = {
            "复位打印机",
            "标准ASCII字体", "压缩ASCII字体",
            "字体不放大", "宽高加倍",
            "取消加粗模式", "选择加粗模式",
            "取消倒置打印", "选择倒置打印",
            "取消黑白反显", "选择黑白反显",
            "取消顺时针旋转90°", "选择顺时针选择90°"};
    private final StringBuilder mSBuilder = new StringBuilder();
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mAdapter;
    private BluetoothSocket mBluetoothSocket;  //说明:本身蓝牙是支持同时和多个设备通信的，这里结合实际需求，采用单连接方式
    private BluetoothStateReceiver mBtStateReceiver;

    private BluetoothPmActivity mActivity;
    private FrameLayout mFlContainer;  //悬浮窗 容器
    private TextView mFlTvLinkedState; //悬浮窗 显示打印机连接状态
    private Button mFlBtnBtSetting;    //悬浮窗 跳转到蓝牙设置页面
    private final HashMap<String, View> mViewMaps = new HashMap<>();
    private BtServiceHandler mHandler;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ");
        super.onCreate();
        BluetoothManager bm = (BluetoothManager) getSystemService(Service.BLUETOOTH_SERVICE);
        mAdapter = bm.getAdapter();
        mHandler = new BtServiceHandler(Looper.getMainLooper(), this);
        initBluetoothFilter();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: intent:" + intent);
        return new BluetoothBinder(this);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.e(TAG, "onRebind: intent:" + intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.e(TAG, "onStartCommand: " + intent.getAction());
        //Bundle bundle = intent.getBundleExtra(ACTIVITY_EXTRA_NAME);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: ");
        removeActivityAllViews();
        //mActivity = null;
        //mContainer = null;
        //return super.onUnbind(intent);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelBluetoothFilter();
        Log.d(TAG, "onDestroy: ");
    }

    /**
     * 移除当前activity中通过服务添加的所以view
     */
    private void removeActivityAllViews() {
        if (mActivity != null && mFlContainer != null) {
            Log.e(TAG, "removeActivityAllViews: 移除view");
            for (Map.Entry<String, View> map : mViewMaps.entrySet()) {
                Log.e(TAG, "removeActivityAllViews: key:" + map.getKey() + " value:" + map.getValue());
                mFlContainer.removeView(map.getValue());
                mViewMaps.remove(map.getKey());
            }
        }
    }


    private IBluetoothStateCallBack mIBluetoothStateCallBack;

    public void setBluetoothStateCallback(@Nullable IBluetoothStateCallBack callback) {
        mIBluetoothStateCallBack = callback;
        if (mBtStateReceiver != null)
            mBtStateReceiver.setCallBack(callback);
        sendCurBondedDevs();
    }

    @Nullable
    public Set<BluetoothDevice> getBondedDevs() {
        return mAdapter.getBondedDevices();
    }

    /**
     * 通过接口回调发送当前绑定的设备
     */
    public void sendCurBondedDevs() {
        if (mIBluetoothStateCallBack != null) {
            Set<BluetoothDevice> devs = getBondedDevs();
            if (devs != null) {
                int devsSize = devs.size();
                ArrayList<BtSettingDeviceInfo> deviceInfos = new ArrayList<>();
                if (devsSize > 0) {
                    if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
                        BluetoothDevice connectedDev = mBluetoothSocket.getRemoteDevice();
                        for (BluetoothDevice bondedDev : devs) {
                            if (isSameDev(connectedDev, bondedDev)) //将连接的设备放在集合首位
                                deviceInfos.add(0, new BtSettingDeviceInfo(bondedDev, true));
                            else
                                deviceInfos.add(new BtSettingDeviceInfo(bondedDev, false));
                        }
                    } else {
                        for (BluetoothDevice bondedDev : devs) {
                            deviceInfos.add(new BtSettingDeviceInfo(bondedDev, false));
                        }
                    }
                }
                mIBluetoothStateCallBack.updateBondedDevs(deviceInfos);
            }
        }
    }

    private boolean isSameDev(@NonNull BluetoothDevice devOne,
                              @NonNull BluetoothDevice devTwo) {
        return Objects.equals(devOne.getName(), devTwo.getName());
    }

    public void releaseCurConnect() {
        if (mBluetoothSocket != null) {
            if (mBluetoothSocket.isConnected()) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mBluetoothSocket = null;
        }
        setFlowViewContent(false);
    }

    /**
     * 判断指定设备是否已经连接
     */
    public boolean isConnected(BluetoothDevice device) {
        if (device == null || mBluetoothSocket == null)
            return false;

        return mBluetoothSocket.isConnected()
                && isSameDev(device, mBluetoothSocket.getRemoteDevice());
    }

    public boolean isConnected() {
        if (mBluetoothSocket == null)
            return false;

        return mBluetoothSocket.isConnected();
    }

    private void setFlowViewContent(boolean isConnected) {
        if (mActivity != null && mFlContainer != null) {
            if (isConnected) {
                mFlTvLinkedState.setText("打印机已连接");
                mFlBtnBtSetting.setText("切换设备");
            } else {
                mFlTvLinkedState.setText("打印机未连接");
                mFlBtnBtSetting.setText("连接");
            }
        }
    }


    public void printData(String jsonSting) {

    }

    public void setCurActivity(BluetoothPmActivity activity) {
        mActivity = activity;
        mFlContainer = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

    /**
     * 调用在setCurActivity之后才有效
     *
     * @see #setCurActivity(BluetoothPmActivity)
     */
    public void showBtStateView() {
        removeActivityAllViews();

        if (mActivity != null) {
            Log.e(TAG, "showBtStateView: ");
            if (mFlContainer == null)
                mFlContainer = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

            //EnFloatingView floatingView= new EnFloatingView(mActivity, R.layout.layout_float_view);
            EnFloatingView floatingView = new EnFloatingView(mActivity, R.layout.bluetooth_min);
            floatingView.setAdsorptionEdge(false);
            floatingView.setLayoutParams(getBtMinLayoutParams());
            mFlTvLinkedState = floatingView.findViewById(R.id.tv_bt_min_link_state);
            mFlBtnBtSetting = floatingView.findViewById(R.id.btn_bt_min_link);
            mFlBtnBtSetting.setOnClickListener(v -> {
                if (mActivity != null) {
                    Intent intent = new Intent(mActivity, BluetoothSetActivity.class);
                    mActivity.startActivity(intent);
                }
            });
            ImageView ivClose = floatingView.findViewById(R.id.iv_bt_min_close);
            ivClose.setOnClickListener(v -> removeActivityAllViews());
            Log.e(TAG, "showBtStateView: parent:" + floatingView.getParent());
            mFlContainer.addView(floatingView);
            mViewMaps.put(TAG_BT_MIN_LAYOUT, floatingView);
            if (isConnected())
                setFlowViewContent(true);
        }
    }

    private FrameLayout.LayoutParams getBtMinLayoutParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.setMargins(0, params.topMargin, params.rightMargin, 100);
        return params;
    }

    public void releaseCurActivity() {
        Log.e(TAG, "releaseCurActivity: mActivity:" + mActivity);
        if (mActivity != null) {
            mActivity = null;
            mFlContainer = null;
        }

    }


    public boolean isBluetoothEnable() {
        return mAdapter.isEnabled();
    }

    public boolean closeBluetooth() {
        return mAdapter.disable();
    }

    /**
     * 是否扫描标准蓝牙设备
     */
    public boolean scanBluetooth(boolean isScan) {
        return isScan ? mAdapter.startDiscovery() : mAdapter.cancelDiscovery();
    }

    private void setDiscoverable(int second) {
        if (second <= 0)
            throw new IllegalArgumentException("传入参数不能小于等于0");
        Log.d(TAG, "setDiscoverable: 请求设备被发现");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, second);
        startActivity(discoverableIntent);
    }


    /**
     * 说明:
     * 1.失败、成功 -->这两个状态可以立即拿到结果
     * 2.进行中    -->为异步处理结果
     */
    public static final int BOND_FAILED = 0; //(解绑)绑定 失败
    public static final int BOND_SUCCESS = 1;//(解绑)绑定 成功
    public static final int BOND_ING = 2;    //(解绑)绑定 进行中 (需要等待广播结果)

    @IntDef({BOND_FAILED, BOND_SUCCESS, BOND_ING})
    @Retention(RetentionPolicy.SOURCE)
    @interface BondCode {

    }

    @BondCode
    public int unBondDevice(BluetoothDevice device) {
        if (device == null)
            return BOND_FAILED;
        if (isBondNo(device))
            return BOND_SUCCESS;
        if (isBonding(device) || isBonded(device)) {
            try {//注:解绑设备 方法说明中 没有表明为异步处理，这里按照还是按照异步的思维处理(在广播中获取结果)
                //@see BluetoothDevice#removeBond()
                return BlueToothUtil.removeBond(BluetoothDevice.class, device) ? BOND_ING : BOND_FAILED;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return BOND_FAILED;
    }

    @BondCode
    public int bondDevice(BluetoothDevice device) {
        if (device == null)
            return BOND_FAILED;
        if (isBonded(device))
            return BOND_SUCCESS;
        if (isBonding(device))
            return BOND_ING;
        if (isBondNo(device)) {
            try {
                //return BlueToothUtil.createBond(BluetoothDevice.class, device);
                return device.createBond() ? BOND_ING : BOND_FAILED;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return BOND_FAILED;
    }

    private boolean isBonding(@NonNull BluetoothDevice device) {
        return BluetoothDevice.BOND_BONDING == device.getBondState();
    }

    private boolean isBonded(@NonNull BluetoothDevice device) {
        return BluetoothDevice.BOND_BONDED == device.getBondState();
    }

    private boolean isBondNo(@NonNull BluetoothDevice device) {
        return BluetoothDevice.BOND_NONE == device.getBondState();
    }

    public void connect(final BluetoothDevice device) {
        if (isBonded(device)) {
            new Thread() {
                @Override
                public void run() {
                    if (isBonded(device)) {
                        try {
                            sleep(100);
                            Log.d(TAG, "run: 开启新线程去连接蓝牙:");
                            mHandler.sendEmptyMessage(MSG_CONNECT_START);
                            if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
                                Log.e(TAG, "run: 当前设备已连接,正关闭当前设备连接，切换到新设备  当前连接设备:" + mBluetoothSocket.getRemoteDevice().getName());
                                mBluetoothSocket.close();
                            }
                            mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                            mAdapter.cancelDiscovery();
                            mBluetoothSocket.connect();
                            mHandler.sendEmptyMessage(mBluetoothSocket.isConnected() ? MSG_CONNECT_SUCCESS : MSG_CONNECT_FAILED);
                            printBluetoothInfo();
                            //printInfo();
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (mBluetoothSocket != null) {
                                try {
                                    mBluetoothSocket.close();
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                            mBluetoothSocket = null;
                            mHandler.sendEmptyMessage(MSG_CONNECT_FAILED);
                        }
                    }
                }
            }.start();
        } else {
            Toast.makeText(this, "请先配对", Toast.LENGTH_SHORT).show();
            bondDevice(device);
        }
    }

    private void printBluetoothInfo() {
        if (mBluetoothSocket != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e(TAG, "run: 蓝牙是否连接:" + mBluetoothSocket.isConnected() + " 连接类型:" + mBluetoothSocket.getConnectionType());
            } else {
                Log.e(TAG, "run: 蓝牙是否连接:" + mBluetoothSocket.isConnected());
            }
        }
    }

    public void printInfo() {
        if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
            new Thread() {
                @Override
                public void run() {
                    OutputStream out;
                    try {
                        out = mBluetoothSocket.getOutputStream();
                        //OutputStreamWriter writer = new OutputStreamWriter(out, "GBK");

                        long startTime = System.currentTimeMillis();
                        Log.e(TAG, "run: 初始化打印机");
                       /*//初始化打印机
                       writer.write(0x1B);
                       writer.write(0x40);
                       writer.flush();

                       //直接打印文字
                       writer.write("hello printer");
                       writer.flush();*/


                        setStyle(out);//打印之前先设置打印机参数，不然会概率性发生默认将最后两行打印到起始位置
                        eYearTest(out);

                        //sendReceiptWithResponse(out);

                        Log.e(TAG, "run: 打印结束: 耗时:" + (System.currentTimeMillis() - startTime));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            Toast.makeText(this, "设备未连接", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 发送票据
     */
    void sendReceiptWithResponse(OutputStream outputStream) {
        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();
        esc.addPrintAndFeedLines((byte) 3);
        // 设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        // 设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        // 打印文字
        esc.addText("Sample\n");
        esc.addPrintAndLineFeed();

        /* 打印文字 */
        // 取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        // 设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        // 打印文字
        esc.addText("Print text\n");
        // 打印文字
        esc.addText("Welcome to use SMARNET printer!\n");

        /* 打印繁体中文 需要打印机支持繁体字库 */
        String message = "佳博智匯票據打印機\n";
        //String message = "佳博智能票据打印机\n";
        esc.addText(message, "GB2312");
        esc.addPrintAndLineFeed();

        /* 绝对位置 具体详细信息请查看GP58编程手册 */
        esc.addText("智汇");
        esc.addSetHorAndVerMotionUnits((byte) 7, (byte) 0);
        esc.addSetAbsolutePrintPosition((short) 6);
        esc.addText("网络");
        esc.addSetAbsolutePrintPosition((short) 10);
        esc.addText("设备");
        esc.addPrintAndLineFeed();

        /* 打印图片 */
        // 打印文字
        esc.addText("Print bitmap!\n");
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.gprinter);
        // 打印图片
        esc.addRastBitImage(b, 380, 0);

        /* 打印一维条码 */
        // 打印文字
        esc.addText("Print code128\n");
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        // 设置条码可识别字符位置在条码下方
        // 设置条码高度为60点
        esc.addSetBarcodeHeight((byte) 60);
        // 设置条码单元宽度为1
        esc.addSetBarcodeWidth((byte) 1);
        // 打印Code128码
        esc.addCODE128(esc.genCodeB("SMARNET"));
        esc.addPrintAndLineFeed();

        /*
         * QRCode命令打印 此命令只在支持QRCode命令打印的机型才能使用。 在不支持二维码指令打印的机型上，则需要发送二维条码图片
         */
        // 打印文字
        esc.addText("Print QRcode\n");
        // 设置纠错等级
        esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31);
        // 设置qrcode模块大小
        esc.addSelectSizeOfModuleForQRCode((byte) 3);
        // 设置qrcode内容
        esc.addStoreQRCodeData("www.smarnet.cc");
        esc.addPrintQRCode();// 打印QRCode
        esc.addPrintAndLineFeed();

        // 设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //打印文字
        esc.addText("Completed!\r\n");

        // 开钱箱
        esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
        esc.addPrintAndFeedLines((byte) 8);
        // 加入查询打印机状态，用于连续打印
        byte[] bytes = {29, 114, 1};
        esc.addUserCommand(bytes);
        Vector<Byte> datas = esc.getCommand();
        // 发送数据
        //DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);

        try {
            outputStream.write(convertVectorByteToBytes(datas));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void connectedState() {
        int flag = -1;

        int a2dp = mAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = mAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = mAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        int health1 = mAdapter.getProfileConnectionState(BluetoothProfile.GATT);
        int health2 = mAdapter.getProfileConnectionState(BluetoothProfile.GATT_SERVER);
        int health3 = mAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health4 = mAdapter.getProfileConnectionState(BluetoothProfile.HEARING_AID);
        int health5 = mAdapter.getProfileConnectionState(BluetoothProfile.HID_DEVICE);
        int health6 = mAdapter.getProfileConnectionState(BluetoothProfile.SAP);

        Log.e(TAG, "connectedState: a2dp:" + a2dp + " headset:");
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        } else if (health1 == BluetoothProfile.STATE_CONNECTED) {
            flag = health1;
        } else if (health2 == BluetoothProfile.STATE_CONNECTED) {
            flag = health2;
        } else if (health3 == BluetoothProfile.STATE_CONNECTED) {
            flag = health3;
        } else if (health4 == BluetoothProfile.STATE_CONNECTED) {
            flag = health4;
        } else if (health5 == BluetoothProfile.STATE_CONNECTED) {
            flag = health5;
        } else if (health6 == BluetoothProfile.STATE_CONNECTED) {
            flag = health6;
        }
        Log.e(TAG, "flag is " + flag);

        //if (flag != -1) {
        if (true) {
            mAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {

                @Override
                public void onServiceDisconnected(int profile) {
                    Log.e(TAG, "onServiceDisconnected:" + profile);

                }

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    Log.e(TAG, "onServiceConnected: " + profile);
                    List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                    if (mDevices != null && mDevices.size() > 0) {
                        for (BluetoothDevice device : mDevices) {
                            Log.e(TAG, "device name: " + device.getName());
                        }
                    } else {
                        Log.e(TAG, "mDevices is null");
                    }
                }
            }, flag);
        }
    }


    private void setStyle(OutputStream outputStream) {
        try {
            Log.e(TAG, "setStyle: 设置style ----start----");
            outputStream.write(codes[0]);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "setStyle: 设置style ----error----");
        }
        Log.e(TAG, "setStyle: 设置style ----end----");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void eYearTest(OutputStream outputStream) {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder
                .append(STAR)
                .append("\n")
                .append(STAR)
                .append("\n")
                .append("           水费通知单")
                .append("\n")
                .append(STAR)
                .append("\n")
                .append(STAR)
                .append("\n")
                .append("用户姓名:")
                .append(SPACE)
                .append("佚名")
                .append("\n")
                .append("地址:    ")
                .append(SPACE)
                .append("环球中心")
                .append("\n")
                .append("用户编号:")
                .append(SPACE)
                .append("454541454")
                .append("\n")
                .append("上月读数:")
                .append(SPACE)
                .append("45d")
                .append("\n")
                .append("本月读数:")
                .append(SPACE)
                .append("\n")
                .append("上月用量:")
                .append(SPACE)
                .append("\n")
                .append("本月用量:")
                .append(SPACE)
                .append("\n")
                .append("欠费金额:")
                .append(SPACE)
                .append("\n")
                .append("欠费月数:")
                .append(SPACE)
                .append("\n")
                .append("联系电话:")
                .append(SPACE)
                .append("\n\n\n\n\n") //这里需要多行切换，不然最后的内容会在机器里面
        ;
        try {
            outputStream.write(sBuilder.toString().getBytes("gbk"));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected byte[] convertVectorByteToBytes(Vector<Byte> data) {
        byte[] sendData = new byte[data.size()];
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); ++i) {
                sendData[i] = (Byte) data.get(i);
            }
        }
        return sendData;
    }

    public byte[] addStrToCommand(String str, String encode) {
        if (!str.equals("")) {
            try {
                return str.getBytes(encode);
            } catch (UnsupportedEncodingException var5) {
                var5.printStackTrace();
            }
        }
        return null;
    }


    private void initBluetoothFilter() {

        if (mBtStateReceiver == null)
            mBtStateReceiver = new BluetoothStateReceiver(this);
        registerReceiver(mBtStateReceiver, mBtStateReceiver.getBtStateFilter());
    }

    private void cancelBluetoothFilter() {
        if (mBtStateReceiver != null)
            unregisterReceiver(mBtStateReceiver);
    }


    private static final int MSG_CONNECT_START = 0;
    private static final int MSG_CONNECT_SUCCESS = 1;
    private static final int MSG_CONNECT_FAILED = 2;

    private static final class BtServiceHandler extends Handler {
        WeakReference<BluetoothService> wk;

        public BtServiceHandler(@NonNull Looper looper, BluetoothService bluetoothService) {
            super(looper);
            wk = new WeakReference<>(bluetoothService);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            BluetoothService service = wk.get();
            if (service == null)
                return;
            switch (msg.what) {
                case MSG_CONNECT_START:
                    service.mBtStateReceiver.sendStateCode(IBluetoothStateCallBack.BT_CODE_DEV_CONNECT_START);
                    break;
                case MSG_CONNECT_SUCCESS:
                    service.mBtStateReceiver.sendStateCode(IBluetoothStateCallBack.BT_CODE_DEV_CONNECT_SUCCESS);
                    service.sendCurBondedDevs();
                    service.connectedState();
                    service.setFlowViewContent(true);
                    break;
                case MSG_CONNECT_FAILED:
                    service.mBtStateReceiver.sendStateCode(IBluetoothStateCallBack.BT_CODE_DEV_CONNECT_FAILED);
                    service.sendCurBondedDevs();
                    service.setFlowViewContent(false);
                    break;
            }
        }
    }
}
