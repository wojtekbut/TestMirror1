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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Button connect;
    Button onoff;
    Button arduino;
    Boolean on;
    TextView xval;
    TextView yval;
    TextView macadres;
    ListView listaview;
    ArrayList listaArray;
    ArrayAdapter<String> arrayAdapter;
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
        arduino = (Button) findViewById(R.id.arduino);
        listaview = (ListView) findViewById(R.id.lista);
        listaview.setVisibility(View.INVISIBLE);
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
            startActivity(enableIntent);
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
                        String roz = "run:";
                        String msg = "off";
                        //byte[] bytes = msg.getBytes(charset);
                        polaczenie.writes(roz, msg);
                    } else {
                        on = !on;
                        onoff.setText("Wyłącz");
                        String roz = "run:";
                        String msg = "on";
                        byte[] bytes = msg.getBytes(charset);
                        polaczenie.writes(roz, msg);
                    }

                }
            }
        });

        arduino.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (polaczenie != null) {
                    String roz = "lista:";
                    String msg = "getBonded";
                    polaczenie.writes(roz,msg);
                }
            }
        });

        if (device != null) {
            Log.d("resume", "onResume: device" + device.toString());
            if (polaczenie == null) {
                Log.d("resume", "onResume: polaczenie jest null");
                polaczenie = new ConnectThread(device, mHandler);
                polaczenie.start();
                macadres.setText(nazwa.concat(adresMac));
            }
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
            try {
                Log.d("handle:", "lista: " + Arrays.toString((byte[]) msg.obj));
                Log.d("handle:", "lista clas: " + msg.obj.getClass().getName());
            } catch (Exception e) {
                //e.printStackTrace();
            }

            if (msg.what != 5) {
                text = msg.obj.toString();
                Log.d("mhandler", "handleMessage: " + text);
                Log.d("mhandler", "text.length()= " + text.length() + text);
            }
            if (text.length()==5 && text.equals("- - -")) {
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
                case 5:
                    //try {
                        //String listaS = Arrays.toString((byte[]) msg.obj);
                        //byte[] list = (byte[]) msg.obj;
                        //Log.d("bonded:", "dlugosc listyS: " + listaS.length());
                        //Log.d("bonded:", "listaS: " + listaS);
                        //Log.d("bonded:", "listaS clasa: " + listaS.getClass().getName());

                        //Log.d("bonded:", "dlugosc listy: " + list.length);
                        //Log.d("bonded:", "lista: " + Arrays.toString(list));
                        //Log.d("bonded:", "lista clasa: " + list.getClass().getName());

                        //ByteArrayInputStream bais = new ByteArrayInputStream(list);

                        //ObjectInputStream objectIn = new ObjectInputStream(bais);
                                //new ByteArrayInputStream((byte[]) list));
                        //listaArray = (ArrayList<String>) objectIn.readObject();
                        listaArray = (ArrayList) msg.obj;
                        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.lista, listaArray);
                        listaview.setAdapter(arrayAdapter);
                        listaview.setVisibility(View.VISIBLE);
                        listaview.bringToFront();
                        onoff.setVisibility(View.INVISIBLE);
                        arduino.setVisibility(View.INVISIBLE);
                        connect.setVisibility(View.INVISIBLE);
                        listaview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String info = ((TextView) view).getText().toString();
                                String address = info.substring(0,17);
                                String name = info.substring(17);
                                Log.d("onclicklista:", "adres: " + address + " nazwa: " + name);
                                listaview.setVisibility(View.INVISIBLE);
                                //listaview.bringToFront();
                                onoff.setVisibility(View.VISIBLE);
                                arduino.setVisibility(View.VISIBLE);
                                connect.setVisibility(View.VISIBLE);
                                polaczenie.writes("polArd", address);

                            }
                        });
                    //} catch()  {
                        //e.printStackTrace();

                    break;
            }
        }
    };
}
