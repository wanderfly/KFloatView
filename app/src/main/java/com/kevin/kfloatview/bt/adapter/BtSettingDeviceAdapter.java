package com.kevin.kfloatview.bt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.kfloatview.R;

import java.util.ArrayList;

/**
 * @author Kevin  2021/6/1
 */
public class BtSettingDeviceAdapter extends RecyclerView.Adapter<BtSettingDeviceAdapter.DeviceHolder> {

    public static class DeviceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView ivIcon;
        private final ImageView ivLinked;
        private final TextView tvDeviceName;
        private final TextView tvLinkState;

        private final OnItemClickListener listener;

        public DeviceHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_bt_setting_rc_device_ic);
            ivLinked = itemView.findViewById(R.id.iv_bt_setting_rc_linked);
            tvDeviceName = itemView.findViewById(R.id.tv_bt_setting_rc_device_name);
            tvLinkState = itemView.findViewById(R.id.tv_bt_setting_rc_link_state);
            listener = itemClickListener;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(v, getAdapterPosition());
            }
        }
    }


    private ArrayList<BtSettingDeviceInfo> mBondedDevs;
    private final Drawable mDrawPrinterNormal;
    private final Drawable mDrawPrinterChecked;

    public BtSettingDeviceAdapter(@NonNull Context context, @NonNull ArrayList<BtSettingDeviceInfo> bondedDevices) {
        this.mDrawPrinterNormal = ResourcesCompat.getDrawable(context.getResources(), R.drawable.bt_setting_printer_normal, null);
        this.mDrawPrinterChecked = ResourcesCompat.getDrawable(context.getResources(), R.drawable.bt_setting_printer_checked, null);
        this.mBondedDevs = bondedDevices;
    }

    /**
     * 定义RecyclerView选项单击事件的回调接口
     */
    public interface OnItemClickListener {

        void onItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateBondedDevices(@NonNull ArrayList<BtSettingDeviceInfo> bondedDevices) {
        this.mBondedDevs = bondedDevices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RelativeLayout rl = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_setting_rc_adapter, parent, false);

        return new DeviceHolder(rl, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BtSettingDeviceAdapter.DeviceHolder holder, int position) {
        BtSettingDeviceInfo bonedDev = mBondedDevs.get(position);

        String devName = bonedDev.getName();
        holder.tvDeviceName.setText(devName == null ? "未知设备" : devName);
        if (bonedDev.getLinkState()) {
            holder.ivIcon.setImageDrawable(mDrawPrinterChecked);
            holder.tvLinkState.setText("已连接");
            holder.tvLinkState.setTextColor(Color.parseColor("#1890FF"));
            holder.tvDeviceName.setTextColor(Color.parseColor("#1890FF"));
            holder.ivLinked.setVisibility(View.VISIBLE);
        } else {
            holder.ivIcon.setImageDrawable(mDrawPrinterNormal);
            holder.tvLinkState.setText("连接");
            holder.tvLinkState.setTextColor(Color.parseColor("#CAE2F9"));
            holder.tvDeviceName.setTextColor(Color.parseColor("#333333"));
            holder.ivLinked.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return mBondedDevs.size();
    }
}
