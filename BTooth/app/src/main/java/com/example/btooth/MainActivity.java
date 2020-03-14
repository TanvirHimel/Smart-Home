package com.example.btooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button idOn, idOff,idDisconnector;
    TextView idBufferIn;
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread MyConnectionBT;
    private StringBuilder DataStringIN = new StringBuilder();
    // Unique service identifier - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for the MAC address
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idOn = (Button) findViewById(R.id.idOn);
        idOff = (Button) findViewById(R.id.idOff);
        idDisconnector = (Button) findViewById(R.id.idDisconnector);
        idBufferIn = (TextView) findViewById(R.id.idBufferIn);
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        idBufferIn.setText("Data: " + dataInPrint);//<-<- Part to modify >->->
                        DataStringIN.delete(0, DataStringIN.length());
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        // Configuration onClick listeners for buttons
        idOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConnectionBT.write("1");
            }
        });

        idOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConnectionBT.write("0");
            }
        });

        idDisconnector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btSocket != null) try {
                    btSocket.close();
                }
                catch (IOException e){
                    Toast.makeText(getBaseContext(),"ERROR",Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        //creates a secure output connection for the device
        //using the UUID service
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Get the MAC address from DeviceListActivity via intent
        Intent intent = getIntent();
        //Get the MAC address from DeviceListActivity via EXTRA
        address = intent.getStringExtra(light.EXTRA_DEVICE_ADDRESS);
        //Set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "The creation of the Socket failed", Toast.LENGTH_LONG).show();
        }
        // Establish the connection with the Bluetooth socket.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConnectionBT = new ConnectedThread(btSocket);
        MyConnectionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // When you exit the application this part allows
            // Do not leave the socket open
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Verify that the Bluetooth Bluetooth device is available and request that it be activated if it is disabled
   /* private void VerificationBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "The device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }*/



    public class ConnectedThread extends Thread{
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // It stays in listening mode to determine the data entry
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the data obtained to the event via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //Frame Send
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //if it is not possible to send data the connection is closed
                Toast.makeText(getBaseContext(), "The connection failed", Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }
}

