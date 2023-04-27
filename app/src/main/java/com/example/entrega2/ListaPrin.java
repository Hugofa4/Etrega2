package com.example.entrega2;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListaPrin extends RecyclerView.ViewHolder {

    public ImageView imagen;
    public TextView usuario;
    public TextView texto;

    public ListaPrin(@NonNull View itemView){
        super(itemView);
        //El recyclerView estará compuesto por una imagen y dos campos de texto
        imagen = itemView.findViewById(R.id.imagen);
        usuario = itemView.findViewById(R.id.usuario);
        texto = itemView.findViewById(R.id.texto);
    }
}
