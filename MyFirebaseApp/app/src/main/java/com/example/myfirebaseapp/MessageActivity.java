package com.example.myfirebaseapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MessageActivity extends AppCompatActivity {

    private BluetoothDevice device;
    private BluetoothSocket mmSocket;
    private OutputStream outputStream;
    private EditText editTextMessage;
    private TextView textViewDisplay;
    private Button buttonSend;
    private Button singleMessageButton;
    private StringBuilder messageHistory = new StringBuilder(); // to store message history

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        editTextMessage = findViewById(R.id.editTextMessage);
        textViewDisplay = findViewById(R.id.textViewDisplay);
        buttonSend = findViewById(R.id.buttonSend);
//        singleMessageButton = findViewById(R.id.singleMessageButton);

        device = getIntent().getParcelableExtra("device");

        if (device == null) {
            Toast.makeText(this, "No Bluetooth device found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        messageHistory = new StringBuilder();

        connectToDevice();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                sendMessage(message);
                editTextMessage.setText(""); // Clear the input after sending the message
            }
        });

//        singleMessageButton.setOnClickListener(v -> {
//            Intent intent = new Intent(MessageActivity.this, MessageActivity.class);
//            intent.putExtra("device", device); // Pass the BluetoothDevice object back
//            startActivity(intent);
//        });

        // If there's any existing message history, display it
        if (savedInstanceState != null) {
            String savedHistory = savedInstanceState.getString("messageHistory");
            if (savedHistory != null) {
                messageHistory.append(savedHistory);
                textViewDisplay.setText(messageHistory.toString());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the message history in case of activity recreation
        outState.putString("messageHistory", messageHistory.toString());
    }

    private void connectToDevice() {
        try {
            final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mmSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            outputStream = mmSocket.getOutputStream();
            Toast.makeText(this, "Connected to HC05", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendMessage(String message) {
        try {
            outputStream.write((message + "\n").getBytes());
            // Append the current message to the message history
            messageHistory.append(message).append("\n");
            // Update the display with the message history
            textViewDisplay.setText(messageHistory.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mmSocket != null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
