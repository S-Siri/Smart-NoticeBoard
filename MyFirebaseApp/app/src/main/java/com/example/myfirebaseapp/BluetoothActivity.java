package com.example.myfirebaseapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceClickListener {
    private static final int REQUEST_ENABLE_BT = 100;
    private BluetoothAdapter bluetoothAdapter = null;
    private DeviceAdapter deviceAdapter;

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean bluetoothScanGranted = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    bluetoothScanGranted = result.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false);
                }
                Boolean bluetoothConnectGranted = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    bluetoothConnectGranted = result.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false);
                }

                if (bluetoothScanGranted != null && bluetoothConnectGranted != null) {
                    if (bluetoothScanGranted && bluetoothConnectGranted) {
                        setUpBluetoothAdapter();
                        enableBluetoothIfNeeded();
                        showPairedDevices();
                        discoverDevices();
                    } else {
                        Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new DeviceAdapter(this, this);
        recyclerView.setAdapter(deviceAdapter);

        requestPermissions();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            });
        } else {
            setUpBluetoothAdapter();
            enableBluetoothIfNeeded();
            showPairedDevices();
            discoverDevices();
        }
    }

    @SuppressLint("MissingPermission")
    private void setUpBluetoothAdapter() {
        BluetoothManager bluetoothManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            bluetoothManager = getSystemService(BluetoothManager.class);
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
        } else {
            if (!isBluetoothScanPermitted()) {
                return;
            }
            Toast.makeText(this, "Scanning Started", Toast.LENGTH_SHORT).show();
            bluetoothAdapter.startDiscovery();
        }
    }

    @SuppressLint("MissingPermission")
    private void enableBluetoothIfNeeded() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private boolean isBluetoothConnectPermitted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isBluetoothScanPermitted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    private void discoverDevices() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && isBluetoothConnectPermitted()) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.i("DevicesFound", deviceName + " " + deviceHardwareAddress);
                    deviceAdapter.addDevice(device);
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void showPairedDevices() {
        if (isBluetoothConnectPermitted()) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    deviceAdapter.addDevice(device);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onDeviceClick(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, handle it as needed
            return;
        }

        if (!device.getName().equals("HC-05")) {
            Toast.makeText(this, "Please select the HC05 device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initiate connection
        final BluetoothSocket mmSocket;
        try {
            final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            mmSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Connected! Show toast and send a message
        Toast.makeText(this, "Connected to HC05", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream outputStream = mmSocket.getOutputStream();
                    String message = "Your single-line message here\n";  // Add a newline character

                    // Send the message as bytes
                    byte[] bytes = message.getBytes();
                    outputStream.write(bytes);

                    // (Optional) Handle sending multiple messages in a loop

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Close the socket
                    try {
                        mmSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        // Start MessageActivity with the selected device
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("device", device);
        startActivity(intent);
    }
}
