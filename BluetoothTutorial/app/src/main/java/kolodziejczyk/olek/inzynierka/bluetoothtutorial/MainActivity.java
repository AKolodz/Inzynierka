package kolodziejczyk.olek.inzynierka.bluetoothtutorial;

import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {

    public static final String VIEW = "kolodziejczyk.olek.inzynierka.bluetoothtutorial.View";
    private static final int SUCCESS_CONNECT =0 ;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECTION = 2;
    private static final int MESSAGE_READ = 9999;


    BluetoothAdapter myBluetoothAdapter = null;
    BluetoothDevice myDevice = null;
    BluetoothSocket mySocket = null;
    ConnectThread connectThread=null;
    ConnectedThread connectedThread=null;

    boolean connection=false;
    private static String MAC=null;
    UUID MY_UUID=UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @InjectView(R.id.bConnect)
    Button btnConnect;
    @InjectView(R.id.bDiscover)
    Button btnDiscover;
    @InjectView(R.id.tvIndicator)
    TextView tvIndicator;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    ConnectedThread connectedThread=new ConnectedThread((BluetoothSocket) msg.obj);
                    Toast.makeText(getBaseContext(),"CONNECT",Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    tvIndicator.setText("MESSAGE READ");
                    byte[] readBuff=(byte[]) msg.obj;
                    String receivedMsg=new String(readBuff);
                    Toast.makeText(getApplicationContext(),receivedMsg,Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        myBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        if(myBluetoothAdapter==null){//nie jest wspierany bluetooth
            Toast.makeText(getBaseContext(),"Bluetooth is not supported",Toast.LENGTH_SHORT).show();
        }else{ //jest wspierany bluetooth
            if(!myBluetoothAdapter.isEnabled()){ //nie jest włączony
                turnOnBt();
            }
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connection){
                    connectThread.cancel();

                }else{
                    Intent intentList=new Intent(MainActivity.this,BtDeviceList.class);
                    intentList.putExtra(MainActivity.VIEW,"paired");
                    startActivityForResult(intentList,REQUEST_CONNECTION);
                }
            }
        });
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentList=new Intent(MainActivity.this,BtDeviceList.class);
                intentList.putExtra(MainActivity.VIEW,"discovered");
                startActivityForResult(intentList,REQUEST_CONNECTION);
            }
        });
    }

    private void turnOnBt() {
        Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode== Activity.RESULT_OK){
                    Toast.makeText(getBaseContext(),"Bluetooth is activated",Toast.LENGTH_SHORT).show();

                }else if(resultCode==Activity.RESULT_CANCELED){
                    Toast.makeText(getBaseContext(),"Bluetooth must be enabled!",Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(getBaseContext(),"Bluetooth is not activated",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_CONNECTION:
                if(resultCode==Activity.RESULT_OK){
                    MAC=data.getExtras().getString(BtDeviceList.MAC_ADDRESS);
                    myDevice=myBluetoothAdapter.getRemoteDevice(MAC);
                    myBluetoothAdapter.cancelDiscovery();

                    connectThread=new ConnectThread(myDevice);
                    connectThread.start();

                    connection=true;
                    btnConnect.setText("Disconnect");
                    Toast.makeText(getBaseContext(),"Connected: "+MAC,Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getBaseContext(),"Obtaining MAC address FAILED",Toast.LENGTH_SHORT).show();
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
            myBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                    connection=false;
                    Toast.makeText(getBaseContext(),"Error occurred: "+connectException,Toast.LENGTH_LONG).show();

                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket);
            manageConnectedSocket(mmSocket);

        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                Toast.makeText(getBaseContext(),"Device disconnected",Toast.LENGTH_SHORT).show();
                connection=false;
                btnConnect.setText("Connect to paired");
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
            connectedThread = new ConnectedThread(socket);
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
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    buffer= new byte[1024];
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