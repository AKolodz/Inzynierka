package kolodziejczyk.olek.inzynierka.emergencyapp;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.text_view_login)
    TextView tvLogin;
    @InjectView(R.id.text_view_password)
    TextView tvPassword;
    @InjectView(R.id.edit_text_login)
    EditText etLogin;
    @InjectView(R.id.edit_text_password)
    EditText etPassword;
    @InjectView(R.id.button_login)
    Button bLogin;

    public enum FragmentToLaunch{VIEW, EDIT, ADD}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setTitle(R.string.title_login);

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredLogin=etLogin.getText().toString();
                String enteredPassword=etPassword.getText().toString();
                String correctLogin="Admin";
                String correctPassword="Password1";

                if (enteredLogin.equals(correctLogin)&& enteredPassword.equals(correctPassword)){
                    Intent intent= new Intent(getApplicationContext(),HomeScreen.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(getApplicationContext(),"Hello "+correctLogin,Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Wrong data, please try again",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
