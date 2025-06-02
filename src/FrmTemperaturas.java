import entidades.RegistroTemperatura;
import servicios.TemperaturasServicios;

import javax.swing.*;
import javax.swing.table.DefaultTableModel; // Importación faltante

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class FrmTemperaturas extends JFrame {

    private JTabbedPane tpCambiosMoneda;
    private JPanel pnlGrafica;
    private JPanel pnlConsultas;
    private JTable tblDatos;
    private DefaultTableModel modeloTabla; // Ahora debería funcionar
    private List<RegistroTemperatura> datos;
    private final String RUTA_ARCHIVO = "datos/Temperaturas.csv";
    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;
    private JTextField txtFechaConsulta;
    private JTextArea txtResultados;

    public FrmTemperaturas() {
        setTitle("Registro de Temperaturas");
        setSize(900, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Cargar datos iniciales
        datos = TemperaturasServicios.cargarDesdeArchivo(RUTA_ARCHIVO);

        // Configurar barra de herramientas
        JToolBar tb = new JToolBar();

        JButton btnAgregar = new JButton();
        btnAgregar.setIcon(new ImageIcon(getClass().getResource("/iconos/agregar.png")));
        btnAgregar.setToolTipText("Agregar dato");
        btnAgregar.addActionListener(this::agregarDato);
        tb.add(btnAgregar);

        JButton btnEliminar = new JButton();
        btnEliminar.setIcon(new ImageIcon(getClass().getResource("/iconos/eliminar.png")));
        btnEliminar.setToolTipText("Eliminar dato");
        btnEliminar.addActionListener(this::eliminarDato);
        tb.add(btnEliminar);

        JButton btnGuardar = new JButton();
        btnGuardar.setIcon(new ImageIcon(getClass().getResource("/iconos/guardar.png")));
        btnGuardar.setToolTipText("Guardar");
        btnGuardar.addActionListener(this::guardarCambios);
        tb.add(btnGuardar);

        JButton btnModificar = new JButton();
        btnModificar.setIcon(new ImageIcon(getClass().getResource("/iconos/modificar.png")));
        btnModificar.setToolTipText("Modificar Dato");
        btnModificar.addActionListener(this::modificarDato);
        tb.add(btnModificar);

        JButton btnGraficar = new JButton();
        btnGraficar.setIcon(new ImageIcon(getClass().getResource("/iconos/graficar.png")));
        btnGraficar.setToolTipText("Graficar promedios");
        btnGraficar.addActionListener(this::mostrarGrafica);
        tb.add(btnGraficar);

        // Panel principal con BoxLayout
        JPanel pnlPrincipal = new JPanel();
        pnlPrincipal.setLayout(new BoxLayout(pnlPrincipal, BoxLayout.Y_AXIS));

        // Panel superior para controles
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlControles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Tabla de datos
        JPanel pnlTabla = new JPanel(new BorderLayout());
        // Crear el modelo de tabla con columnas y 0 filas inicialmente
        modeloTabla = new DefaultTableModel(
                new Object[] { "Ciudad", "Fecha", "Temperatura (°C)" }, 0);
        tblDatos = new JTable(modeloTabla);
        actualizarTabla();
        pnlTabla.add(new JScrollPane(tblDatos), BorderLayout.CENTER);

        // Pestañas
        tpCambiosMoneda = new JTabbedPane();
        tpCambiosMoneda.addTab("Datos", pnlTabla);

        pnlGrafica = new JPanel(new BorderLayout());
        tpCambiosMoneda.addTab("Gráfica", pnlGrafica);

        pnlConsultas = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Panel para rango de fechas (gráfica)
        JPanel pnlRangoFechas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlRangoFechas.add(new JLabel("Rango (dd/MM/yyyy):"));
        txtFechaDesde = new JTextField(10);
        pnlRangoFechas.add(txtFechaDesde);
        pnlRangoFechas.add(new JLabel("a"));
        txtFechaHasta = new JTextField(10);
        pnlRangoFechas.add(txtFechaHasta);

        // Panel para consulta por fecha
        JPanel pnlConsultaFecha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlConsultaFecha.add(new JLabel("Consultar fecha (dd/MM/yyyy):"));
        txtFechaConsulta = new JTextField(10);
        pnlConsultaFecha.add(txtFechaConsulta);
        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.addActionListener(this::consultarFechaEspecifica);
        pnlConsultaFecha.add(btnConsultar);

        // Área de resultados
        txtResultados = new JTextArea(5, 50);
        txtResultados.setEditable(false);
        JScrollPane scrollResultados = new JScrollPane(txtResultados);

        // Configurar layout de consultas
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        pnlConsultas.add(new JLabel("Gráfica de promedios:"), gbc);

        gbc.gridy = 1;
        pnlConsultas.add(pnlRangoFechas, gbc);

        gbc.gridy = 2;
        pnlConsultas.add(new JLabel("Ciudades extremas por fecha:"), gbc);

        gbc.gridy = 3;
        pnlConsultas.add(pnlConsultaFecha, gbc);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        pnlConsultas.add(scrollResultados, gbc);

        tpCambiosMoneda.addTab("Consultas", pnlConsultas);

        // Agregar componentes al panel principal
        pnlPrincipal.add(pnlControles);
        pnlPrincipal.add(tpCambiosMoneda);

        // Agregar al frame
        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(pnlPrincipal, BorderLayout.CENTER);

        actualizarTabla();

    }

    private void actualizarTabla() {
        // Limpiar la tabla
        modeloTabla.setRowCount(0);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        System.out.println("Cargando " + datos.size() + " filas en la tabla");

        // Llenar la tabla con los datos
        for (RegistroTemperatura r : datos) {
            modeloTabla.addRow(new Object[] {
                    r.getCiudad(),
                    r.getFecha().format(formato),
                    r.getTemperatura()
            });
        }
    }

    private void agregarDato(ActionEvent evt) {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField txtCiudad = new JTextField();
        JTextField txtFecha = new JTextField();
        JTextField txtTemperatura = new JTextField();

        panel.add(new JLabel("Ciudad:"));
        panel.add(txtCiudad);
        panel.add(new JLabel("Fecha (dd/MM/yyyy):"));
        panel.add(txtFecha);
        panel.add(new JLabel("Temperatura:"));
        panel.add(txtTemperatura);

        int resultado = JOptionPane.showConfirmDialog(
                this, panel, "Agregar Registro",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                RegistroTemperatura nuevo = new RegistroTemperatura(
                        txtCiudad.getText(),
                        LocalDate.parse(txtFecha.getText(), formato),
                        Double.parseDouble(txtTemperatura.getText()));
                TemperaturasServicios.agregar(datos, nuevo);
                actualizarTabla();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error en formato: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modificarDato(ActionEvent evt) {
        int fila = tblDatos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un registro para modificar",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RegistroTemperatura registro = datos.get(fila);
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField txtCiudad = new JTextField(registro.getCiudad());
        JTextField txtFecha = new JTextField(registro.getFecha()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JTextField txtTemperatura = new JTextField(String.valueOf(registro.getTemperatura()));

        panel.add(new JLabel("Ciudad:"));
        panel.add(txtCiudad);
        panel.add(new JLabel("Fecha (dd/MM/yyyy):"));
        panel.add(txtFecha);
        panel.add(new JLabel("Temperatura:"));
        panel.add(txtTemperatura);

        int resultado = JOptionPane.showConfirmDialog(
                this, panel, "Modificar Registro",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                RegistroTemperatura actualizado = new RegistroTemperatura(
                        txtCiudad.getText(),
                        LocalDate.parse(txtFecha.getText(), formato),
                        Double.parseDouble(txtTemperatura.getText()));
                TemperaturasServicios.modificar(datos, fila, actualizado);

                actualizarTabla();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error en formato: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarDato(ActionEvent evt) {
        int fila = tblDatos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un registro para eliminar",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Eliminar este registro?", "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            TemperaturasServicios.eliminar(datos, fila);
            actualizarTabla();
        }
    }

    private void guardarCambios(ActionEvent evt) {
        TemperaturasServicios.guardarEnArchivo(RUTA_ARCHIVO, datos);
        JOptionPane.showMessageDialog(this,
                "Datos guardados correctamente",
                "Guardar", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarGrafica(ActionEvent evt) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            LocalDate desde = LocalDate.parse(txtFechaDesde.getText(), formato);
            LocalDate hasta = LocalDate.parse(txtFechaHasta.getText(), formato);

            Map<String, Double> promedios = TemperaturasServicios.calcularPromedios(
                    datos, desde, hasta);

            if (promedios.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No hay datos en el rango seleccionado",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            DefaultCategoryDataset dataset = TemperaturasServicios.getDatosGraficaBarras(promedios);
            JFreeChart grafica = TemperaturasServicios.getGraficaBarras(
                    dataset, "Promedio de Temperaturas: " + desde + " a " + hasta);
            TemperaturasServicios.mostrarGraficaBarras(pnlGrafica, grafica);
            tpCambiosMoneda.setSelectedIndex(1); // Cambiar a pestaña de gráfica

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha inválido. Use dd/MM/yyyy",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void consultarFechaEspecifica(ActionEvent evt) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            LocalDate fecha = LocalDate.parse(txtFechaConsulta.getText(), formato);

            Map<String, RegistroTemperatura> extremos = TemperaturasServicios.encontrarExtremosPorFecha(datos, fecha);

            txtResultados.setText("");
            RegistroTemperatura max = extremos.get("max");
            RegistroTemperatura min = extremos.get("min");

            if (max == null || min == null) {
                txtResultados.setText("No hay datos para la fecha seleccionada.");
                return;
            }

            txtResultados.append("Ciudad más calurosa: " + max.getCiudad() + " (" + max.getTemperatura() + "°C)\n");
            txtResultados.append("Ciudad menos calurosa: " + min.getCiudad() + " (" + min.getTemperatura() + "°C)");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha inválido. Use dd/MM/yyyy",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}