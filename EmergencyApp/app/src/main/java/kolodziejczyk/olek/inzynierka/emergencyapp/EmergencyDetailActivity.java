package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class EmergencyDetailActivity extends AppCompatActivity {

    public static final String NEW_OBJECT_EXTRA = "New Emergency Object";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);
        createAndAddFragment();
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

    private void createAndAddFragment(){
        Intent intent=getIntent();
        MainActivity.FragmentToLaunch fragmentToLaunch= (MainActivity.FragmentToLaunch) intent.getSerializableExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA);
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        switch(fragmentToLaunch) {
            case VIEW:
                setTitle(R.string.title_emergency_view);
                EmergencyViewFragment emergencyViewFragment = new EmergencyViewFragment();
                fragmentTransaction.add(R.id.emergency_object_container,emergencyViewFragment);
                break;
            case ADD:
                setTitle(R.string.title_emergency_add);
                EmergencyEditFragment emergencyAddFragment= new EmergencyEditFragment();

                Bundle bundle=new Bundle();
                bundle.putBoolean(NEW_OBJECT_EXTRA,true);
                emergencyAddFragment.setArguments(bundle);

                fragmentTransaction.add(R.id.emergency_object_container,emergencyAddFragment);
                break;
            case EDIT:
                setTitle(R.string.title_emergency_edit);
                EmergencyEditFragment emergencyEditFragment=new EmergencyEditFragment();
                fragmentTransaction.add(R.id.emergency_object_container,emergencyEditFragment);
                break;
        }

        fragmentTransaction.commit();
    }
}
