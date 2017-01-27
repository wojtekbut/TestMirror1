package com.example.wojtek.testmirror1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class ListaUrzadzen extends Activity {
    TextView textView;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> sparowaneUrzadzeniaArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_urzadzen);
        sparowaneUrzadzeniaArray = new ArrayAdapter<String>(this, R.layout.lista);
        ListView pairedListView = (ListView) findViewById(R.id.ListView);
        pairedListView.setAdapter(sparowaneUrzadzeniaArray);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices and append to 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Add previosuly paired devices to the array
        if (pairedDevices.size() > 0) {
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
            for (BluetoothDevice device : pairedDevices) {
                sparowaneUrzadzeniaArray.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = "Brak sparowanych urządzeń.".toString();
            sparowaneUrzadzeniaArray.add(noDevices);
        }

    }

    // Set up on-click listener for the list (nicked this - unsure)
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            //textView1.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            String name = info.substring(0,info.length() - 17);

            // Make an intent to start next activity while taking an extra which is the MAC address.
            Intent data = new Intent();
            data.putExtra("adres", address);
            data.putExtra("nazwa", name);
            setResult(RESULT_OK,data);
            finish();
        }
    };



}
