package org.example.ejerciciointegrador.data;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FiltroOfertaTest {

    @Test
    void filtroVacioNoTieneCriterios() {
        FiltroOferta filtro = FiltroOferta.vacio();

        assertNull(filtro.texto());
        assertNull(filtro.empresa());
        assertNull(filtro.estado());
        assertNull(filtro.salarioMin());
        assertNull(filtro.salarioMax());
        assertNull(filtro.desde());
        assertNull(filtro.hasta());
    }

    @Test
    void filtroCombinaVariosCriterios() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        FiltroOferta filtro = new FiltroOferta("java", "Indra", "pendiente",
                20000.0, 40000.0, desde, null);

        assertEquals("java", filtro.texto());
        assertEquals("Indra", filtro.empresa());
        assertEquals("pendiente", filtro.estado());
        assertEquals(20000.0, filtro.salarioMin());
        assertEquals(40000.0, filtro.salarioMax());
        assertEquals(desde, filtro.desde());
        assertNull(filtro.hasta());
    }
}
