package org.example.ejerciciointegrador.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OfertaTest {

    private static final String LINEA_VALIDA =
            "1;Desarrollador Java Junior;Indra;pendiente;26/10/2026;https://empleo.example.com/indra/1001;Enviado por el portal";

    @Test
    void desdeLineaCsvParseaTodosLosCampos() {
        Oferta oferta = Oferta.desdeLineaCsv(LINEA_VALIDA);

        assertNull(oferta.getId(), "El id del CSV se ignora");
        assertNull(oferta.getIdEmpresa(), "La empresa se resuelve después en el servicio");
        assertEquals("Indra", oferta.getEmpresaNombre());
        assertEquals("Desarrollador Java Junior", oferta.getTitulo());
        assertEquals("pendiente", oferta.getEstado());
        assertEquals(LocalDate.of(2026, 10, 26), oferta.getFechaPostulacion());
        assertEquals("https://empleo.example.com/indra/1001", oferta.getEnlace());
        assertEquals("Enviado por el portal", oferta.getNotas());
        assertNull(oferta.getSalario(), "El CSV no incluye salario");
    }

    @Test
    void desdeLineaCsvRechazaFaltanColumnas() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Oferta.desdeLineaCsv("1;Solo titulo;Indra"));
        assertTrue(e.getMessage().contains("7 columnas"));
    }

    @Test
    void desdeLineaCsvRechazaEstadoNoValido() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Oferta.desdeLineaCsv("1;Titulo;Indra;en_proceso;26/10/2026;;"));
        assertTrue(e.getMessage().contains("Estado no válido"));
    }

    @Test
    void desdeLineaCsvRechazaFechaConFormatoIncorrecto() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Oferta.desdeLineaCsv("1;Titulo;Indra;pendiente;2026-10-26;;"));
        assertTrue(e.getMessage().contains("dd/MM/yyyy"));
    }

    @Test
    void desdeLineaCsvRechazaTituloOEmpresaVacios() {
        assertThrows(IllegalArgumentException.class,
                () -> Oferta.desdeLineaCsv("1;;Indra;pendiente;26/10/2026;;"));
        assertThrows(IllegalArgumentException.class,
                () -> Oferta.desdeLineaCsv("1;Titulo;;pendiente;26/10/2026;;"));
    }

    @Test
    void esEstadoValidoIgnoraMayusculasYEspacios() {
        assertTrue(Oferta.esEstadoValido("  ENTREVISTA "));
        assertTrue(Oferta.esEstadoValido("aceptado"));
        assertFalse(Oferta.esEstadoValido("contratado"));
        assertFalse(Oferta.esEstadoValido(null));
    }

    @Test
    void gettersDeTextoParaLaInterfaz() {
        Oferta oferta = Oferta.desdeLineaCsv(LINEA_VALIDA);

        assertEquals("26/10/2026", oferta.getFechaTexto());
        assertEquals("--", oferta.getSalarioTexto());

        oferta.setSalario(35000.0);
        assertTrue(oferta.getSalarioTexto().contains("35000"));
        assertTrue(oferta.getSalarioTexto().contains("€"));
    }
}
