package com.example.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button btnONOFF;
    Button btnDiscover;

    ListView lvPairedDevices;

    BluetoothAdapter myBluetoothAdapter;

    Intent bluetoothEnableIntent;

    int requestCodeForEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        requestCodeForEnable = 1;

        btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnDiscover = (Button) findViewById(R.id.btnEnableDiscoverability);

        lvPairedDevices = (ListView) findViewById(R.id.listViewPairedDevices);



        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnBluetoothOnOff();
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableDiscoverability();
            }
        });

    }

    private void turnBluetoothOnOff(){
        if(myBluetoothAdapter == null){
            Log.d(TAG, "Bluetooth does not supporting");
        } else {
            if(!myBluetoothAdapter.isEnabled()){
                Log.d(TAG, "ENABLING BLUETOOTH");
                startActivity(bluetoothEnableIntent);
            } else if (myBluetoothAdapter.isEnabled()){
                Log.d(TAG, "DISABLING BLUETOOTH");
                myBluetoothAdapter.disable();
            }
        }
    }

    private void enableDiscoverability(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 10);
        startActivity(intent);

        IntentFilter intentFilter = new IntentFilter(myBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(scanModeReceiver,intentFilter);
    }

    BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int modeValue = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                if(modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE){
                    Log.d(TAG, "SCAN MODE RECEIVER : DEVICE IS NOT IN SCAN MODE BUT CAN RECEIVE CONNECTION");
                }
                if(modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    Log.d(TAG, "SCAN MODE RECEIVER : DEVICE IS IN DISCOVERABLE MODE");
                }
                if(modeValue == BluetoothAdapter.SCAN_MODE_NONE){
                    Log.d(TAG, "SCAN MODE RECEIVER : DEVICE CANNOT RECEIVE CONNECTION AND IS NOT DISCOVERING");
                }
            }
        }
    };
}
