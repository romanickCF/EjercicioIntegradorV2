package org.example.ejerciciointegrador.models;

import lombok.Data;

import java.util.Date;

@Data
public class Empresa {
    int id;
    String nombre;
    String sector;
    String web;
    Date fechaRegistro;

}
