package com.example.entrega2;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ComentarioBDWebService extends Worker {
    public ComentarioBDWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/hfernandez026/WEB/comentariosBd.php";
        //Se pasan los parametros, que varian según el método que se quiera realizar
        int metodo = getInputData().getInt("metodo", 0);
        int idPubli = getInputData().getInt("idPubli", 0);
        String parametros;
        if (metodo == 1){
            String usuario = getInputData().getString("usuario");
            String texto = getInputData().getString("texto");
            parametros = "metodo=" + metodo + "&idPubli=" + idPubli + "&usuario=" + usuario + "&texto=" + texto;
            if(metodo == 1){}
        }else{
            parametros = "metodo=" + metodo + "&idPubli=" + idPubli;
        }
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
        if (metodo == 0){
            return cargarComentarios(urlConnection, statusCode);
        }else{
            return insertarComentario(statusCode);
        }
    }

    private Result cargarComentarios(HttpURLConnection urlConnection, Integer statusCode){
        if(statusCode == 200){
            //La petición ha sido correcta
            BufferedInputStream inputStream = null;
            String result = "";
            JSONArray jsonArray = null;
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
            //Se va leyendo el bufferReader linea a linea y se almacena el resultado en el String result
            String line = "";
            while (true) {
                try {
                    if (!((line = bufferedReader.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                result += line;
            }
            Data resultados;
            //Si el String result es distinto de null existen comentarios y se devuelven
            if(!result.equals("null")){
                resultados = new Data.Builder()
                        .putString("result",result)
                        .build();
            }else{
                //Si no existen comentarios se devuelve un 0
                resultados = new Data.Builder()
                        .putString("result","0")
                        .build();
            }
            return Result.success(resultados);
        }else{
            //Si la petición ha sido incorrecta se devuelve un 0
            Data resultados = new Data.Builder()
                    .putString("resultado", "0")
                    .build();
            return Result.success(resultados);
        }

    }

    private Result insertarComentario(Integer statusCode){
        if (statusCode == 200) {
            //La petición ha sido correcta, se devuelve un 1
            Data resultados = new Data.Builder()
                    .putString("resultado", "1")
                    .build();
            return Result.success(resultados);
        } else {
            //La petición ha sido incorrecta se devuelve un 0
            Data resultados = new Data.Builder()
                    .putString("resultado", "0")
                    .build();
            return Result.success(resultados);
        }
    }

}