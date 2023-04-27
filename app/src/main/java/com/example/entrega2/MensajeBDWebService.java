package com.example.entrega2;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


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

public class MensajeBDWebService extends Worker {
    public MensajeBDWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //Se especifican la dirección del archivo php y los parámetros necesarios
        String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/hfernandez026/WEB/mensajes.php";
        int idPubli = getInputData().getInt("idPubli",0);
        String usuario = getInputData().getString("usuario");
        String texto = getInputData().getString("texto");
        String token = getInputData().getString("token");
        String parametros = "idPubli=" + idPubli + "&usuario=" + usuario + "&texto=" + texto + "&token=" + token;
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