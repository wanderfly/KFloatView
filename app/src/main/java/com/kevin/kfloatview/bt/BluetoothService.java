package com.kevin.kfloatview.bt;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kevin.kfloatview.R;
import com.kevin.kfloatview.bt.floatview.EnFloatingView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice mCacheFound;     //缓存指定的蓝牙设备
    private BluetoothStateReceiver mBtStateReceiver;

    private BluetoothActivity mActivity;
    private FrameLayout mContainer;
    private final HashMap<String, View> mViewMaps = new HashMap<>();

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ");
        super.onCreate();
        BluetoothManager bm = (BluetoothManager) getSystemService(Service.BLUETOOTH_SERVICE);
        mAdapter = bm.getAdapter();
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
        Log.e(TAG, "onStartCommand: " + intent.getAction());
        //Bundle bundle = intent.getBundleExtra(ACTIVITY_EXTRA_NAME);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: ");
        if (mActivity != null && mContainer != null) {
            Log.e(TAG, "onUnbind: 移除view");
            for (Map.Entry<String, View> map : mViewMaps.entrySet()) {
                Log.e(TAG, "onUnbind: key:"+map.getKey()+" value:"+map.getValue());
                mContainer.removeView(map.getValue());
                mViewMaps.remove(map.getKey());
            }
            mActivity = null;
            mContainer = null;
        }
        //return super.onUnbind(intent);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelBluetoothFilter();
        Log.d(TAG, "onDestroy: ");
    }

    public interface PrintCallback {
        void stateChange(String data);
    }

    private PrintCallback mPrintCallback;

    public void setPrintCallback(PrintCallback callback) {
        mPrintCallback = callback;
    }


    public void printData(String jsonSting) {

    }

    public void setCurActivity(BluetoothActivity activity) {
        mActivity = activity;
    }

    /**
     * 调用在setCurActivity之后才有效
     *
     * @see #setCurActivity(BluetoothActivity)
     */
    public void showBtStateView() {
        if (mActivity != null) {
            Log.e(TAG, "showBtStateView: ");
            mContainer = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

            //添加测试TextView
            /*TextView textView = new TextView(mActivity);
            textView.setText("nice too meet you\n boy");
            textView.setLayoutParams(getFloatingLayoutParams());
            textView.setBackgroundColor(Color.CYAN);
            mContainer.addView(textView);
            mViewMaps.put("mTV", textView);*/

            //
            EnFloatingView floatingView= new EnFloatingView(mActivity, R.layout.layout_float_view);
            floatingView.setLayoutParams(getFloatingLayoutParams());
            mContainer.addView(floatingView);
            mViewMaps.put("floatingView", floatingView);



        }
    }

    private FrameLayout.LayoutParams getFloatingLayoutParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.START;
        params.setMargins(0, params.topMargin, params.rightMargin, 500);
        return params;
    }

    public void releaseCurActivity() {
        Log.e(TAG, "releaseCurActivity: mActivity:" + mActivity);
        if (mActivity != null)
            mActivity = null;
    }


    /**
     * 是否扫描标准蓝牙设备
     */
    private boolean scanBluetooth(boolean isScan) {
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

    private Set<BluetoothDevice> getBondedDevices() {
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            mSBuilder.setLength(0);
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();//MAC address
                if (DEBUG) {
                    Log.d(TAG, "getBondedDevices: 设备名字:" + deviceName);
                    Log.d(TAG, "getBondedDevices: 设备地址:" + deviceHardwareAddress);
                }
                mSBuilder.append("设备名字:")
                        .append(deviceName)
                        .append("\n")
                        .append("设备地址:")
                        .append(deviceHardwareAddress)
                        .append("\n\n");
            }
            // mTvContent.setText(mSBuilder.toString());
        }
        return pairedDevices;
    }

    private boolean bondDevices(BluetoothDevice device) {
        if (device == null)
            return false;
        if (isBonded(device) || isBonding(device))
            return true;
        if (isBondNo(device)) {
            try {
                return BlueToothUtil.createBond(BluetoothDevice.class, device);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean isBonding(BluetoothDevice device) {
        return BluetoothDevice.BOND_BONDING == device.getBondState();
    }

    private boolean isBonded(BluetoothDevice device) {
        return BluetoothDevice.BOND_BONDED == device.getBondState();
    }

    private boolean isBondNo(BluetoothDevice device) {
        return BluetoothDevice.BOND_NONE == device.getBondState();
    }

    private void connect(final BluetoothDevice device) {
        if (isBonded(device)) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        if (isBonded(device)) {
                            sleep(1000);
                            Log.d(TAG, "run: 开启新线程去连接蓝牙:");

                            mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                            mAdapter.cancelDiscovery();
                            mBluetoothSocket.connect();
                            printBluetoothInfo();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (mBluetoothSocket != null) {
                            try {
                                mBluetoothSocket.close();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                }
            }.start();
        } else {
            Toast.makeText(this, "请先配对", Toast.LENGTH_SHORT).show();
            bondDevices(device);
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

    private void printInfo() {
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
            mBtStateReceiver = new BluetoothStateReceiver();
        registerReceiver(mBtStateReceiver, mBtStateReceiver.getBtStateFilter());
    }

    private void cancelBluetoothFilter() {
        if (mBtStateReceiver != null)
            unregisterReceiver(mBtStateReceiver);
    }


    private static final int MSG_BOND_BLUETOOTH_DEVICE = 0;
    private static final int MSG_CONNECT_BLUETOOTH_DEVICE = 1;
    private static final int MSG_CONNECT_BLE_BLUETOOTH_DEVICE = 2;
    private static final int MSG_CLOSE_BLE_CONNECT = 3;
    private static final int MSG_CONNECT_OTHER = 20;
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MSG_BOND_BLUETOOTH_DEVICE:
                    bondDevices((BluetoothDevice) message.obj);
                    break;
                case MSG_CONNECT_BLUETOOTH_DEVICE:
                    connect((BluetoothDevice) message.obj);
                    break;
                case MSG_CONNECT_BLE_BLUETOOTH_DEVICE:
                    //connectBle((BluetoothDevice) message.obj);
                    break;
                case MSG_CLOSE_BLE_CONNECT:
                    //closeBluetoothBleGatt();
                    break;
                case MSG_CONNECT_OTHER:
                    break;
            }
            return false;
        }
    });
}
