package kolodziejczyk.olek.inzynierka.bluetoothtutorial;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BtDeviceList extends ListActivity {

    private BroadcastReceiver mReceiver=null;
    IntentFilter filter=null;
    BluetoothAdapter myBluetoothAdapter=null;
    ArrayAdapter<String> ArrayBluetooth=null;
    static String MAC_ADDRESS=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayBluetooth = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        myBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        Intent intent=getIntent();
        String whatToShow=intent.getExtras().getString(MainActivity.VIEW);




        switch(whatToShow){
            case "paired":
                getPairedDevices();
                break;
            case "discovered":
                getDiscoveredDevices();
                myBluetoothAdapter.cancelDiscovery();
                myBluetoothAdapter.startDiscovery();
                break;
        }

        setListAdapter(ArrayBluetooth);
    }

    private void getDiscoveredDevices() {

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    ArrayBluetooth.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
        filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver,filter);

    }

    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices=myBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size()>0){
            for(BluetoothDevice device:pairedDevices){
                String deviceName=device.getName();
                String deviceAddress=device.getAddress();
                ArrayBluetooth.add(deviceName+"\n"+deviceAddress);
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        myBluetoothAdapter.cancelDiscovery();
        String generalInfo=((TextView)v).getText().toString();
        String macAddress=generalInfo.substring(generalInfo.length()-17);

        Intent intentMacReturn=new Intent();
        intentMacReturn.putExtra(MAC_ADDRESS,macAddress);
        setResult(RESULT_OK,intentMacReturn);
        finish();
    }
}
