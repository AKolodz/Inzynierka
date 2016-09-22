package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothListFragment extends Fragment {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECTION = 2;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final int MESSAGE_READ = 9999;
    private static final int SUCCESS_CONNECT = 0;
    public static final String WHAT_TO_DO="kolodziejczyk.olek.inzynierka.emergencyapp.whattodo";


    private BluetoothAdapter bluetoothAdapter=null;
    private boolean isConnected=false;
    private static String macAddress=null;
    private BluetoothDevice deviceToConnectWith=null;
    private ConnectThread connectThread=null;
    private ConnectedThread connectedThread=null;

    public enum BtAction{GET_PAIRED,DISCOVER_NEW}

    @InjectView(R.id.button_paired)
    Button bConnectToPairedDevices;
    @InjectView(R.id.button_discover)
    Button bDiscoverNewDevices;
    @InjectView(R.id.button_confirmation)
    Button bConfirmation;
    @InjectView(R.id.text_view_device_information)
    TextView tvDeviceInformation;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    Toast.makeText(getActivity().getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    byte[] readBuff=(byte[]) msg.obj;
                    String receivedMsg=new String(readBuff);
                    Toast.makeText(getActivity().getApplicationContext(),receivedMsg,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public BluetoothListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentLayout=inflater.inflate(R.layout.fragment_bluetooth_list,container,false);
        ButterKnife.inject(this,fragmentLayout);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        checkBtState();

        bConnectToPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected){
                    connectThread.cancel();
                }else{
                    Intent intentList=new Intent(getActivity().getBaseContext(),BtDeviceList.class);
                    intentList.putExtra(BluetoothListFragment.WHAT_TO_DO,BtAction.GET_PAIRED);
                    startActivityForResult(intentList,REQUEST_CONNECTION);
                }
            }
        });

        bDiscoverNewDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentList=new Intent(getActivity().getBaseContext(),BtDeviceList.class);
                intentList.putExtra(BluetoothListFragment.WHAT_TO_DO,BtAction.DISCOVER_NEW);
                startActivityForResult(intentList,REQUEST_CONNECTION);
            }
        });

        bConfirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(),EmergencyDetailActivity.class);
                intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.VIEW);
                //intent.putExtra(BtDeviceList.MAC????,macAddress); tam jeśli jest puste to prosimy o wybór urządzenia, jesli nie to podtrzymujemy połączenie
                startActivity(intent);
                getActivity().finish();
            }
        });

        return fragmentLayout;
    }

    private void checkBtState() {
        if(bluetoothAdapter==null){
            Toast.makeText(getActivity().getBaseContext(),R.string.lack_of_BTmodule,Toast.LENGTH_LONG).show();
            getActivity().finish();
        }else{
            if(!bluetoothAdapter.isEnabled()){
                turnOnBluetooth();
            }
        }
    }

    private void turnOnBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    @Override
    public void onPause() {
        //getActivity().unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data); //should I comment this line?
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode== Activity.RESULT_OK){
                    Toast.makeText(getActivity().getBaseContext(),"Bluetooth is activated!",Toast.LENGTH_SHORT).show();
                }else if(resultCode==Activity.RESULT_CANCELED){
                    Toast.makeText(getActivity().getBaseContext(),"Bluetooth must be enable!",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }else{
                    Toast.makeText(getActivity().getBaseContext(),"Bluetooth is NOT activated, try again",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            case REQUEST_CONNECTION:
                if(resultCode==Activity.RESULT_OK){
                    macAddress=data.getExtras().getString(BtDeviceList.MAC_ADDRESS);
                    deviceToConnectWith=bluetoothAdapter.getRemoteDevice(macAddress);
                    bluetoothAdapter.cancelDiscovery();

                    connectThread=new ConnectThread(deviceToConnectWith);
                    connectThread.start();

                    isConnected=true;
                    bConnectToPairedDevices.setText("Disconnect");
                    Toast.makeText(getActivity().getBaseContext(),"Connecting: "+macAddress,Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity().getBaseContext(),"Obtaining MAC address FAILED",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                    isConnected=false;
                    Toast.makeText(getActivity().getBaseContext(),"Error occurred: "+connectException,Toast.LENGTH_LONG).show();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            mHandler.obtainMessage(SUCCESS_CONNECT).sendToTarget();
            manageConnectedSocket(mmSocket);
        }



        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                Toast.makeText(getActivity().getBaseContext(),"Device disconnected",Toast.LENGTH_SHORT).show();
                isConnected=false;
                bConnectToPairedDevices.setText("Connect to paired device");
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        connectedThread=new ConnectedThread(mmSocket);
        connectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if(true){
                        // Send the obtained bytes to the UI activity
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
