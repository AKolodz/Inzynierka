package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothListFragment extends Fragment {


    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int SUCCESS_CONNECT =0 ;
    private static final int MESSAGE_READ = 1;
    @InjectView(R.id.switch_bluetooth_on_off)
    Switch sTurnOnOff;
    @InjectView(R.id.button_connection_new)
    Button bConnect;
    @InjectView(R.id.list_view_BT_devices)
    ListView lvDevices;

    ArrayAdapter<String> devicesListAdapter;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> devicesBT;
    IntentFilter filter;
    BroadcastReceiver receiver;
    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    ConnectedThread connectedThread=new ConnectedThread((BluetoothSocket) msg.obj);
                    Toast.makeText(getActivity().getBaseContext(),"CONNECT",Toast.LENGTH_SHORT).show();
                    String s="succesfully connected";
                    connectedThread.write(s.getBytes());
                    break;
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    String string=new String(readBuff);
                    Toast.makeText(getActivity().getBaseContext(),string,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private static final int REQUEST_ENABLE_BT = 1;

    public BluetoothListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentLayout=inflater.inflate(R.layout.fragment_bluetooth_list,container,false);
        ButterKnife.inject(this,fragmentLayout);

        init();

        checkBluetoothStateAndDiscover();

        return fragmentLayout;
    }

    private void checkBluetoothStateAndDiscover() {
        if(bluetoothAdapter==null){
            Toast.makeText(getActivity().getBaseContext(),"No bluetooth detected",Toast.LENGTH_SHORT).show();
        }else{
            if(!bluetoothAdapter.isEnabled()){
                turnOnBt();
            }
            getPairedDevices();
            startDiscovery();
        }
    }

    private void startDiscovery() {
        bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();
    }

    private void turnOnBt() {
        Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
    }

    private void getPairedDevices() {
        devicesBT=bluetoothAdapter.getBondedDevices();
        if(devicesBT.size()>0){
            for(BluetoothDevice device:devicesBT){
                pairedDevices.add(device.getName());
            }
        }
    }

    private void init() {
        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(bluetoothAdapter.isDiscovering()){
                   bluetoothAdapter.cancelDiscovery();
                }
                if(devicesListAdapter.getItem(i).contains("Paired")){
                    Toast.makeText(getActivity().getBaseContext(),"Already paired",Toast.LENGTH_SHORT).show();

                    BluetoothDevice selectedDevice=devices.get(i) ;

                    ConnectThread connect=new ConnectThread(selectedDevice);
                    connect.start();
                }else{
                    Toast.makeText(getActivity().getBaseContext(),"Device is not paired",Toast.LENGTH_SHORT).show();
                }
            }
        });
        devicesListAdapter=new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_list_item_1,0);
        pairedDevices=new ArrayList<String>();
        lvDevices.setAdapter(devicesListAdapter);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        devices=new ArrayList<BluetoothDevice>();

        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    String pairedString="";
                    for(int numberOfPairedDevice=0;numberOfPairedDevice<pairedDevices.size();numberOfPairedDevice++){
                        if(device.getName().equals(pairedDevices.get(numberOfPairedDevice))){
                            pairedString+="(Paired)";
                            break;
                        }
                    }

                    devicesListAdapter.add(device.getName()+" "+pairedString+" "+"\n"+device.getAddress());
                    Toast.makeText(getActivity().getBaseContext(),"GOT IT",Toast.LENGTH_SHORT).show();

                }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                        Toast.makeText(getActivity().getBaseContext(),"Discovering!",Toast.LENGTH_SHORT).show();
                }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                }else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(bluetoothAdapter.getState()==bluetoothAdapter.STATE_OFF){
                        turnOnBt();
                    }
                }
            }
        };
        filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(receiver,filter);
        filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        getActivity().registerReceiver(receiver,filter);
        filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(receiver,filter);
        filter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(receiver,filter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_CANCELED){
            Toast.makeText(getActivity().getBaseContext(),"Bluetooth must be enabled to continue",Toast.LENGTH_SHORT).show();
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
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
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
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    buffer=new byte[1024];
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

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
