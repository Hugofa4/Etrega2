package com.example.entrega2;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Comentarios extends AppCompatActivity {

    private String[] publicacion = new String[4];
    private String usuario;
    private Bitmap imagen;
    private String token;
    private EditText eComen;
    private String idPubliNotifi;

    protected void onCreate(Bundle SavedInstanceState) {
        Bundle extras = getIntent().getExtras();
        boolean notificacion = false;

        if (extras != null) {
            //Si el booleano notificacion es true se quiere llegar a esta página desde el botón acceder a comentarios
            //Si es false se quiere acceder desde una notificación
            notificacion = extras.getString("notificacion").equals("0");
            if(notificacion){
                //Se recogen todos los datos necesarios
                publicacion = extras.getStringArray("publicacion");
                usuario = extras.getString("usuario");
                String imagen64 = extras.getString("imagen64");
                byte[] decodedString = Base64.decode(imagen64, Base64.DEFAULT);
                imagen = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }else{
                //En este caso debemos recuperar la publicación a partir del id que llega en el data de la notificación
                idPubliNotifi = extras.getString("idPubli");
                cargarPubliNotifi(parseInt(idPubliNotifi));
            }

        }
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.comentarios);

        if(notificacion){
            //En este caso ya tenemos la información necesaria para mostrar la publicación y cargar sus comentarios
            TextView usuarioT = (TextView) findViewById(R.id.usuarioC);
            usuarioT.setText(publicacion[1]);
            TextView textoT = (TextView) findViewById(R.id.textoC);
            textoT.setText(publicacion[2]);
            ImageView imagenV = (ImageView) findViewById(R.id.imagenC);
            imagenV.setImageBitmap(imagen);
            cargarComentarios(parseInt(publicacion[0]));
        }

        eComen = (EditText) findViewById((R.id.eComentario));
        Button bPubliComen = (Button) findViewById(R.id.bPubliComen);
        bPubliComen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publicarComentario(eComen.getText().toString());
            }
        });


    }

    private void cargarComentarios(int idPubli){
        //Se recogen de la base de datos todos los comentarios de la publicación con el id pasado
        Data datos = new Data.Builder()
                .putInt("metodo",0)
                .putInt("idPubli",idPubli)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ComentarioBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("result");
                        //Si result no es ni 0 ni 1 es que se han devuelto comentarios
                        if(!result.equals("0") && !result.equals("1")) {
                            JSONArray jsonArray = null;
                            try {
                                //Se guardan en un jsonArray los resultados que están en un String
                                jsonArray = new JSONArray(result);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            //Se inicializan los arrays necesarios con el tamaño del jsonArray que tiene los resultados
                            String[] comentarios = new String[jsonArray.length()];
                            String[] usuarios = new String[jsonArray.length()];
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String usuarioC = null;
                                try {
                                    //Se almacena el usuario que ha realizado cada comentario
                                    usuarioC = jsonArray.getJSONObject(i).getString("Usuario");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                comentarios[i] = usuarioC;
                                String coment = null;
                                try {
                                    //Se almacena el texto del comentario
                                    coment = jsonArray.getJSONObject(i).getString("Comentario");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                usuarios[i] = coment;
                            }
                            //El if es para saber si por lo menos hay un comentario en la publicación
                            if (comentarios[0] != null) {
                                //Se ponen los valores necesarios en el adapter para poder mostrar el ListView con los resultados
                                ArrayAdapter eladaptador =
                                        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2,android.R.id.text1,comentarios){
                                            @Override
                                            public View getView(int position, View convertView, ViewGroup parent) {
                                                View vista= super.getView(position, convertView, parent);
                                                TextView usuario = (TextView) vista.findViewById(android.R.id.text1);
                                                TextView comentario = (TextView) vista.findViewById(android.R.id.text2);
                                                usuario.setText(usuarios[position]);
                                                comentario.setText(comentarios[position].toString());
                                                return vista;
                                            }
                                        };
                                ListView lalista = findViewById(R.id.lComentarios);
                                lalista.setAdapter(eladaptador);
                            }
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void publicarComentario(String texto){
        //Se llama al método para publicar un comentario y guardarlo en la BD pasando los parámetros necesarios
        Data datos = new Data.Builder()
                .putInt("metodo",1)
                .putString("usuario",usuario)
                .putString("texto",texto)
                .putInt("idPubli", parseInt(publicacion[0]))
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ComentarioBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        if(result.equals("1")){
                            //Si ha ido correctamente se llama al siguiente método
                            tokenEnviar(texto);
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void avisarConMensaje(String texto, String token){
        //Utiliza el servicio web encargado de enviar la notificación al último móvil en el que se ha iniciado sesión con el usuario de la publicación en la que nos encontramos
        Data datos = new Data.Builder()
                .putInt("idPubli", parseInt(publicacion[0]))
                .putString("usuario",usuario)
                .putString("texto", texto)
                .putString("token",token)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(MensajeBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        //Se ha terminado el proceso del comentario se muestra un dialog confirmando que se ha enviado y se recarga la lista para que se muestre
                        String result = status.getOutputData().getString("resultado");
                        if(result.equals("1")){
                            DialogoComentario d = new DialogoComentario();
                            d.show(getSupportFragmentManager(), "1");
                            cargarComentarios(parseInt(publicacion[0]));
                            //Se resetea el editText donde se escribe el comentario
                            eComen.setText("");
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void tokenEnviar(String texto){
        //Se recupera el token del usuario que ha subido la publicación para poder enviarle el mensaje
        Data datos = new Data.Builder()
                .putInt("metodo",3)
                .putString("usuario",publicacion[1])
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(UsuarioBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        //Se recupera el token y ya se puede enviar el mensaje
                        token = status.getOutputData().getString("resultado");
                        avisarConMensaje(texto, token);
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);

    }

    private void cargarPubliNotifi(int idPubli){
        //Cuando se viene desde la notificación se necesita recuperar los datos de la publicación a partir de su id
        Data datos = new Data.Builder()
                .putInt("metodo",2)
                .putInt("idPubli",idPubli)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(MostrarBDWebService.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        //Si ha ido correctamente
                        String result = status.getOutputData().getString("result");
                        if(!result.isEmpty()) {
                            JSONArray jsonArray = null;
                            try {
                                //Se guarda la información en un jsonArray
                                jsonArray = new JSONArray(result);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            //Se recorre el jsonArray para almacenar toda la información
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String[] publicacionN = new String[3];
                                try {
                                    usuario = jsonArray.getJSONObject(i).getString("Usuario");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                publicacionN[0] = usuario;
                                String texto = null;
                                try {
                                    texto = jsonArray.getJSONObject(i).getString("Texto");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                publicacionN[1] = texto;
                                String imagenId = null;
                                try {
                                    imagenId = jsonArray.getJSONObject(i).getString("ImagenId");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                publicacionN[2] = imagenId;
                                //Se asigna los datos a sus respectivas Views
                                TextView usuarioT = (TextView) findViewById(R.id.usuarioC);
                                usuarioT.setText(publicacionN[0]);
                                TextView textoT = (TextView) findViewById(R.id.textoC);
                                textoT.setText(publicacionN[1]);
                                cargarImagenN(publicacionN[2], idPubliNotifi);
                            }
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void cargarImagenN(String imagenId, String idPubliN){
        //Cargar la imagen cuando se viene de una notificación, hay que realizar la petición en un thread
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    //Se crea la dirección y los parámetros
                    String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/hfernandez026/WEB/mostrarBd.php";
                    String parametros = "metodo=3&imagenId=" + imagenId;
                    HttpURLConnection urlConnection = null;
                    try {
                        URL destino = new URL(direccion);
                        urlConnection = (HttpURLConnection) destino.openConnection();
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setReadTimeout(5000);
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
                        //Si la petición ha sido correcta
                        BufferedInputStream inputStream = null;
                        try {
                            //El inputStream será la imagen de la publicacion en base64
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
                        //Se lee el bufferedReader con el resultado linea a linea y se concatena en un String
                        String line = "";
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
                        //Se comprueba que el resultado no está vacio
                        if (!res.isEmpty()) {
                            JSONArray jsonArray = null;
                            try {
                                jsonArray = new JSONArray(res);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            Bitmap imagenN = null;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String img;
                                try {
                                    img = jsonArray.getJSONObject(i).getString("Imagen");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                //Una vez se tiene la imagen en base64 hay que reconvertirla a bitmap
                                //Para ello hay que quitar los espacios de linea que son creados al enviar el texto a la base de datos cuando se almacena la imagen
                                String r = img.replaceAll("\n", "");
                                String r2 = r.replaceAll(" ", "+");
                                byte[] decodedString = Base64.decode(r2, Base64.DEFAULT);
                                imagenN = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            }
                            //Se pone la imagen en el ImageView
                            ImageView imagenV = (ImageView) findViewById(R.id.imagenC);
                            imagenV.setImageBitmap(imagenN);
                        }
                    }
                } catch (RuntimeException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        //Comienza el thread que se acaba de programar
        thread.start();
        //Se hace este bucle para que no avance al resto de tareas hasta no terminar esta, ya que es una tarea asíncrona
        while(thread.isAlive()){
        }
        //Una vez se tiene la imagen se puede cargar el resto de la información
        cargarComentarios(parseInt(idPubliN));
    }
}

