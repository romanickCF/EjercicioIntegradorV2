package org.example.ejerciciointegrador.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.RequiredArgsConstructor;
import org.example.ejerciciointegrador.common.Alertas;
import org.example.ejerciciointegrador.common.AppContext;
import org.example.ejerciciointegrador.common.SceneNavigator;
import org.example.ejerciciointegrador.data.EmpresaJdbcDAO;
import org.example.ejerciciointegrador.models.Empresa;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de gestión de empresas (HU-10).
 * <p>
 * Muestra las empresas en una tabla y permite crear, editar y borrar.
 * </p>
 */
@RequiredArgsConstructor
public class EmpresasController implements Initializable {

    /** Tabla con el listado de empresas. */
    @FXML
    private TableView<Empresa> table;

    /** Columna del identificador. */
    @FXML
    private TableColumn<Empresa, String> cId;

    /** Columna del nombre. */
    @FXML
    private TableColumn<Empresa, String> cNombre;

    /** Columna del sector. */
    @FXML
    private TableColumn<Empresa, String> cSector;

    /** Columna de la web. */
    @FXML
    private TableColumn<Empresa, String> cWeb;

    /** Columna de la fecha de registro. */
    @FXML
    private TableColumn<Empresa, String> cFecha;

    /** Etiqueta con el número de empresas. */
    @FXML
    private Label lblTotal;

    /** DAO de empresas. */
    private final EmpresaJdbcDAO empresaDAO;

    /**
     * Se invoca automáticamente después de cargar el FXML.
     * <p>
     * Configura las columnas y carga todas las empresas.
     * </p>
     *
     * @param location  ubicación usada para resolver rutas relativas, o {@code null}
     * @param resources recursos usados para localizar la vista, o {@code null}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cId.setCellValueFactory(row -> new SimpleStringProperty(String.valueOf(row.getValue().getId())));
        cNombre.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getNombre()));
        cSector.setCellValueFactory(row -> new SimpleStringProperty(texto(row.getValue().getSector())));
        cWeb.setCellValueFactory(row -> new SimpleStringProperty(texto(row.getValue().getWeb())));
        cFecha.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getFechaTexto()));
        cargar();
    }

    /**
     * Abre el formulario para crear una empresa nueva.
     *
     * @param actionEvent evento de acción generado al pulsar el botón nueva
     */
    @FXML
    public void nueva(ActionEvent actionEvent) {
        AppContext.getInstance().remove("empresa");
        SceneNavigator.navigateTo("empresa-form-view.fxml", "Nueva empresa");
    }

    /**
     * Abre el formulario para editar la empresa seleccionada.
     *
     * @param actionEvent evento de acción generado al pulsar el botón editar
     */
    @FXML
    public void editar(ActionEvent actionEvent) {
        Empresa empresa = seleccionada();
        if (empresa != null) {
            AppContext.getInstance().put("empresa", empresa);
            SceneNavigator.navigateTo("empresa-form-view.fxml", "Editar empresa");
        }
    }

    /**
     * Borra la empresa seleccionada tras pedir confirmación, avisando de que
     * también se borrarán sus candidaturas y entrevistas.
     *
     * @param actionEvent evento de acción generado al pulsar el botón borrar
     */
    @FXML
    public void borrar(ActionEvent actionEvent) {
        Empresa empresa = seleccionada();
        if (empresa == null) {
            return;
        }
        try {
            int ofertas = empresaDAO.contarOfertas(empresa.getId());
            String aviso = ofertas == 0
                    ? "¿Borrar la empresa \"" + empresa.getNombre() + "\"?"
                    : "¿Borrar la empresa \"" + empresa.getNombre() + "\"?\nSe borrarán también sus "
                    + ofertas + " candidaturas y las entrevistas asociadas.";
            if (Alertas.confirmar(aviso)) {
                empresaDAO.deleteById(empresa.getId());
                cargar();
            }
        } catch (RuntimeException e) {
            Alertas.error("No se pudo borrar la empresa: " + e.getMessage());
        }
    }

    /**
     * Vuelve al listado de candidaturas.
     *
     * @param actionEvent evento de acción generado al pulsar el botón volver
     */
    @FXML
    public void goBack(ActionEvent actionEvent) {
        SceneNavigator.navigateTo("ofertas-view.fxml", "Candidaturas");
    }

    /* Métodos auxiliares */

    /**
     * Consulta todas las empresas y rellena la tabla.
     */
    private void cargar() {
        try {
            List<Empresa> empresas = empresaDAO.findAll();
            table.setItems(FXCollections.observableArrayList(empresas));
            lblTotal.setText(empresas.size() + " empresas");
        } catch (RuntimeException e) {
            Alertas.error("Error al consultar la base de datos: " + e.getMessage());
        }
    }

    /**
     * Devuelve la empresa seleccionada o avisa si no hay ninguna.
     *
     * @return la empresa seleccionada, o {@code null} si no hay selección
     */
    private Empresa seleccionada() {
        Empresa empresa = table.getSelectionModel().getSelectedItem();
        if (empresa == null) {
            Alertas.info("Selecciona primero una empresa de la tabla.");
        }
        return empresa;
    }

    /**
     * Devuelve "--" si el texto es nulo o está en blanco.
     *
     * @param valor texto a mostrar
     * @return el texto, o "--" si no hay contenido
     */
    private static String texto(String valor) {
        return (valor == null || valor.isBlank()) ? "--" : valor;
    }
}
