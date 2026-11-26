package org.example.ejerciciointegrador.controllers;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import lombok.RequiredArgsConstructor;
import org.example.ejerciciointegrador.common.Alertas;
import org.example.ejerciciointegrador.common.AppContext;
import org.example.ejerciciointegrador.common.SceneNavigator;
import org.example.ejerciciointegrador.data.EmpresaJdbcDAO;
import org.example.ejerciciointegrador.data.EntrevistaJdbcDAO;
import org.example.ejerciciointegrador.data.OfertaJdbcDAO;
import org.example.ejerciciointegrador.models.Empresa;
import org.example.ejerciciointegrador.models.Entrevista;
import org.example.ejerciciointegrador.models.Oferta;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de detalle de una oferta.
 * <p>
 * Muestra toda la información de la candidatura, los datos de su empresa y,
 * dentro de un panel expandible, la lista de entrevistas con sus acciones de
 * alta, edición y borrado (HU-11).
 * </p>
 */
@RequiredArgsConstructor
public class OfertaDetailController implements Initializable {

    /** Título de la oferta. */
    @FXML
    private Label lblTitulo;

    /** Estado de la candidatura. */
    @FXML
    private Label lblEstado;

    /** Fecha de postulación. */
    @FXML
    private Label lblFecha;

    /** Salario de la oferta. */
    @FXML
    private Label lblSalario;

    /** Enlace a la oferta. */
    @FXML
    private Label lblEnlace;

    /** Descripción de la oferta. */
    @FXML
    private Label lblDescripcion;

    /** Notas de la candidatura. */
    @FXML
    private Label lblNotas;

    /** Nombre de la empresa. */
    @FXML
    private Label lblEmpresa;

    /** Sector de la empresa. */
    @FXML
    private Label lblSector;

    /** Web de la empresa. */
    @FXML
    private Label lblWeb;

    /** Fecha de registro de la empresa. */
    @FXML
    private Label lblRegistro;

    /** Panel expandible con las entrevistas. */
    @FXML
    private TitledPane paneEntrevistas;

    /** Tabla de entrevistas de la oferta. */
    @FXML
    private TableView<Entrevista> tableEntrevistas;

    /** Columna de la fecha y hora. */
    @FXML
    private TableColumn<Entrevista, String> cEFecha;

    /** Columna del tipo de entrevista. */
    @FXML
    private TableColumn<Entrevista, String> cETipo;

    /** Columna del resultado. */
    @FXML
    private TableColumn<Entrevista, String> cEResultado;

    /** Columna de las notas. */
    @FXML
    private TableColumn<Entrevista, String> cENotas;

    /** DAO de ofertas. */
    private final OfertaJdbcDAO ofertaDAO;

    /** DAO de empresas. */
    private final EmpresaJdbcDAO empresaDAO;

    /** DAO de entrevistas. */
    private final EntrevistaJdbcDAO entrevistaDAO;

    /** Oferta que se está mostrando. */
    private Oferta oferta;

    /**
     * Se invoca automáticamente después de cargar el FXML.
     * <p>
     * Recupera la oferta seleccionada del {@link AppContext}, la vuelve a leer
     * de la base de datos para tener los datos actualizados y rellena la
     * pantalla.
     * </p>
     *
     * @param location  ubicación usada para resolver rutas relativas, o {@code null}
     * @param resources recursos usados para localizar la vista, o {@code null}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cEFecha.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getFechaTexto()));
        cETipo.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTipo()));
        cEResultado.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getResultado()));
        cENotas.setCellValueFactory(row -> new SimpleStringProperty(texto(row.getValue().getNotas())));

        Oferta seleccionada = (Oferta) AppContext.getInstance().get("oferta");
        oferta = (seleccionada == null) ? null : ofertaDAO.findById(seleccionada.getId()).orElse(null);
        if (oferta == null) {
            Alertas.error("La candidatura ya no existe.");
            SceneNavigator.navigateTo("ofertas-view.fxml", "Candidaturas");
            return;
        }

        lblTitulo.setText(oferta.getTitulo());
        lblEstado.setText(oferta.getEstado());
        lblFecha.setText(oferta.getFechaTexto());
        lblSalario.setText(oferta.getSalarioTexto());
        lblEnlace.setText(texto(oferta.getEnlace()));
        lblDescripcion.setText(texto(oferta.getDescripcion()));
        lblNotas.setText(texto(oferta.getNotas()));

        Empresa empresa = empresaDAO.findById(oferta.getIdEmpresa()).orElse(null);
        if (empresa != null) {
            lblEmpresa.setText(empresa.getNombre());
            lblSector.setText(texto(empresa.getSector()));
            lblWeb.setText(texto(empresa.getWeb()));
            lblRegistro.setText(empresa.getFechaTexto());
        }

        cargarEntrevistas();
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

    /**
     * Abre el formulario para editar la oferta mostrada.
     *
     * @param actionEvent evento de acción generado al pulsar el botón editar
     */
    @FXML
    public void editarOferta(ActionEvent actionEvent) {
        AppContext.getInstance().put("oferta", oferta);
        AppContext.getInstance().put("origen", "oferta-detail-view.fxml");
        SceneNavigator.navigateTo("oferta-form-view.fxml", "Editar candidatura");
    }

