package com.example.wojtek.testmirror1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    Button connect;
    Button onoff;
    Boolean on;
    TextView xval;
    TextView yval;
    TextView macadres;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice device;
    String adresMac;
    ConnectThread polaczenie;
    String text, nazwa;
    public Charset charset = Charset.forName("UTF-8");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xval = (TextView) findViewById(R.id.xval);
        yval = (TextView) findViewById(R.id.yval);
        macadres = (TextView) findViewById(R.id.textView2);
        macadres.setText("Nie połączony.");
        connect = (Button) findViewById(R.id.connect);
        onoff = (Button) findViewById(R.id.onoff);
        on = false;
        device = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 3);
        }



    }

    protected void onResume() {
        super.onResume();
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent devlisti = new Intent(getApplicationContext(), ListaUrzadzen.class);
                startActivityForResult(devlisti, 1);
            }
        });
        onoff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (polaczenie != null) {
                    if (on) {
                        on = !on;
                        onoff.setText("Włącz");
                        String msg = "off" + "\n";
                        byte[] bytes = msg.getBytes(charset);
                        polaczenie.write(bytes);
                    } else {
                        on = !on;
                        onoff.setText("Wyłącz");
                        String msg = "on" + "\n";
                        byte[] bytes = msg.getBytes(charset);
                        polaczenie.write(bytes);
                    }

                }
            }
        });

        if (device != null) {
            Log.d("resume", "onResume: device" + device.toString());
            if (polaczenie == null) {Log.d("resume", "onResume: polaczenie jest null");}
            polaczenie = new ConnectThread(device,mHandler);
            polaczenie.start();
            macadres.setText(nazwa.concat(adresMac));
        } else {
            Log.d("resume", "onResume: device = null" );
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        connect.setOnClickListener(null);
        if (polaczenie != null) {
            Log.d("pause", "onPause: usuwam polaczenie");
            polaczenie.cancel();
            polaczenie = null;
        }
        macadres.setText("Nie połączony.");
        if (device != null) {
            Log.d("pause", "onPause: device" + device.toString());
        } else {
            Log.d("pause", "onPause: device = null");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
            adresMac = data.getStringExtra("adres");
            nazwa = data.getStringExtra(("nazwa"));
            macadres.setText(nazwa.concat(adresMac));
            device = mBluetoothAdapter.getRemoteDevice(adresMac);
            polaczenie = new ConnectThread(device, mHandler);
            polaczenie.start();
        } else {
            macadres.setText("Nie wybrano adresu.\nPołącz jeszcze raz.");
        }
    }



    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            text = msg.obj.toString();
            Log.d("mhandler", "handleMessage: "+ text);
            //Log.d("mhandler", "handleMessage(2,6): "+ text.substring(2,7));
            if (text.substring(2,7).equals("- - -")) {
                on = false;
                onoff.setText("Włącz");
            } else
                if (!on) {
                    on = true;
                    onoff.setText("Wyłącz");
            }
            switch (msg.what) {
                case 1:
                    xval.setText(text);
                    break;
                case 2:
                    yval.setText(text);
                    break;
            }
        }
    };
}
