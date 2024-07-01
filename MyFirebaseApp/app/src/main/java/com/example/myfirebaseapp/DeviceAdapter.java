package com.example.myfirebaseapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<BluetoothDevice> deviceList;
    private Context context;
    private OnDeviceClickListener onDeviceClickListener;

    public DeviceAdapter(Context context, OnDeviceClickListener onDeviceClickListener) {
        this.deviceList = new ArrayList<>();
        this.context = context;
        this.onDeviceClickListener = onDeviceClickListener;
    }

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    public void addDevice(BluetoothDevice device) {
        if (!deviceList.contains(device)) {
            deviceList.add(device);
            notifyItemInserted(deviceList.size() - 1);
        }
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new DeviceViewHolder(view, onDeviceClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.itemView.setTag(device);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView deviceName;
        TextView deviceAddress;
        OnDeviceClickListener onDeviceClickListener;

        DeviceViewHolder(@NonNull View itemView, OnDeviceClickListener onDeviceClickListener) {
            super(itemView);
            deviceName = itemView.findViewById(android.R.id.text1);
            deviceAddress = itemView.findViewById(android.R.id.text2);
            this.onDeviceClickListener = onDeviceClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            BluetoothDevice device = (BluetoothDevice) v.getTag();
            onDeviceClickListener.onDeviceClick(device);
        }
    }
}
