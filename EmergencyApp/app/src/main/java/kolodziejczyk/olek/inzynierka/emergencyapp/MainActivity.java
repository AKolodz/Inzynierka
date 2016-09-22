package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    private Intent intent=null;

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
                    intent=new Intent(getApplicationContext(),HomeScreen.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(getApplicationContext(),"Hello "+correctLogin,Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Wrong data, please try again",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
