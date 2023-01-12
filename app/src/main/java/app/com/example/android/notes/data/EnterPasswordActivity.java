package app.com.example.android.notes.data;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import app.com.example.android.notes.R;
import app.com.example.android.notes.notes;

public class EnterPasswordActivity extends AppCompatActivity {

    EditText editText;
    Button button;

    String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);

        // Load password
        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        password = settings.getString("password","");

        editText = (EditText)findViewById(R.id.editText);
        button = (Button)findViewById(R.id.button1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();

                if(text.equals(password)){
                    // enter the app
                    Intent intent = new Intent(getApplicationContext(), notes.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(EnterPasswordActivity.this,"Wrong password",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

