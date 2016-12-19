package kolodziejczyk.olek.inzynierka.emergencyapp;

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

        clickable();

    }

    private void clickable() {
        bEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent= new Intent(getApplicationContext(),BluetoothListActivity.class);
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

}
