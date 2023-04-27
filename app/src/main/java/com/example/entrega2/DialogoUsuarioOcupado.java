package com.example.entrega2;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class DialogoUsuarioOcupado extends DialogFragment {
    @NonNull
    @Override
    public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Se define el título y el mensaje de el diálogo
        builder.setTitle("Nombre de usuario existente");
        builder.setMessage("Ya hay un usuario con ese nombre, cambielo");
        return builder.create();
    }
}