package org.example.ejerciciointegrador.common;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Utilidad para mostrar cuadros de diálogo (información, error y confirmación).
 */
public class Alertas {

    /**
     * Muestra un mensaje informativo.
     *
     * @param mensaje texto a mostrar
     */
    public static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Información", mensaje);
    }

    /**
     * Muestra un mensaje de error o de validación.
     *
     * @param mensaje texto a mostrar
     */
    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }

    /**
     * Pide confirmación al usuario antes de una acción destructiva.
     *
     * @param mensaje pregunta a mostrar
     * @return {@code true} si el usuario pulsa Aceptar
     */
    public static boolean confirmar(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmar");
        alert.setHeaderText(null);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    /**
     * Crea y muestra un diálogo del tipo indicado.
     *
     * @param tipo    tipo de alerta
     * @param titulo  título de la ventana
     * @param mensaje texto a mostrar
     */
    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo, mensaje, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
