import entidades.RegistroTemperatura;
import servicios.TemperaturasServicios;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import datechooser.beans.DateChooserCombo;
import java.time.LocalDate;
import java.time.ZoneId;

public class FrmTemperaturas extends JFrame {
    private DateChooserCombo dccDesde, dccHasta;

    private JTabbedPane tpCambiosMoneda;
    private JPanel pnlGrafica;
    private JPanel pnlConsultas;
    private JTable tblDatos;
    private DefaultTableModel modeloTabla;
    private List<RegistroTemperatura> datos;
    private final String RUTA_ARCHIVO = "src/datos/Temperaturas.csv";
    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;
    private JTextField txtFechaConsulta;
    private JTextArea txtResultados;

    public FrmTemperaturas() {

        setTitle("Registro de Temperaturas");
        setSize(900, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        datos = TemperaturasServicios.cargarDesdeArchivo(RUTA_ARCHIVO);

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

        JPanel pnlPrincipal = new JPanel();
        pnlPrincipal.setLayout(new BoxLayout(pnlPrincipal, BoxLayout.Y_AXIS));

        JPanel pnlTabla = new JPanel(new BorderLayout());

        modeloTabla = new DefaultTableModel(
                new Object[] { "Ciudad", "Fecha", "Temperatura (°C)" }, 0);
        tblDatos = new JTable(modeloTabla);
        actualizarTabla();
        pnlTabla.add(new JScrollPane(tblDatos), BorderLayout.CENTER);

        tpCambiosMoneda = new JTabbedPane();
        tpCambiosMoneda.addTab("Datos", pnlTabla);

        pnlGrafica = new JPanel(new BorderLayout());
        tpCambiosMoneda.addTab("Gráfica", pnlGrafica);

        pnlConsultas = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JPanel pnlConsultaFecha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlConsultaFecha.add(new JLabel("Consultar fecha:"));
        DateChooserCombo dccConsulta = new DateChooserCombo();
        pnlConsultaFecha.add(dccConsulta);
        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.addActionListener(e -> {
            if (dccConsulta.getSelectedDate() != null) {
                LocalDate fecha = dccConsulta.getSelectedDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                consultarFechaEspecifica(fecha);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Seleccione una fecha",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        pnlConsultaFecha.add(btnConsultar);

        txtResultados = new JTextArea(5, 50);
        txtResultados.setEditable(false);
        JScrollPane scrollResultados = new JScrollPane(txtResultados);

        setTitle("Registro de Temperaturas");
        setSize(900, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Cargar datos predeterminados al inicio
        datos = TemperaturasServicios.cargarDesdeArchivo(RUTA_ARCHIVO);


        // Nuevo diseño simplificado
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        pnlConsultas.add(new JLabel("Ciudades extremas por fecha:"), gbc);

        gbc.gridy = 1;
        pnlConsultas.add(pnlConsultaFecha, gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        pnlConsultas.add(scrollResultados, gbc);

        tpCambiosMoneda.addTab("Consultas", pnlConsultas);

        pnlPrincipal.add(tpCambiosMoneda);

        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(pnlPrincipal, BorderLayout.CENTER);

        TemperaturasServicios.ordenarPorCiudadYFecha(datos);
        actualizarTabla();

    }

    private void actualizarTabla() {

        modeloTabla.setRowCount(0);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (RegistroTemperatura r : datos) {
            modeloTabla.addRow(new Object[] {
                    r.getCiudad(),
                    r.getFecha().format(formato),
                    r.getTemperatura()
            });
        }
    }

    private void agregarDato(ActionEvent evt) {
        JPanel panel = crearPanelEdicion(null);

        int resultado = JOptionPane.showConfirmDialog(
                this, panel, "Agregar Registro",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                Component[] components = panel.getComponents();
                String ciudad = ((JTextField) components[1]).getText();
                LocalDate fecha = ((DateChooserCombo) components[3]).getSelectedDate()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                double temperatura = Double.parseDouble(((JTextField) components[5]).getText());

                RegistroTemperatura nuevo = new RegistroTemperatura(ciudad, fecha, temperatura);
                TemperaturasServicios.agregar(datos, nuevo);
                TemperaturasServicios.ordenarPorCiudadYFecha(datos);
                actualizarTabla();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage(),
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
        JPanel panel = crearPanelEdicion(registro);

        int resultado = JOptionPane.showConfirmDialog(
                this, panel, "Modificar Registro",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                Component[] components = panel.getComponents();
                String ciudad = ((JTextField) components[1]).getText();
                LocalDate fecha = ((DateChooserCombo) components[3]).getSelectedDate()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                double temperatura = Double.parseDouble(((JTextField) components[5]).getText());

                RegistroTemperatura actualizado = new RegistroTemperatura(ciudad, fecha, temperatura);
                TemperaturasServicios.modificar(datos, fila, actualizado);
                TemperaturasServicios.ordenarPorCiudadYFecha(datos);
                actualizarTabla();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage(),
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
        // Crear panel de fechas si no existe
        if (dccDesde == null) {
            dccDesde = new DateChooserCombo();
            dccHasta = new DateChooserCombo();

            JPanel pnlFechas = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlFechas.add(new JLabel("Desde:"));
            pnlFechas.add(dccDesde);
            pnlFechas.add(new JLabel("Hasta:"));
            pnlFechas.add(dccHasta);

            JButton btnGenerar = new JButton("Generar Gráfica");
            btnGenerar.addActionListener(e -> generarGrafica());
            pnlFechas.add(btnGenerar);

            // Añadir el panel de fechas al NORTH del panel principal de gráfica
            pnlGrafica.add(pnlFechas, BorderLayout.NORTH);
        }

        // Cambiar a la pestaña de la gráfica
        tpCambiosMoneda.setSelectedIndex(1);
    }

    private void generarGrafica() {
        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (desde == null || hasta == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione ambas fechas.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, Double> promedios = TemperaturasServicios.calcularPromedios(datos, desde, hasta);

        if (promedios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos en el rango seleccionado.", "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DefaultCategoryDataset dataset = TemperaturasServicios.getDatosGraficaBarras(promedios);
        JFreeChart grafica = TemperaturasServicios.getGraficaBarras(dataset,
                "Promedio de Temperaturas: " + desde + " a " + hasta);

        // Crear un panel para la gráfica (sin afectar el panel de fechas)
        JPanel pnlContenedorGrafica = new JPanel(new BorderLayout());
        ChartPanel panelGrafica = new ChartPanel(grafica);
        pnlContenedorGrafica.add(panelGrafica, BorderLayout.CENTER);

        // Limpiar solo el CENTER del panel principal (conservando NORTH con las fechas)
        pnlGrafica.removeAll();
        pnlGrafica.add(pnlContenedorGrafica, BorderLayout.CENTER);

        // Si el panel de fechas no está, lo volvemos a añadir
        if (dccDesde != null) {
            JPanel pnlFechas = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlFechas.add(new JLabel("Desde:"));
            pnlFechas.add(dccDesde);
            pnlFechas.add(new JLabel("Hasta:"));
            pnlFechas.add(dccHasta);

            JButton btnGenerar = new JButton("Generar Gráfica");
            btnGenerar.addActionListener(e -> generarGrafica());
            pnlFechas.add(btnGenerar);

            pnlGrafica.add(pnlFechas, BorderLayout.NORTH);
        }

        pnlGrafica.revalidate();
        pnlGrafica.repaint();
    }

    // nuevo helper
    private JPanel crearPanelEdicion(RegistroTemperatura registroExistente) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        // Campo Ciudad
        JTextField txtCiudad = new JTextField();
        if (registroExistente != null) {
            txtCiudad.setText(registroExistente.getCiudad());
        }

        // Selector de Fecha (DateChooserCombo)
        DateChooserCombo dccFecha = new DateChooserCombo();
        if (registroExistente != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(registroExistente.getFecha()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
            dccFecha.setSelectedDate(calendar); // Ahora usando Calendar
        }

        // Campo Temperatura
        JTextField txtTemperatura = new JTextField();
        if (registroExistente != null) {
            txtTemperatura.setText(String.valueOf(registroExistente.getTemperatura()));
        }

        panel.add(new JLabel("Ciudad:"));
        panel.add(txtCiudad);
        panel.add(new JLabel("Fecha:"));
        panel.add(dccFecha);
        panel.add(new JLabel("Temperatura (°C):"));
        panel.add(txtTemperatura);

        return panel;
    }
    //

    private void consultarFechaEspecifica(LocalDate fecha) {
        try {
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
                    "Error al consultar fecha",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
}