package com.example.entrega2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Token extends AppCompatActivity {

    private String token;
    private String usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //Se recoge el usuario con el que se ha iniciado sesion
            usuario = extras.getString("usuario");
            token = extras.getString("token");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.token);
        EditText t = (EditText) findViewById(R.id.tokenT);
        t.setText(token);
        Button b = (Button) findViewById(R.id.bToken);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accederPagPrincipal();
            }
        });
    }

    private void accederPagPrincipal(){
        Intent i = new Intent (this, PagPrincipal.class);
        i.putExtra("usuario",usuario);
        startActivity(i);
    }
}
