package pat.olek.emg_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginScreen extends AppCompatActivity implements View.OnClickListener{

    /*Button bLogin, bRegistration;
    EditText etUsername, etPassword;*/
    @InjectView(R.id.usernameEdittext)
    EditText etUsername;
    @InjectView(R.id.passwordEdittext)
    EditText etPassword;
    @InjectView(R.id.buttonLogin)
    Button bLogin;
    @InjectView(R.id.buttonRegistration)
    Button bRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        /*etUsername= (EditText) findViewById(R.id.usernameEdittext);
        etPassword= (EditText) findViewById(R.id.passwordEdittext);
        bLogin= (Button) findViewById(R.id.buttonLogin);
        bRegistration= (Button) findViewById(R.id.buttonRegistration);*/
        ButterKnife.inject(this);

        bLogin.setOnClickListener(this);
        bRegistration.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonLogin:

                if(etUsername.getText().toString().equals("admin1") && etPassword.getText().toString().equals("password1")) {
                    startActivity(new Intent(this, HomeScreen.class));
                }else{
                    Toast.makeText(this,"Wrong data. Try again, Piglet",Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.buttonRegistration:
                startActivity(new Intent(this,Register.class));


                break;
        }
    }
}
