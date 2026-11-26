package org.example.ejerciciointegrador.common;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * Contenedor compartido de datos de la aplicación, implementado como singleton.
 * <p>
 * Se usa para pasar información entre pantallas (por ejemplo, la oferta
 * seleccionada en la tabla). Las claves que se utilizan son:
 * {@code "oferta"}, {@code "empresa"}, {@code "entrevista"} y
 * {@code "origen"} (el FXML al que volver desde un formulario).
 * </p>
 */
public class AppContext {

    /** Instancia única del contexto, creada al cargar la clase. */
    private static final AppContext INSTANCE = new AppContext();

    /** Mapa observable donde se almacenan los datos compartidos, indexado por clave. */
    private final ObservableMap<String, Object> datos = FXCollections.observableHashMap();

    /** Constructor privado para impedir la instanciación externa (patrón singleton). */
    private AppContext() {
    }

    /**
     * Devuelve la instancia única del contexto de la aplicación.
     *
     * @return la instancia compartida de {@link AppContext}
     */
    public static AppContext getInstance() {
        return INSTANCE;
    }

    /**
     * Devuelve el mapa observable con todos los datos compartidos.
     *
     * @return el {@link ObservableMap} que almacena los pares clave-valor
     */
    public ObservableMap<String, Object> getDatos() {
        return datos;
    }

    /**
     * Obtiene el valor asociado a la clave indicada.
     *
     * @param key clave del dato que se desea recuperar
     * @return el valor asociado a la clave, o {@code null} si no existe
     */
    public Object get(String key) {
        return datos.get(key);
    }

    /**
     * Almacena o actualiza el valor asociado a la clave indicada.
     *
     * @param key   clave con la que se identifica el dato
     * @param value valor a guardar
     */
    public void put(String key, Object value) {
        datos.put(key, value);
    }

    /**
     * Elimina el valor asociado a la clave indicada.
     *
     * @param key clave del dato que se desea eliminar
     */
    public void remove(String key) {
        datos.remove(key);
    }
}
