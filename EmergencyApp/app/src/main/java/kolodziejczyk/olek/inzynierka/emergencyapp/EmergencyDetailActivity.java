package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class EmergencyDetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);
        createAndAddFragment();

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
