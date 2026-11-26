package org.example.ejerciciointegrador.models;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class Oferta {
        private Long id;
        private Long idEmpresa;
        private String empresaNombre;
        private String titulo;
        private String descripcion;
        private String estado;
        private LocalDate fechaPostulacion;
        private String enlace;
        private Double salario;
        private String notas;
    }


