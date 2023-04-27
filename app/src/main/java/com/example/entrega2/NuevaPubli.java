package com.example.entrega2;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NuevaPubli extends AppCompatActivity {

    private String usuario;
    private CharSequence texto;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private Bitmap foto;
    private String fotoen64;
    private String idImagen;
    private Thread thread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //Se recoge el usuario que va a realizar la nueva publicación
            usuario = extras.getString("usuario");
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nuevapubli);
        EditText textoPubli = (EditText) findViewById(R.id.eEscribeT);
        Button bSacarFoto = (Button) findViewById(R.id.bSacarFoto);
        bSacarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sacarFoto();
            }
        });
        Button bNuevaPubli = (Button) findViewById(R.id.bPublicar);
        bNuevaPubli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cuando se pulsa el botón de publicar se recoge lo escrito y se llama al metodo correspondiente
                texto = textoPubli.getText().toString();
                try {
                    nuevaPublicacion();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //La actividad encargada de hacer la foto
        takePictureLauncher =
                registerForActivityResult(new
                        ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK &&
                            result.getData()!= null) {
                        //Se recoge el resultado y se muestra en un ImageView
                        Bundle bundle = result.getData().getExtras();
                        ImageView elImageView = findViewById(R.id.imageView);
                        foto = (Bitmap) bundle.get("data");
                        elImageView.setImageBitmap(foto);
                    } else {
                        Log.d("TakenPicture", "No photo taken");
                    }
                });

    }

    private void sacarFoto(){
        //Llama a la actividad que permite acceder a la camara y sacar la foto cuando se pulsa el botón
        Intent elIntentFoto= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(elIntentFoto);
    }



    private void nuevaPublicacion() throws JSONException, IOException {
        insertarNuevaFoto();
        //Se crea este bucle para que no llame al siguiente método hasta que el thread asíncrono termine
        while (thread.isAlive()){}
        insertarNuevaPublicacion();
        enviarNotificacion();
        //Se vuelve a la página principal
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void insertarNuevaFoto() throws JSONException, IOException {
        //Se crea un nuevo thread para poder insertar la imagen en la base de datos
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Se transforma el bitmap a un String en base64
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    foto.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] fototransformada = stream.toByteArray();
                    fotoen64 = Base64.encodeToString(fototransformada,Base64.DEFAULT);
                    //Se recoge el timestamp y junto al nombre del usuario se crea el id de la imagen, así es irrepetible
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    idImagen = timeStamp + usuario;
                    String parametros = "metodo=0&imagen=" + fotoen64 + "&id=" + idImagen;

                    String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/hfernandez026/WEB/publicacionesBd.php";
                    HttpURLConnection urlConnection = null;
                    URL destino = new URL(direccion);
                    urlConnection = (HttpURLConnection) destino.openConnection();
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(parametros.toString());
                    out.close();
                    int statusCode = 0;
                    statusCode = urlConnection.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    private void insertarNuevaPublicacion(){
        Data datos = new Data.Builder()
                .putInt("metodo",1)
                .putString("usuario",usuario)
                .putString("texto",texto.toString())
                .putString("imagen",idImagen)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(PublicacionBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        Integer result = status.getOutputData().getInt("resultado",0);
                        if(result == 0){
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void enviarNotificacion(){
        //Se pide el permiso necesario para realizar una notificación
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 11);
        }

        //Se crean los elementos necesarios para enviar la notificación
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "IdCanal");
        NotificationChannel canal = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canal = new NotificationChannel("IdCanal", "NombreCanal",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(canal);
        }

        builder.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Publicación")
                .setContentText("Has realizado una nueva publicación.")
                .setSubText("Información extra")
                .setVibrate(new long[]{0, 1000, 500, 1000});
        canal.setDescription("Descripción del canal");
        canal.enableLights(true);
        canal.setLightColor(Color.RED);
        canal.enableVibration(true);
        canal.setVibrationPattern(new long[]{0, 1000, 500, 1000});
        //Cuando se realiza la nueva publicación llega una notificación
        manager.notify(1, builder.build());
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (foto != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            foto.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] fototransformada = stream.toByteArray();
            fotoen64 = Base64.encodeToString(fototransformada,Base64.DEFAULT);
        }
        savedInstanceState.putString("foto",fotoen64);
        EditText t = (EditText) findViewById(R.id.eEscribeT);
        savedInstanceState.putString("texto", t.getText().toString());
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fotoen64 = savedInstanceState.getString("foto");
        texto = savedInstanceState.getString("texto");
    }

    protected void onStart() {
        super.onStart();
        EditText textoPubli = (EditText) findViewById(R.id.eEscribeT);
        textoPubli.setText(texto);
        if(fotoen64 != null){
            byte[] decodedString = Base64.decode(fotoen64, Base64.DEFAULT);
            foto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ImageView elImageView = findViewById(R.id.imageView);
            elImageView.setImageBitmap(foto);
        }
    }

    protected void onResume(){
        super.onResume();
        EditText textoPubli = (EditText) findViewById(R.id.eEscribeT);
        textoPubli.setText(texto);
        if(fotoen64 != null){
            byte[] decodedString = Base64.decode(fotoen64, Base64.DEFAULT);
            foto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ImageView elImageView = findViewById(R.id.imageView);
            elImageView.setImageBitmap(foto);
        }
    }
}
