package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HomeScreen extends AppCompatActivity {

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
                Intent intent=new Intent(getApplicationContext(),EmergencyDetailActivity.class);
                intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA,MainActivity.FragmentToLaunch.VIEW);
                startActivity(intent);
            }
        });

        bMedicaments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent= new Intent(getApplicationContext(),MedicamentsList.class);
                //startActivity(intent);
            }
        });
    }
}
