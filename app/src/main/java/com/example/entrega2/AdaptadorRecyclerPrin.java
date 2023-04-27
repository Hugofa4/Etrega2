package com.example.entrega2;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdaptadorRecyclerPrin extends RecyclerView.Adapter<ListaPrin>{

    private Bitmap[] imagenes;
    private String[] usuarios;
    private String[] textos;
    private static boolean[] seleccionados;

    public AdaptadorRecyclerPrin(Bitmap[] i, String[] u, String[] t){
        //Se guardan todos los arrays necesarios
        imagenes = i;
        usuarios = u;
        textos = t;
        seleccionados = new boolean[i.length];
    }

    @NonNull
    @Override
    public ListaPrin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Se asigna el layout que tendrán todos los elementos
        View elLayoutDeCadaItem= LayoutInflater.from(parent.getContext()).inflate(R.layout.cardviewprin,null);
        ListaPrin lp = new ListaPrin(elLayoutDeCadaItem);
        return lp;
    }

    @Override
    public void onBindViewHolder(@NonNull ListaPrin holder, int position) {
        holder.imagen.setImageBitmap(imagenes[position]);
        holder.usuario.setText(usuarios[position]);
        holder.texto.setText(textos[position]);
        //Se crea el listener para saber que publicación se ha seleccionado
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (seleccionados[holder.getAdapterPosition()]==true){
                    seleccionados[holder.getAdapterPosition()]=false;
                    //Si se quita la selección de una publicacion se le quita el marco que se le había añadido
                    holder.usuario.setTextColor(Color.BLACK);
                    holder.imagen.setPadding(0,0,0,0);
                }
                else{
                    //Cuando una publicacion es seleccionada se la añade un marco a la imagen y el nombre de usuario se pone en rojo
                    seleccionados[holder.getAdapterPosition()]=true;
                    holder.usuario.setTextColor(Color.RED);
                    holder.imagen.setPadding(10,10,10,10);
                    //Si habia otra publicación seleccionada se quita su selección, solo puede haber una
                    for (boolean b : seleccionados){
                        b = false;
                    }

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usuarios.length;
    }

    public int getPublicacionSeleccionada(){
        //Método que se usa para saber la posición en el array del usuario seleccionada
        int publi = -1;
        int aux = 0;
        for (boolean s : seleccionados) {
            if (s){
                publi = aux;
            }else{
                aux = aux + 1;
            }
        }
        if (publi < 0){
            publi = aux;
        }
        return publi;
    }
}
