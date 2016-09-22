package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BluetoothListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        createAndAddFragment();

    }

    private void createAndAddFragment() {
        setTitle("BLUETOOTH");
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        BluetoothListFragment bluetoothListFragment=new BluetoothListFragment();
        fragmentTransaction.add(R.id.bluetooth_dev_list_container,bluetoothListFragment);
        fragmentTransaction.commit();
    }


}

