package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HomeScreen extends AppCompatActivity {

    private Intent intent=null;
    private boolean bluetoothOn=false;
    private BluetoothAdapter bluetoothAdapter=null;

    public static final String FIRST_RUN_EXTRA = "If it's first run";

    @InjectView(R.id.button_emergency)
    Button bEmergency;
    @InjectView(R.id.button_medicaments)
    Button bMedicaments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ButterKnife.inject(this);


        bEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBtState();
                if(bluetoothOn){
                    intent= new Intent(getApplicationContext(),EmergencyDetailActivity.class);
                    intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA,MainActivity.FragmentToLaunch.VIEW);
                    intent.putExtra(HomeScreen.FIRST_RUN_EXTRA,true);
                }else{
                    intent= new Intent(getApplicationContext(),BluetoothListActivity.class);
                }
                startActivity(intent);
            }
        });

        bMedicaments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Coming Soon", Toast.LENGTH_SHORT).show();
                //intent= new Intent(getApplicationContext(),MedicamentsList.class);
                //startActivity(intent);
            }
        });
    }

    private void checkBtState() {
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            Toast.makeText(getBaseContext(),R.string.lack_of_BTmodule,Toast.LENGTH_LONG).show();
        }else{
            if(bluetoothAdapter.isEnabled()){
                bluetoothOn=true;
            }else{
                bluetoothOn=false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }else if(id==R.id.bluetooth){
            Intent intent=new Intent(this,BluetoothListActivity.class);
            startActivity(intent);
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }
}
