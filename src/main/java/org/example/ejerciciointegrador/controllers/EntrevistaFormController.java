package org.example.ejerciciointegrador.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.example.ejerciciointegrador.common.Alertas;
import org.example.ejerciciointegrador.common.AppContext;
import org.example.ejerciciointegrador.common.SceneNavigator;
import org.example.ejerciciointegrador.data.EntrevistaJdbcDAO;
import org.example.ejerciciointegrador.models.Entrevista;
import org.example.ejerciciointegrador.models.Oferta;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

/**
 * Controlador del formulario de alta y edición de entrevistas.
 * <p>
 * La oferta a la que pertenece la entrevista se toma del {@link AppContext}.
 * Si además hay una entrevista seleccionada, se edita; en caso contrario se
 * crea una nueva. Al terminar vuelve a la pantalla de detalle de la oferta.
 * </p>
 */
@RequiredArgsConstructor
public class EntrevistaFormController implements Initializable {

    /** Formato de la hora admitido en el formulario (por ejemplo 9:30 o 16:45). */
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("H:mm");

    /** Título del formulario. */
    @FXML
    private Label lblCabecera;

    /** Etiqueta con el título de la oferta a la que pertenece. */
    @FXML
    private Label lblOferta;

    /** Selector de la fecha. */
    @FXML
    private DatePicker dpFecha;

    /** Campo de la hora (H:mm). */
    @FXML
    private TextField txtHora;

    /** Selector del tipo de entrevista. */
    @FXML
    private ComboBox<String> cmbTipo;

    /** Selector del resultado. */
    @FXML
    private ComboBox<String> cmbResultado;

    /** Área de texto de las notas (opcional). */
    @FXML
    private TextArea txtNotas;

    /** DAO de entrevistas. */
    private final EntrevistaJdbcDAO entrevistaDAO;

    /** Oferta a la que pertenece la entrevista. */
    private Oferta oferta;

    /** Entrevista que se edita, o {@code null} si se está creando una nueva. */
    private Entrevista entrevista;

    /**
     * Se invoca automáticamente después de cargar el FXML.
     * <p>
     * Rellena los desplegables y, si hay una entrevista en el
     * {@link AppContext}, carga sus datos en el formulario.
     * </p>
     *
     * @param location  ubicación usada para resolver rutas relativas, o {@code null}
     * @param resources recursos usados para localizar la vista, o {@code null}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        oferta = (Oferta) AppContext.getInstance().get("oferta");
        entrevista = (Entrevista) AppContext.getInstance().get("entrevista");

        lblOferta.setText(oferta.getTitulo() + " · " + oferta.getEmpresaNombre());
        cmbTipo.getItems().addAll(Entrevista.TIPOS);
        cmbResultado.getItems().addAll(Entrevista.RESULTADOS);

        if (entrevista != null) {
            lblCabecera.setText("Editar entrevista");
            dpFecha.setValue(entrevista.getFecha().toLocalDate());
            txtHora.setText(entrevista.getFecha().toLocalTime().format(FORMATO_HORA));
            cmbTipo.setValue(entrevista.getTipo());
            cmbResultado.setValue(entrevista.getResultado());
            txtNotas.setText(entrevista.getNotas());
        } else {
            lblCabecera.setText("Nueva entrevista");
            dpFecha.setValue(LocalDate.now());
            txtHora.setText("10:00");
            cmbResultado.setValue("Pendiente");
        }
    }

    /**
     * Valida y guarda los datos del formulario, y vuelve al detalle de la oferta.
     *
     * @param actionEvent evento de acción generado al pulsar el botón guardar
     */
    @FXML
    public void save(ActionEvent actionEvent) {
        if (dpFecha.getValue() == null) {
            Alertas.error("La fecha es obligatoria.");
            return;
        }
        LocalTime hora;
        try {
            hora = LocalTime.parse(txtHora.getText().trim(), FORMATO_HORA);
        } catch (DateTimeParseException e) {
            Alertas.error("La hora no es válida. Usa el formato HH:mm (por ejemplo 16:30).");
            return;
        }
        if (cmbTipo.getValue() == null) {
            Alertas.error("Selecciona el tipo de entrevista.");
            return;
        }
        if (cmbResultado.getValue() == null) {
            Alertas.error("Selecciona el resultado.");
            return;
        }

        Entrevista datos = (entrevista == null) ? new Entrevista() : entrevista;
        datos.setIdOferta(oferta.getId());
        datos.setFecha(dpFecha.getValue().atTime(hora));
        datos.setTipo(cmbTipo.getValue());
        datos.setResultado(cmbResultado.getValue());
        datos.setNotas(txtNotas.getText());

        try {
            if (entrevista == null) {
                entrevistaDAO.save(datos);
            } else {
                entrevistaDAO.update(datos);
            }
        } catch (RuntimeException e) {
            Alertas.error("No se pudo guardar la entrevista: " + e.getMessage());
            return;
        }
        goBack(actionEvent);
    }

    /**
     * Vuelve al detalle de la oferta sin guardar los cambios.
     *
     * @param actionEvent evento de acción generado al pulsar el botón volver
     */
    @FXML
    public void goBack(ActionEvent actionEvent) {
        AppContext.getInstance().remove("entrevista");
        SceneNavigator.navigateTo("oferta-detail-view.fxml", "Detalle de candidatura");
    }
}
