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

public class UsuarioBDWebService extends Worker {
    public UsuarioBDWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/hfernandez026/WEB/usuariosBd.php";
        String parametros = "";
        int metodo = getInputData().getInt("metodo",0);
        String usuario = getInputData().getString("usuario");
        //Dependiendo del método se necesitan unos parámetros
        if (metodo == 3){
            parametros = "metodo=" + metodo + "&usuario=" + usuario;
        }else{
            String contra = getInputData().getString("contra");
            String token = getInputData().getString("token");
            parametros = "metodo=" + metodo + "&usuario=" + usuario + "&contra=" + contra + "&token=" + token;
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
        if (metodo == 0) {
            return registrado(statusCode);
        }else if(metodo == 1) {
            return iniciado(statusCode, urlConnection);
        }else if(metodo == 2){
            return tokenActualizado(statusCode);
        }else{
            return recogerToken(statusCode, urlConnection);
        }
    }

    private Result registrado(int statusCode){
        if (statusCode == 200) {
            //El nuevo usuario se ha insertado en la base de datos correctamente
            Data resultados = new Data.Builder()
                    .putInt("resultado", 1)
                    .build();
            return Result.success(resultados);
        } else {
            //Hubo un error al insertar el nuevo usuario
            Data resultados = new Data.Builder()
                    .putInt("resultado", 0)
                    .build();
            return Result.success(resultados);
        }
    }

    private Result iniciado(int statusCode, HttpURLConnection urlConnection){
        String result = "";
        if (statusCode == 200) {
            //La petición ha sido correcta
            BufferedInputStream inputStream = null;
            try {
                //Se recoge el resultado devuelto por el select
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
            while (true) {
                //Se pasa por todas las lineas del bufferReader y se van concatenando en un único String
                try {
                    if (!((line = bufferedReader.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                result += line;
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //Se crea un json donde almacenar la información recibida
            JSONParser parser = new JSONParser();
            JSONObject json = null;
            try {
                json = (JSONObject) parser.parse(result);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            //La select puede devolver uno o ningún usuario
            String usuarioI;
            usuarioI = (String) json.get("usuario");
            if (usuarioI != null) {
                //La select ha devuelto un usuario, por lo que el usuario y la contraseña son correctas, se devuelve un 1 para indicar que se debe iniciar sesión
                Data resultados = new Data.Builder()
                        .putInt("resultado", 1)
                        .build();
                return Result.success(resultados);
            } else {
                //El usuario o la contraseña son incorrectas, se devuelve un 0
                Data resultados = new Data.Builder()
                        .putInt("resultado", 0)
                        .build();
                return Result.success(resultados);
            }
        } else {
            //Hubo un error en la petición, se devuelve un 0
            Data resultados = new Data.Builder()
                    .putInt("resultado", 0)
                    .build();
            return Result.success(resultados);
        }
    }

    private Result tokenActualizado(int statusCode){
        if (statusCode == 200) {
            //El token del usuario se ha actualizado correctamente con el del movil usado para iniciar sesión, se devuelve un 1
            Data resultados = new Data.Builder()
                    .putInt("resultado", 1)
                    .build();
            return Result.success(resultados);
        } else {
            //Errro al actualizar el token
            Data resultados = new Data.Builder()
                    .putInt("resultado", 0)
                    .build();
            return Result.success(resultados);
        }
    }

    private Result recogerToken(int statusCode, HttpURLConnection urlConnection){
        String result = "";
        if (statusCode == 200) {
            //La petición ha sido correcta
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
            //Se concatenan todas las lineas del bufferedReader en el String result
            while (true) {
                try {
                    if (!((line = bufferedReader.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                result += line;
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JSONParser parser = new JSONParser();
            JSONObject json = null;
            //Se convierte el String result en un JSON que contará con un único par de clave-valor, el token
            try {
                json = (JSONObject) parser.parse(result);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            String token;
            //Se guarda en un String el token devuelto por la select y se devuelve
            token = (String) json.get("token");
            Data resultados = new Data.Builder()
                    .putString("resultado", token)
                    .build();
            return Result.success(resultados);
        }else{
            //Error al realizar la petición
            Data resultados = new Data.Builder()
                    .putString("resultado", "0")
                    .build();
            return Result.success(resultados);
        }
    }
}
