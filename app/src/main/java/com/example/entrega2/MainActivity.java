package com.example.entrega2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private String estado;
    private CharSequence usuario;
    private CharSequence contra;
    private String[] token = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        solicitarPermisos();

        Button bSesion = (Button) findViewById(R.id.bIniciarSesion);
        estado = "prin";
        bSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInicioSesion();
            }
        });
        Button bRegistro = (Button) findViewById(R.id.bRegistrar);
        bRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegistro();
            }
        });
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        token[0] = task.getResult();
                    }
                });
    }

    private void solicitarPermisos(){
        //Se piden los permisos necesarios para enviar notificaciones y acceder a la camara
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
              PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new
              String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 11);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) !=
              PackageManager.PERMISSION_GRANTED) {
         ActivityCompat.requestPermissions(this, new
               String[]{Manifest.permission.CAMERA}, 11);
        }
    }

    private void openInicioSesion(){
        estado = "inicioSesion";
        setContentView(R.layout.iniciosesion);
        EditText usuarioI = (EditText) findViewById(R.id.eTusuarioI);
        EditText contraI = (EditText) findViewById(R.id.eTcontraI);
        Button bInicioS = (Button) findViewById(R.id.bInicioSI);
        //Se pone lo que estaba escrito en caso de que hubiese algo
        usuarioI.setText(usuario);
        contraI.setText(contra);
        bInicioS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion(usuarioI.getText().toString(), contraI.getText().toString());
            }
        });
    }

    private void openRegistro(){
        estado = "registro";
        setContentView(R.layout.registro);
        EditText usuarioR = (EditText) findViewById(R.id.eTusuarioR);
        EditText contraR = (EditText) findViewById(R.id.eTcontraR);
        usuarioR.setText(usuario);
        contraR.setText(contra);
        Button bResgistro = (Button) findViewById(R.id.bRegistrarR);
        bResgistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrar(usuarioR.getText().toString(), contraR.getText().toString());
            }
        });
    }

    private void registrar(String usuario, String contra){
        //se inserta en la tabla el nuevo usuario con su contraseña y su token
        Data datos = new Data.Builder()
                .putInt("metodo",0)
                .putString("usuario",usuario)
                .putString("contra",contra)
                .putString("token", token[0])
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(UsuarioBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        Integer result = status.getOutputData().getInt("resultado",0);
                        if(result == 0){
                            //El nombre de usuario ya existe
                            DialogoUsuarioOcupado d = new DialogoUsuarioOcupado();
                            d.show(getSupportFragmentManager(), "1");
                        }else{
                            //Se accede a la página principal
                            Intent i = new Intent (this, Token.class);
                            i.putExtra("usuario",usuario);
                            i.putExtra("token", token[0]);
                            startActivity(i);
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void iniciarSesion(String usuario, String contra){
        //Se busca en la base de datos el usuario con la contraseña introducida
        Data datos = new Data.Builder()
                .putInt("metodo",1)
                .putString("usuario",usuario)
                .putString("contra",contra)
                .putString("token",token[0])
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(UsuarioBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        Integer result = status.getOutputData().getInt("resultado",0);
                        if(result == 0){
                            //El usuario o la contraseña son incorrectos
                            DialogoMalInicio d = new DialogoMalInicio();
                            d.show(getSupportFragmentManager(), "1");
                        }else{
                            //Si se inicia sesion correctamente se adjudica al usuario el token del móvil en el que se ha iniciado sesión
                            actualizarToken(usuario, contra);
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void actualizarToken(String usuario, String contra){
        Data datos = new Data.Builder()
                .putInt("metodo",2)
                .putString("usuario",usuario)
                .putString("contra",contra)
                .putString("token",token[0])
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(UsuarioBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        //Se accede a la página principal
                        Integer result = status.getOutputData().getInt("resultado",0);
                        Intent i = new Intent (this, Token.class);
                        i.putExtra("usuario",usuario);
                        i.putExtra("token",token[0]);
                        startActivity(i);
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //Se guardan todas las variables que se han escrito y el estado en el que se encuentra la actividad
        savedInstanceState.putString("estado", estado);
        if (estado.equals("inicioSesion")){
            EditText usuarioI = (EditText) findViewById(R.id.eTusuarioI);
            EditText contraI = (EditText) findViewById(R.id.eTcontraI);
            savedInstanceState.putString("usuario", usuarioI.getText().toString());
            savedInstanceState.putString("contra", contraI.getText().toString());
        }else if(estado.equals("registro")){
            EditText usuarioR = (EditText) findViewById(R.id.eTusuarioR);
            EditText contraR = (EditText) findViewById(R.id.eTcontraR);
            savedInstanceState.putString("usuario", usuarioR.getText().toString());
            savedInstanceState.putString("contra", contraR.getText().toString());

        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Se recuperan todos los datos guardados
        estado = savedInstanceState.getString("estado");
        usuario = savedInstanceState.getString("usuario");
        contra = savedInstanceState.getString("contra");
    }

    protected void onStart() {
        super.onStart();
        if (estado.equals("inicioSesion")){
            openInicioSesion();
        }else if(estado.equals("registro")){
            openRegistro();
        }
    }

    protected void onResume(){
        super.onResume();
        //Dependiendo del estado en el que estaba se va a un método o a otro
        if (estado.equals("inicioSesion")){
            openInicioSesion();
        }else if(estado.equals("registro")){
            openRegistro();
        }
    }

}