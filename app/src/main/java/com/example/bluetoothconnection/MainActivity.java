package com.example.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String APP_NAME = "NKEY";
    private static final UUID MY_UUID = UUID.fromString("09103f87-07c1-4ef4-a6a5-f744bcd72253");

    public ArrayList<BluetoothDevice> myBluetoothDevices = new ArrayList<>();
    public DeviceListAdapter myDeviceListAdapter;
    ListView lvNewDevices;

    int requestCodeForEnable = 1;

    Button btnONOFF;
    Button btnDiscover;

    BluetoothAdapter myBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnDiscover = (Button) findViewById(R.id.btnDiscoverUnpairedDevices);

        lvNewDevices = (ListView) findViewById(R.id.newDevicesListView);
        myBluetoothDevices = new ArrayList<>();

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableEnableBluetooth();
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevices();
            }
        });
    }

    private void disableEnableBluetooth(){
        if(myBluetoothAdapter == null){
            Log.d(TAG, "Does not support bluetooth");
        } else {
            if(!myBluetoothAdapter.isEnabled()){
                Log.d(TAG,"ENABLING BLUETOOTH ...");
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, 1);

                IntentFilter bluetoothIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(enableDisableBluetoothReceiver, bluetoothIntent);

            }
            if(myBluetoothAdapter.isEnabled()){
                Log.d(TAG, "DISABLING BLUETOOTH...");
                myBluetoothAdapter.disable();

                IntentFilter bluetoothIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(enableDisableBluetoothReceiver, bluetoothIntent);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == requestCodeForEnable){
            if(resultCode == RESULT_OK){
                Log.d(TAG, "ALLOWING TURNING BT ON");
                enableDisableDiscoverability();
            } else if(resultCode == RESULT_CANCELED){
                Log.d(TAG, "DENIED TURNING BLUETOOTH ON");
            }
        }
    }

    private void enableDisableDiscoverability(){
        Log.d(TAG, "MAKING DEVICE DISCOVERABLE...");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 20);
        startActivity(discoverableIntent);

        IntentFilter discoverabilityIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(acceptDeclineDiscoverabilityReceiver, discoverabilityIntentFilter);
    }

    private void discoverDevices(){
        Log.d(TAG, "LOOKING FOR UNPAIRED DEVICES");

        if(myBluetoothAdapter.isDiscovering()){
            myBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "CANCEL DISCOVERING");

            myBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(discoverReceiver, discoverDevicesIntent);
        }
        if(!myBluetoothAdapter.isDiscovering()){

            myBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(discoverReceiver, discoverDevicesIntent);
        }
    }

    BroadcastReceiver enableDisableBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, myBluetoothAdapter.ERROR);

                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "BLUETOOTH IS OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "BLUETOOTH IS TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "BLUETOOTH IS ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "BLUETOOTH IS TURNING ON");
                        break;
                }
            }
        }
    };

    BroadcastReceiver acceptDeclineDiscoverabilityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "DISCOVERABILITY ENABLED");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "DISCOVERABILITY ENABLED. ABLE TO RECEIVE CONNECTIONS");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "DISCOVERABILITY DISABLED. ABLE TO RECEIVE CONNECTIONS");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "CONNECTED");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "CONNECTING...");
                }
            }
        }
    };

    BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "ACTION FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                myBluetoothDevices.add(device);
                Log.d(TAG, "ON RECEIVE" + device.getName());
                myDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, myBluetoothDevices);
                lvNewDevices.setAdapter(myDeviceListAdapter);
            }
        }
    };

}
