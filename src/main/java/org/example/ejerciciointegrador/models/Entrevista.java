package org.example.ejerciciointegrador.models;

import lombok.Data;

import java.util.Date;

@Data
public class Entrevista {

    int id;
    int id_oferta;
    Date Fecha;
    String tipo;
    String notas;
    String resultado;

}
