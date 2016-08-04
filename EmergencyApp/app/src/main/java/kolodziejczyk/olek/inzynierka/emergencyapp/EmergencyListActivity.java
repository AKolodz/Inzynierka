package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EmergencyListActivity extends AppCompatActivity {

    public final static String FRAGMENT_TO_LOAD_EXTRA="kolodziejczyk.olek.inzynierka.emergencyapp.Fragment_To_Load";

    @InjectView(R.id.button_add_pattern)
    Button bAddPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_list);
        setTitle(R.string.title_emergency_list);
        ButterKnife.inject(this);

        bAddPattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),EmergencyDetailActivity.class);
                intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.ADD);
                startActivity(intent);
            }
        });
    }

}
