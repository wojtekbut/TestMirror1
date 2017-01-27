package com.example.wojtek.testmirror1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;


/**
 * Created by wojtek on 22.01.17.
 */

public class ConnectThread extends Thread {

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final Handler mHandler;
    InputStream mmInStream;
    OutputStream mmOutStream;
    private final String TAG = "connectThread";

    public ConnectThread(BluetoothDevice device, Handler handler) {

        mmDevice = device;
        mHandler = handler;
        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));

        } catch (IOException e) {
            Log.d("connectThread", "Socket's create() method failed", e);
        }
        mmSocket = tmp;

    }


    @Override
    public void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("connectThread", "Could not close the client socket", closeException);
            }
            return;
        }

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {

        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        czytaj();

    }

    public void czytaj() {

        byte[] buffer = new byte[10];
        int bytes;
        bytes = 0;
        Log.d("connectThread", "polacz: polaczylem z: " + mmDevice.toString());
        while (mmSocket.isConnected()) {
            try {
                buffer[bytes] = (byte) mmInStream.read();
                //Log.d(TAG, "polacz: mam bajty: " + new String(buffer));
                if (buffer[bytes] == 10) {
                    bytes = 0;
                    if ((char) buffer[0] == 'x') {
                        //Log.d(TAG, "polacz: mam bajty: " + new String(buffer));

                        mHandler.obtainMessage(1, new String(buffer)).sendToTarget();

                    } else if ((char) buffer[0] == 'y') {

                        mHandler.obtainMessage(2, new String(buffer)).sendToTarget();

                    }
                    Arrays.fill(buffer, (byte) 0);
                } else {
                    bytes += 1;
                }

                if (bytes == 10) {
                    Arrays.fill(buffer, (byte) 0);
                    bytes = 0;
                }

            } catch (IOException e) {

                Log.d("socket", "not connected");
            }
        }

    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public void write(byte[] buffer) {
        if (mmSocket == null) {
            Log.d("socket", "null");
            return;
        }
        try {
            if (!mmSocket.isConnected()) {
                Log.d("socket", "not connected");
                return;
            } else {
                Log.d(TAG, "Wysyłam: " + new String(buffer));

                mmOutStream.write(buffer);
            }

            // Share the sent message back to the UI Activity
            //mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
            //        .sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

}

