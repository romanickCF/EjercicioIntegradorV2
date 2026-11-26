package org.example.ejerciciointegrador.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.example.ejerciciointegrador.common.Alertas;
import org.example.ejerciciointegrador.common.AppContext;
import org.example.ejerciciointegrador.common.SceneNavigator;
import org.example.ejerciciointegrador.data.EmpresaJdbcDAO;
import org.example.ejerciciointegrador.data.OfertaJdbcDAO;
import org.example.ejerciciointegrador.models.Empresa;
import org.example.ejerciciointegrador.models.Oferta;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del formulario de alta y edición de ofertas.
 * <p>
 * Si en el {@link AppContext} existe una oferta, el formulario se rellena con
 * sus datos y al guardar se actualiza; en caso contrario se crea una nueva.
 * Valida los campos antes de guardar.
 * </p>
 */
@RequiredArgsConstructor
public class OfertaFormController implements Initializable {

    /** Título del formulario. */
    @FXML
    private Label lblCabecera;

    /** Campo del título del puesto. */
    @FXML
    private TextField txtTitulo;

    /** Selector de la empresa. */
    @FXML
    private ComboBox<Empresa> cmbEmpresa;

    /** Selector del estado. */
    @FXML
    private ComboBox<String> cmbEstado;

    /** Selector de la fecha de postulación. */
    @FXML
    private DatePicker dpFecha;

    /** Campo del salario (opcional). */
    @FXML
    private TextField txtSalario;

    /** Campo del enlace (opcional). */
    @FXML
    private TextField txtEnlace;

    /** Área de texto de la descripción (opcional). */
    @FXML
    private TextArea txtDescripcion;

    /** Área de texto de las notas (opcional). */
    @FXML
    private TextArea txtNotas;

    /** DAO de ofertas. */
    private final OfertaJdbcDAO ofertaDAO;

    /** DAO de empresas. */
    private final EmpresaJdbcDAO empresaDAO;

    /** Oferta que se edita, o {@code null} si se está creando una nueva. */
    private Oferta oferta;

    /**
     * Se invoca automáticamente después de cargar el FXML.
     * <p>
     * Rellena los desplegables y, si hay una oferta en el {@link AppContext},
     * carga sus datos en el formulario.
     * </p>
     *
     * @param location  ubicación usada para resolver rutas relativas, o {@code null}
     * @param resources recursos usados para localizar la vista, o {@code null}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Empresa> empresas = empresaDAO.findAll();
        cmbEmpresa.getItems().addAll(empresas);
        cmbEstado.getItems().addAll(Oferta.ESTADOS);

        oferta = (Oferta) AppContext.getInstance().get("oferta");
        if (oferta != null) {
            lblCabecera.setText("Editar candidatura");
            txtTitulo.setText(oferta.getTitulo());
            empresas.stream()
                    .filter(e -> e.getId().equals(oferta.getIdEmpresa()))
                    .findFirst()
                    .ifPresent(cmbEmpresa::setValue);
            cmbEstado.setValue(oferta.getEstado());
            dpFecha.setValue(oferta.getFechaPostulacion());
            txtSalario.setText(oferta.getSalario() == null ? "" : String.valueOf(oferta.getSalario()));
            txtEnlace.setText(oferta.getEnlace());
            txtDescripcion.setText(oferta.getDescripcion());
            txtNotas.setText(oferta.getNotas());
        } else {
            lblCabecera.setText("Nueva candidatura");
            cmbEstado.setValue("pendiente");
            dpFecha.setValue(LocalDate.now());
        }
    }

    /**
     * Valida y guarda los datos del formulario, y vuelve a la pantalla de origen.
     *
     * @param actionEvent evento de acción generado al pulsar el botón guardar
     */
    @FXML
    public void save(ActionEvent actionEvent) {
        String error = validar();
        if (error != null) {
            Alertas.error(error);
            return;
        }

        boolean nueva = (oferta == null);
        Oferta datos = nueva ? new Oferta() : oferta;
        datos.setTitulo(txtTitulo.getText().trim());
        datos.setIdEmpresa(cmbEmpresa.getValue().getId());
        datos.setEmpresaNombre(cmbEmpresa.getValue().getNombre());
        datos.setEstado(cmbEstado.getValue());
        datos.setFechaPostulacion(dpFecha.getValue());
        datos.setSalario(salarioIntroducido());
        datos.setEnlace(txtEnlace.getText());
        datos.setDescripcion(txtDescripcion.getText());
        datos.setNotas(txtNotas.getText());

        try {
            if (nueva) {
                ofertaDAO.save(datos);
            } else {
                ofertaDAO.update(datos);
            }
        } catch (RuntimeException e) {
            Alertas.error("No se pudo guardar la candidatura: " + e.getMessage());
            return;
        }
        volver();
    }

    /**
     * Vuelve a la pantalla de origen sin guardar los cambios.
     *
     * @param actionEvent evento de acción generado al pulsar el botón volver
     */
    @FXML
    public void goBack(ActionEvent actionEvent) {
        volver();
    }

    /* Métodos auxiliares */

    /**
     * Comprueba los campos del formulario.
     *
     * @return un mensaje describiendo el primer error, o {@code null} si todo es válido
     */
    private String validar() {
        if (txtTitulo.getText() == null || txtTitulo.getText().isBlank()) {
            return "El título es obligatorio.";
        }
        if (cmbEmpresa.getValue() == null) {
            return "Selecciona una empresa. Si no existe, créala antes en la pantalla de empresas.";
        }
        if (!Oferta.esEstadoValido(cmbEstado.getValue())) {
            return "Selecciona un estado válido.";
        }
        if (dpFecha.getValue() == null) {
            return "La fecha de postulación es obligatoria.";
        }
        String salario = txtSalario.getText();
        if (salario != null && !salario.isBlank()) {
            try {
                if (Double.parseDouble(salario.trim().replace(',', '.')) < 0) {
                    return "El salario no puede ser negativo.";
                }
            } catch (NumberFormatException e) {
                return "El salario debe ser un número.";
            }
        }
        String enlace = txtEnlace.getText();
        if (enlace != null && !enlace.isBlank() && !enlace.trim().startsWith("http")) {
            return "El enlace debe empezar por http:// o https://";
        }
        return null;
    }

    /**
     * Convierte el campo de salario en número.
     *
     * @return el salario, o {@code null} si el campo está vacío
     */
    private Double salarioIntroducido() {
        String salario = txtSalario.getText();
        if (salario == null || salario.isBlank()) {
            return null;
        }
        return Double.parseDouble(salario.trim().replace(',', '.'));
    }

    /**
     * Navega a la pantalla de la que se vino (listado o detalle).
     */
    private void volver() {
        Object origen = AppContext.getInstance().get("origen");
        String fxml = (origen == null) ? "ofertas-view.fxml" : origen.toString();
        String titulo = fxml.startsWith("oferta-detail") ? "Detalle de candidatura" : "Candidaturas";
        SceneNavigator.navigateTo(fxml, titulo);
    }
}
