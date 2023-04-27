package com.example.entrega2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PagPrincipal extends AppCompatActivity {

    private ActivityResultLauncher<Intent> startActivityIntent;
    private String usuario;
    private AdaptadorRecyclerPrin adaptador;
    private ArrayList<String[]> publicaciones = new ArrayList<>();
    private ArrayList<Bitmap> imagenes = new ArrayList<>();

    protected void onCreate(Bundle SavedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //Se recoge el usuario con el que se ha iniciado sesion
            usuario = extras.getString("usuario");
        }
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.pagprincipal);
        cargarPublicaciones();
        cargarImagenes();

        Button nuevaPubli = (Button) findViewById(R.id.bNuevaPubli);
        nuevaPubli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagNuevaPubli();
            }
        });
        Button comentar = (Button) findViewById(R.id.bComentar);
        comentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagComentarios();
            }
        });
        startActivityIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                }
            }
        });
    }

    private void cargarPublicaciones(){
        //Recoger todas la publicaciones de la base de datos para poder mostrarlas
        Data datos = new Data.Builder()
                .putInt("metodo",0)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(MostrarBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("result");
                        //Se almacena en el String result lo devuelto por el servicio web
                        if(!result.equals("null")) {
                            JSONArray jsonArray = null;
                            try {
                                //Se transforma a un jsonArray el String con el resultado
                                jsonArray = new JSONArray(result);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            //Se recorren todos los elementos del array
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //Cada elemento del array es una publicación que contará con 4 elementos
                                String[] publicacion = new String[4];
                                String publicacionId = null;
                                //Se guardan todos los elementos recogiendolos con sus respectivas claves
                                try {
                                    publicacionId = jsonArray.getJSONObject(i).getString("PublicacionId");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                publicacion[0] = publicacionId;
                                String usuario = null;
                                try {
                                    usuario = jsonArray.getJSONObject(i).getString("Usuario");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                publicacion[1] = usuario;
                                String texto = null;
                                try {
                                    texto = jsonArray.getJSONObject(i).getString("Texto");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                publicacion[2] = texto;
                                String imagenId = null;
                                try {
                                    imagenId = jsonArray.getJSONObject(i).getString("ImagenId");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                //Se añade la publicación al array de publicaciones que se ha inicializado previamente
                                publicacion[3] = imagenId;
                                publicaciones.add(publicacion);
                            }
                            //Si hay al menos una publicación en el array se muestran
                            if (!publicaciones.isEmpty()){
                                cargarRecyclerView();
                            }
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void cargarRecyclerView(){
        //Se inicializan los arrays necesarios para indicar la información en cada cardView del recyclerView
        RecyclerView lista= findViewById(R.id.recyclerprin);
        String[] id = new String[publicaciones.size()];
        String[] usuarios = new String[publicaciones.size()];
        String[] textos = new String[publicaciones.size()];
        Bitmap[] imags = new Bitmap[imagenes.size()];
        for (int i = 0; i < publicaciones.size(); i++) {
            String[] publi = publicaciones.get(i);
            id[i] = publi[0];
            usuarios[i] = publi[1];
            textos[i] = publi[2];
            imags[i] = imagenes.get(i);
        }
        adaptador = new AdaptadorRecyclerPrin(imags,usuarios,textos);
        lista.setAdapter(adaptador);
        LinearLayoutManager layoutLineal= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lista.setLayoutManager(layoutLineal);
    }

    private void cargarImagenes(){
        //Se crea un nuevo thread para recuperar las imágenes almacenadas en la base de datos
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    //Dirección y parametros
                    String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/hfernandez026/WEB/mostrarBd.php";
                    String parametros = "metodo=1";
                    HttpURLConnection urlConnection = null;
                    try {
                        URL destino = new URL(direccion);
                        urlConnection = (HttpURLConnection) destino.openConnection();
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setReadTimeout(5000);
                        //Se añaden los parámetros
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                        out.print(parametros);
                        out.close();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    int statusCode = 0;
                    try {
                        statusCode = urlConnection.getResponseCode();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String res = "";
                    if (statusCode == 200) {
                        //La petición es correcta
                        BufferedInputStream inputStream = null;
                        try {
                            inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        BufferedReader bufferedReader = null;
                        try {
                            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        String line = "";
                        //Se almacena en el String result lo devuelto por la select
                        while (true) {
                            try {
                                if (!((line = bufferedReader.readLine()) != null)) break;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            res += line;
                        }
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        //Si la respuesta no es vacia
                        if (!res.equals("null")) {
                            //Se crea un jsonArray con los resultados
                            JSONArray jsonArray = null;
                            try {
                                jsonArray = new JSONArray(res);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            //Por cada el elemento del jsonArray
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Bitmap imagen;
                                String img;
                                try {
                                    //La imagen en base64
                                    img = jsonArray.getJSONObject(i).getString("Imagen");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                //Se eliminan los saltos de línea y espacios creados al enviar la imagen a la base de datos desde android
                                //Se vuelve a convertir a bitmap y se añade al array de imagenes
                                String r = img.replaceAll("\n", "");
                                String r2 = r.replaceAll(" ", "+");
                                byte[] decodedString = Base64.decode(r2, Base64.DEFAULT);
                                imagen = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                imagenes.add(imagen);
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //Comienza el thread
        thread.start();
        //While para no comenzar con la siguiente tarea hasta que no termine esta
        while(thread.isAlive()){

        }

    }

    private void pagNuevaPubli(){
        //Se llama a la actividad de subir una nueva publicación pasandole el usuario con el que se ha iniciado sesión
        Intent intent = new Intent(PagPrincipal.this, NuevaPubli.class);
        intent.putExtra("usuario", usuario);
        startActivityIntent.launch(intent);
    }

    private void pagComentarios(){
        //Acceder a los comentarios de la página seleccionada
        if(adaptador.getPublicacionSeleccionada() < publicaciones.size()){
            //Se coge la publicación que esté en la posición que se almacenó en el adaptador
            String[] publicacion = publicaciones.get(adaptador.getPublicacionSeleccionada());
            //Para poder pasar la imagen a la nueva actividad hay que transformarla a un String en base64
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap fotoSelec= imagenes.get(adaptador.getPublicacionSeleccionada());
            fotoSelec.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] fototransformada = stream.toByteArray();
            String imagen64 = Base64.encodeToString(fototransformada,Base64.DEFAULT);

            Intent i = new Intent (this, Comentarios.class);
            i.putExtra("notificacion","0");
            i.putExtra("usuario",usuario);
            i.putExtra("publicacion", publicacion);
            i.putExtra("imagen64", imagen64);
            startActivity(i);
        }
    }

    protected void onStart() {
        super.onStart();
    }

    //protected void onResume(){
      //  super.onResume();
        //cargarPublicaciones();
        //cargarImagenes();
    //}
}
