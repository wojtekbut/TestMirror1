package com.example.wojtek.testmirror1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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

        } catch (IOException e) {
            Log.e("InStream", "InputStream nie podłączony.");
        }
        try {
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.e("OutStream", "OutputStream nie podłączony.");
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        if (mmSocket.isConnected()) {
            Log.d("mmsocket","podłączony");
        }
        if (!mmSocket.isConnected()){
            Log.d("mmsocket","nie podłączony");
        }

        czytaj();

    }

/*
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
*/

    public void czytaj() {
        byte[] buffer = new byte[2];
        byte[] poczatek = new byte[6];
        byte dlrozkazu;
        byte[] rozkaz;// = new byte[];
        byte[] objetosc = new byte[2];
        byte[] koniec = new byte[7];
        int dlugosc;
        while (mmSocket.isConnected()) {
            try {
                poczatek[0] = (byte) mmInStream.read();
                Log.d("czytaj","Przeczytałem " + (char) poczatek[0]);
                if ((char) poczatek[0] == 's'){
                    for (int i=1;i<6;i++){
                        poczatek[i] = (byte) mmInStream.read();
                    }
                    String spocz = new String(poczatek);
                    Log.d("czytaj","Przeczytałem " + spocz);
                    if (spocz.equals("stArt:")){
                        dlrozkazu = (byte) mmInStream.read();
                        rozkaz = new byte[dlrozkazu];
                        for (int i = 0;i<2;i++) {
                            objetosc[i] = (byte) mmInStream.read();
                        }
                        dlugosc = objetosc[0]*256+objetosc[1];
                        buffer = new byte[dlugosc];
                        for (int i = 0; i< dlrozkazu; i++){
                            rozkaz[i] = (byte) mmInStream.read();
                        }
                        for (int i = 0; i< dlugosc; i++){
                            buffer[i] = (byte) mmInStream.read();
                        }
                        for (int i = 0; i<7; i++) {
                            koniec[i] = (byte) mmInStream.read();
                        }
                        String skoniec = new String(koniec);
                        if (skoniec.equals(":koNiec")){
                            Log.d("czytaj:", "lista: " + Arrays.toString(buffer));
                            wiadomosc(new String(rozkaz), buffer);
                            Arrays.fill(buffer, (byte) 0);
                            Arrays.fill(poczatek, (byte) 0);
                            Arrays.fill(koniec, (byte) 0);
                            Arrays.fill(objetosc, (byte) 0);
                        }else {
                            Arrays.fill(buffer, (byte) 0);
                            Arrays.fill(poczatek, (byte) 0);
                            Arrays.fill(koniec, (byte) 0);
                            Arrays.fill(objetosc, (byte) 0);
                            continue;
                        }
                    }else {
                        Arrays.fill(buffer, (byte) 0);
                        Arrays.fill(poczatek, (byte) 0);
                        Arrays.fill(koniec, (byte) 0);
                        Arrays.fill(objetosc, (byte) 0);
                        continue;
                    }
                }
            } catch (IOException e) {

            }
        }

    }

    public void wiadomosc(String rozkaz, byte[] wiadomosc ) {
        Log.d("wiadomość", rozkaz);
        Log.d("wiadomosc:", "lista: " + Arrays.toString(wiadomosc));
        Log.d("wiadomość:", "lista clasa: " + wiadomosc.getClass().getName());

        if (rozkaz.startsWith("x:")) {
            //Log.d(TAG, "polacz: mam bajty: " + new String(buffer));
            mHandler.obtainMessage(1, new String(wiadomosc)).sendToTarget();
        } else if (rozkaz.startsWith("y:")) {
            mHandler.obtainMessage(2, new String(wiadomosc)).sendToTarget();
        } else if (rozkaz.startsWith("lista:")) {
            ArrayList<String> listaArray = new ArrayList<String>();
            ByteArrayInputStream bais = new ByteArrayInputStream(wiadomosc);

            ObjectInputStream objectIn = null;
            try {
                objectIn = new ObjectInputStream(bais);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new ByteArrayInputStream((byte[]) list));
            try {
                listaArray = (ArrayList<String>) objectIn.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Log.d("wiadomość:", "listaArray clasa: " + listaArray.getClass().getName());
            mHandler.obtainMessage(5,listaArray ).sendToTarget();
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
        try {
            if (mmSocket.getOutputStream() == null) {
                Log.d("socket", "null");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (!mmSocket.isConnected()) {
                Log.d("socket", "not connected");
                //mmOutStream.write(buffer);
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

    public void writeb(String rozkaz, byte[] bity){
        String poczatek, koniec;
        byte[] bpoczatek, bkoniec, brozkaz, wiadomosc;
        poczatek = "stArt:";
        koniec = ":koNiec";
        bpoczatek = poczatek.getBytes();
        bkoniec = koniec.getBytes();
        brozkaz = rozkaz.getBytes();
        byte[] objetosc = new byte[2];
        byte dlrozkazu = (byte) brozkaz.length;


        int dlugosc = bity.length;
        if (dlugosc < 256) {
            objetosc[0] = 0;
            objetosc[1] = (byte) dlugosc;
        } else if (dlugosc > 255 && dlugosc < 65535) {
            objetosc[0] = (byte) (dlugosc / 256);
            objetosc[1] = (byte) (dlugosc - ((int) objetosc[0] * 256));
        } else {
            Log.e("Write", "Wiadomość za długa.");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(bpoczatek);
            outputStream.write(dlrozkazu);
            outputStream.write(objetosc);
            outputStream.write(brozkaz);
            outputStream.write(bity);
            outputStream.write(bkoniec);

        } catch (IOException e) {
            e.printStackTrace();
        }
        wiadomosc = outputStream.toByteArray();
        write(wiadomosc);
    }

    public void writes(String rozkaz, String string) {
        String poczatek, koniec;
        byte[] bpoczatek, bkoniec, brozkaz, bstring, wiadomosc;
        poczatek = "stArt:";
        koniec = ":koNiec";
        bpoczatek = poczatek.getBytes();
        bkoniec = koniec.getBytes();
        brozkaz = rozkaz.getBytes();
        bstring = string.getBytes();
        byte[] objetosc = new byte[2];
        byte dlrozkazu = (byte) brozkaz.length;

        int dlugosc = bstring.length;
        if (dlugosc < 256) {
            objetosc[0] = 0;
            objetosc[1] = (byte) dlugosc;
        } else if (dlugosc > 255 && dlugosc < 65535) {
            objetosc[0] = (byte) (dlugosc / 256);
            objetosc[1] = (byte) (dlugosc - ((int) objetosc[0] * 256));
        } else {
            Log.e("Write", "Wiadomość za długa.");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(bpoczatek);
            outputStream.write(dlrozkazu);
            outputStream.write(objetosc);
            outputStream.write(brozkaz);
            outputStream.write(bstring);
            outputStream.write(bkoniec);

        } catch (IOException e) {
            e.printStackTrace();
        }
        wiadomosc = outputStream.toByteArray();
        write(wiadomosc);
    }

    public void writeBonded(byte[] bondedbuff) {
        try {

            mmOutStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

