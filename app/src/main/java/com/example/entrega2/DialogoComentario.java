package com.example.entrega2;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class DialogoComentario extends DialogFragment {
    @NonNull
    @Override
    public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Se define el título y el texto del diálogo
        builder.setTitle("Info");
        builder.setMessage("Comentario realizado con exito");
        return builder.create();
    }
}