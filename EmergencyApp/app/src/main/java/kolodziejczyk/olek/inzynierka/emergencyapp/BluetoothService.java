package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.UUID;

public class BluetoothService extends Service {

    private final IBinder myBinder=new MyBinder();
    private BluetoothAdapter bluetoothAdapter=null;
    private static String macAddress=null;
    private BluetoothDevice deviceToConnectWith=null;
    private ConnectThread connectThread=null;
    private ConnectedThread connectedThread=null;
    private SharedPreferences sharedPreferencesMacAddress;
    private SharedPreferences.Editor macAddressEditor;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "kolodziejczyk.olek";
    public static final String SHARED_PREFS_MAC_ADDRESS="kolodziejczyk.olek.inzynierka.emergencyapp.SharedPrefsMac";
    private static final int SUCCESS_CONNECT = 0;
    private static final int MESSAGE_READ = 9999;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){

                case SUCCESS_CONNECT:
                    Log.i(TAG,"Handler: SUCCESS CONNECT");
                    sharedPreferencesMacAddress=getSharedPreferences(EmergencyDetailActivity.SHARED_PREFS_FILENAME,0);
                    macAddressEditor=sharedPreferencesMacAddress.edit();
                    macAddressEditor.putString(BluetoothListFragment.SHARED_PREFS_MAC_ADDRESS,macAddress);
                    macAddressEditor.commit();
                    break;

                case MESSAGE_READ:
                    byte[] readBuff=(byte[]) msg.obj;
                    String receivedMsg=new String(readBuff);
                    Toast.makeText(getApplicationContext(),receivedMsg,Toast.LENGTH_SHORT).show();
                    Log.i(TAG,"Handler: MESSAGE");
                    break;
            }
        }
    };

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"OnCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        macAddress=intent.getExtras().getString(BtDeviceList.MAC_ADDRESS);

        if(macAddress==null){
            sharedPreferencesMacAddress=getSharedPreferences(EmergencyDetailActivity.SHARED_PREFS_FILENAME,0);
            macAddress=sharedPreferencesMacAddress.getString(BluetoothService.SHARED_PREFS_MAC_ADDRESS,null);
            Log.i(TAG,"onStartCommand "+macAddress);
            if(macAddress==null){
                Log.i(TAG,"Saved macAddress is incorrect"+macAddress); //go to BluetoothListActivity and select another device
                Intent intentGetMac=new Intent(BluetoothService.this,BluetoothListActivity.class);
                startActivity(intentGetMac);
                stopSelf();
            }
        }else{
            //do nothing
        }
        deviceToConnectWith=bluetoothAdapter.getRemoteDevice(macAddress);
        bluetoothAdapter.cancelDiscovery();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"New Thread");
                connectThread=new ConnectThread(deviceToConnectWith);
                connectThread.start();
            }
        };
        Thread thread=new Thread(runnable);
        thread.start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy()");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        BluetoothService getService(){
            return BluetoothService.this;
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
                    //isConnected=false;
                    Toast.makeText(getBaseContext(),"Error occurred: "+connectException,Toast.LENGTH_LONG).show();
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
                Toast.makeText(getBaseContext(),"Device disconnected",Toast.LENGTH_SHORT).show();
                //isConnected=false;
                //bConnectToPairedDevices.setText("Connect to paired device");
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
