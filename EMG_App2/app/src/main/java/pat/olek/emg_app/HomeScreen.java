package pat.olek.emg_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HomeScreen extends AppCompatActivity implements View.OnClickListener {

    /*Button bTraining, bHistory, bLogout;*/
    @InjectView(R.id.buttonTraining)
    Button bTraining;
    @InjectView(R.id.buttonHistory)
    Button bHistory;
    @InjectView(R.id.buttonLogout)
    Button bLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        /*bTraining=(Button)findViewById(R.id.buttonTraining);
        bHistory=(Button)findViewById(R.id.buttonHistory);
        bLogout=(Button)findViewById(R.id.buttonLogout);*/
        ButterKnife.inject(this);

        bTraining.setOnClickListener(this);
        bHistory.setOnClickListener(this);
        bLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.buttonTraining:

                break;
            case R.id.buttonHistory:

                break;

            case R.id.buttonLogout:
                startActivity(new Intent(this,LoginScreen.class));
                break;
        }
    }
}
