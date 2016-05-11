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
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class Register extends AppCompatActivity implements View.OnClickListener{
   /* Button bRegister;
    EditText etEmail, etVerCode, etUsername, etPassword;*/
    @InjectView(R.id.buttonRegister)
    Button bRegister;
    @InjectView(R.id.regPasswordEdittext)
    EditText etPassword;
    @InjectView(R.id.regUsernameEdittext)
    EditText etUsername;
    @InjectView(R.id.emailEdittext)
    EditText etEmail;
    @InjectView(R.id.verCodeEdittext)
    EditText etVerCode;
    @InjectView(R.id.buttonBack)
    Button bBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        /*etUsername= (EditText) findViewById(R.id.regUsernameEdittext);
        etEmail= (EditText) findViewById(R.id.emailEdittext);
        etPassword= (EditText) findViewById(R.id.regPasswordEdittext);
        etVerCode=(EditText) findViewById(R.id.verCodeEdittext);
        bRegister=(Button) findViewById(R.id.buttonRegister);*/
        ButterKnife.inject(this);

        bRegister.setOnClickListener(this);
        bBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.buttonRegister:
                Toast.makeText(this,"We're working on it",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,LoginScreen.class));
                break;
            case R.id.buttonBack:
                Toast.makeText(this,"As You wish",Toast.LENGTH_LONG).show();
                startActivity(new Intent(this,LoginScreen.class));
                break;
        }
    }
}
