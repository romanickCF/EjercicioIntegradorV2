package org.example.ejerciciointegrador.data;

import java.time.LocalDate;

/**
 * Criterios combinables para buscar ofertas. Cualquier campo a {@code null}
 * (o texto vacío) se ignora en la consulta.
 *
 * @param texto      texto a buscar en el título o en las notas
 * @param empresa    texto contenido en el nombre de la empresa
 * @param estado     estado exacto de la candidatura
 * @param salarioMin salario mínimo (inclusive)
 * @param salarioMax salario máximo (inclusive)
 * @param desde      fecha de postulación mínima (inclusive)
 * @param hasta      fecha de postulación máxima (inclusive)
 */
public record FiltroOferta(String texto, String empresa, String estado,
                           Double salarioMin, Double salarioMax,
                           LocalDate desde, LocalDate hasta) {

    /**
     * Crea un filtro vacío, que devuelve todas las ofertas.
     *
     * @return un filtro sin criterios
     */
    public static FiltroOferta vacio() {
        return new FiltroOferta(null, null, null, null, null, null, null);
    }
}
