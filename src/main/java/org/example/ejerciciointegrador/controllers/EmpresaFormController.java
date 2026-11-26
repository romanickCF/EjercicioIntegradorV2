package org.example.ejerciciointegrador.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.ejerciciointegrador.common.Alertas;
import org.example.ejerciciointegrador.common.AppContext;
import org.example.ejerciciointegrador.common.SceneNavigator;
import org.example.ejerciciointegrador.data.EmpresaJdbcDAO;
import org.example.ejerciciointegrador.models.Empresa;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador del formulario de alta y edición de empresas.
 * <p>
 * Si en el {@link AppContext} existe una empresa, el formulario se rellena con
 * sus datos y al guardar se actualiza; en caso contrario se crea una nueva.
 * </p>
 */
public class EmpresaFormController implements Initializable {

    /** Título del formulario. */
    @FXML
    private Label lblCabecera;

    /** Campo del nombre. */
    @FXML
    private TextField txtNombre;

    /** Campo del sector. */
    @FXML
    private TextField txtSector;

    /** Campo de la web. */
    @FXML
    private TextField txtWeb;

    /** Selector de la fecha de registro. */
    @FXML
    private DatePicker dpFecha;

    /** DAO de empresas. */
    private final EmpresaJdbcDAO empresaDAO;

    /** Empresa que se edita, o {@code null} si se está creando una nueva. */
    private Empresa empresa;

    /**
     * Crea el controlador inyectando el DAO de empresas.
     *
     * @param empresaDAO DAO de empresas
     */
    public EmpresaFormController(EmpresaJdbcDAO empresaDAO) {
        this.empresaDAO = empresaDAO;
    }

    /**
     * Se invoca automáticamente después de cargar el FXML.
     * <p>
     * Si hay una empresa en el {@link AppContext}, carga sus datos en el
     * formulario; si no, prepara uno vacío con la fecha de hoy.
     * </p>
     *
     * @param location  ubicación usada para resolver rutas relativas, o {@code null}
     * @param resources recursos usados para localizar la vista, o {@code null}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        empresa = (Empresa) AppContext.getInstance().get("empresa");
        if (empresa != null) {
            lblCabecera.setText("Editar empresa");
            txtNombre.setText(empresa.getNombre());
            txtSector.setText(empresa.getSector());
            txtWeb.setText(empresa.getWeb());
            dpFecha.setValue(empresa.getFechaRegistro());
        } else {
            lblCabecera.setText("Nueva empresa");
            dpFecha.setValue(LocalDate.now());
        }
    }

    /**
     * Valida y guarda los datos del formulario, y vuelve a la lista de empresas.
     *
     * @param actionEvent evento de acción generado al pulsar el botón guardar
     */
    @FXML
    public void save(ActionEvent actionEvent) {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String web = txtWeb.getText() == null ? "" : txtWeb.getText().trim();

        if (nombre.isEmpty()) {
            Alertas.error("El nombre es obligatorio.");
            return;
        }
        if (dpFecha.getValue() == null) {
            Alertas.error("La fecha de registro es obligatoria.");
            return;
        }
        if (!web.isEmpty() && !web.startsWith("http")) {
            Alertas.error("La web debe empezar por http:// o https://");
            return;
        }

        try {
            Optional<Empresa> duplicada = empresaDAO.findByNombre(nombre);
            boolean nombreRepetido = duplicada.isPresent()
                    && (empresa == null || !duplicada.get().getId().equals(empresa.getId()));
            if (nombreRepetido) {
                Alertas.error("Ya existe una empresa con ese nombre.");
                return;
            }

            Empresa datos = (empresa == null) ? new Empresa() : empresa;
            datos.setNombre(nombre);
            datos.setSector(txtSector.getText());
            datos.setWeb(web);
            datos.setFechaRegistro(dpFecha.getValue());

            if (empresa == null) {
                empresaDAO.save(datos);
            } else {
                empresaDAO.update(datos);
            }
        } catch (RuntimeException e) {
            Alertas.error("No se pudo guardar la empresa: " + e.getMessage());
            return;
        }
        SceneNavigator.navigateTo("empresas-view.fxml", "Empresas");
    }

    /**
     * Vuelve a la lista de empresas sin guardar los cambios.
     *
     * @param actionEvent evento de acción generado al pulsar el botón volver
     */
    @FXML
    public void goBack(ActionEvent actionEvent) {
        SceneNavigator.navigateTo("empresas-view.fxml", "Empresas");
    }
}