    /**
     * Borra la oferta mostrada tras pedir confirmación y vuelve al listado.
     *
     * @param actionEvent evento de acción generado al pulsar el botón borrar
     */
    @FXML
    public void borrarOferta(ActionEvent actionEvent) {
        if (Alertas.confirmar("¿Borrar la candidatura \"" + oferta.getTitulo()
                + "\" y sus entrevistas?")) {
            try {
                ofertaDAO.deleteById(oferta.getId());
                SceneNavigator.navigateTo("ofertas-view.fxml", "Candidaturas");
            } catch (RuntimeException e) {
                Alertas.error("No se pudo borrar la candidatura: " + e.getMessage());
            }
        }
    }

    /**
     * Abre el formulario para añadir una entrevista a esta oferta.
     *
     * @param actionEvent evento de acción generado al pulsar el botón añadir
     */
    @FXML
    public void nuevaEntrevista(ActionEvent actionEvent) {
        AppContext.getInstance().put("oferta", oferta);
        AppContext.getInstance().remove("entrevista");
        SceneNavigator.navigateTo("entrevista-form-view.fxml", "Nueva entrevista");
    }

    /**
     * Abre el formulario para editar la entrevista seleccionada.
     *
     * @param actionEvent evento de acción generado al pulsar el botón editar
     */
    @FXML
    public void editarEntrevista(ActionEvent actionEvent) {
        Entrevista entrevista = entrevistaSeleccionada();
        if (entrevista != null) {
            AppContext.getInstance().put("oferta", oferta);
            AppContext.getInstance().put("entrevista", entrevista);
            SceneNavigator.navigateTo("entrevista-form-view.fxml", "Editar entrevista");
        }
    }

    /**
     * Borra la entrevista seleccionada tras pedir confirmación.
     *
     * @param actionEvent evento de acción generado al pulsar el botón borrar
     */
    @FXML
    public void borrarEntrevista(ActionEvent actionEvent) {
        Entrevista entrevista = entrevistaSeleccionada();
        if (entrevista != null && Alertas.confirmar("¿Borrar la entrevista del "
                + entrevista.getFechaTexto() + "?")) {
            try {
                entrevistaDAO.deleteById(entrevista.getId());
                cargarEntrevistas();
            } catch (RuntimeException e) {
                Alertas.error("No se pudo borrar la entrevista: " + e.getMessage());
            }
        }
    }

    /* Métodos auxiliares */

    /**
     * Carga en la tabla las entrevistas de la oferta y actualiza el título del panel.
     */
    private void cargarEntrevistas() {
        List<Entrevista> entrevistas = entrevistaDAO.findByOferta(oferta.getId());
        tableEntrevistas.setItems(FXCollections.observableArrayList(entrevistas));
        paneEntrevistas.setText("Entrevistas (" + entrevistas.size() + ")");
    }

    /**
     * Devuelve la entrevista seleccionada o avisa si no hay ninguna.
     *
     * @return la entrevista seleccionada, o {@code null} si no hay selección
     */
    private Entrevista entrevistaSeleccionada() {
        Entrevista entrevista = tableEntrevistas.getSelectionModel().getSelectedItem();
        if (entrevista == null) {
            Alertas.info("Selecciona primero una entrevista de la tabla.");
        }
        return entrevista;
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
