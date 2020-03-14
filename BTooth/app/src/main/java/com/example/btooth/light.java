package com.example.btooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class light extends AppCompatActivity {


    private static final String TAG = "LinkedBT"; //<-<- PART A MODIFY >->->
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    // Declaracion of ListView
    ListView idList;
    // Declaration of fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //---------------------------------
        VerificationBT();

        // Initialize the array that will contain the list of linked bluetooth devices
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.number);
        // Presents the linked devices in the ListView
        idList = (ListView) findViewById(R.id.idList);
        idList.setAdapter(mPairedDevicesArrayAdapter);
        idList.setOnItemClickListener(mDeviceClickListener);
        // get the adapter Bluetooth local adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices and add 'pairedDevices'
        Set <BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Add a previous device paired to the array
        if (pairedDevices.size() > 0)
        {
            //IN CASE OF ERROR READ THE PREVIOUS EXPLANATION
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    // Set an (on-click) for the list
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            // Get the MAC address of the device, which is the last 17 characters in the view
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an attempt to start the next activity
            // while taking an EXTRA_DEVICE_ADDRESS that is the MAC address.
            Intent i = new Intent(light.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);
        }
    };

    private void VerificationBT() {
        // Check that the device has Bluetooth and that it is turned on.
        mBtAdapter= BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter==null) {
            Toast.makeText(getBaseContext(), "The device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth On!");
            } else {
                //Ask the user to activate Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }
}
