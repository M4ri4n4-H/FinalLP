package servicios;

import entidades.RegistroTemperatura;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class TemperaturasServicios {

    public static List<RegistroTemperatura> cargarDesdeArchivo(String ruta) {
        List<RegistroTemperatura> datos = new ArrayList<>();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            List<String> lineas = Files.readAllLines(Paths.get(ruta));
            for (int i = 1; i < lineas.size(); i++) {
                String[] partes = lineas.get(i).split(",");
                String ciudad = partes[0];
                LocalDate fecha = LocalDate.parse(partes[1], formato);
                double temperatura = Double.parseDouble(partes[2]);
                datos.add(new RegistroTemperatura(ciudad, fecha, temperatura));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datos;
    }

    public static void guardarEnArchivo(String ruta, List<RegistroTemperatura> datos) {
        List<String> lineas = new ArrayList<>();
        lineas.add("Ciudad,Fecha,Temperatura");

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (RegistroTemperatura r : datos) {
            String linea = r.getCiudad() + "," +
                    r.getFecha().format(formato) + "," +
                    r.getTemperatura();
            lineas.add(linea);
        }

        try {
            Files.write(Paths.get(ruta), lineas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void agregar(List<RegistroTemperatura> datos, RegistroTemperatura nuevo) {
        datos.add(nuevo);
    }

    public static void modificar(List<RegistroTemperatura> datos, int indice, RegistroTemperatura actualizado) {
        if (indice >= 0 && indice < datos.size()) {
            datos.set(indice, actualizado);
        }
    }

    public static void eliminar(List<RegistroTemperatura> datos, int indice) {
        if (indice >= 0 && indice < datos.size()) {
            datos.remove(indice);
        }
    }

    public static Map<String, Double> calcularPromedios(List<RegistroTemperatura> datos, LocalDate desde,
            LocalDate hasta) {
        Map<String, List<Double>> temperaturasPorCiudad = new HashMap<>();

        for (RegistroTemperatura r : datos) {
            if (!r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta)) {
                String ciudad = r.getCiudad();
                if (!temperaturasPorCiudad.containsKey(ciudad)) {
                    temperaturasPorCiudad.put(ciudad, new ArrayList<>());
                }
                temperaturasPorCiudad.get(ciudad).add(r.getTemperatura());
            }
        }

        Map<String, Double> promedios = new HashMap<>();

        for (String ciudad : temperaturasPorCiudad.keySet()) {
            List<Double> lista = temperaturasPorCiudad.get(ciudad);
            double suma = 0;
            for (double t : lista) {
                suma += t;
            }
            promedios.put(ciudad, suma / lista.size());
        }

        return promedios;
    }

    public static DefaultCategoryDataset getDatosGraficaBarras(Map<String, Double> promedios) {
        DefaultCategoryDataset datos = new DefaultCategoryDataset();

        for (String ciudad : promedios.keySet()) {
            double valor = promedios.get(ciudad);
            datos.addValue(valor, "Promedio", ciudad);
        }

        return datos;
    }

    public static JFreeChart getGraficaBarras(DefaultCategoryDataset datos, String titulo) {
        return ChartFactory.createBarChart(
                titulo,
                "Ciudad",
                "Temperatura promedio (°C)",
                datos);
    }

    public static void mostrarGraficaBarras(JPanel pnl, JFreeChart grafica) {
        pnl.removeAll();
        ChartPanel panel = new ChartPanel(grafica);
        panel.setPreferredSize(new Dimension(pnl.getWidth(), pnl.getHeight()));
        panel.setMouseWheelEnabled(true);
        pnl.setLayout(new BorderLayout());
        pnl.add(panel, BorderLayout.CENTER);
        pnl.validate();
    }

    public static Map<String, RegistroTemperatura> encontrarExtremosPorFecha(
            List<RegistroTemperatura> datos, LocalDate fecha) {

        RegistroTemperatura max = null;
        RegistroTemperatura min = null;

        for (RegistroTemperatura r : datos) {
            if (r.getFecha().equals(fecha)) {
                if (max == null || r.getTemperatura() > max.getTemperatura()) {
                    max = r;
                }
                if (min == null || r.getTemperatura() < min.getTemperatura()) {
                    min = r;
                }
            }
        }

        Map<String, RegistroTemperatura> resultado = new HashMap<>();
        resultado.put("max", max);
        resultado.put("min", min);
        return resultado;
    }

    private static boolean esMayor(RegistroTemperatura r1, RegistroTemperatura r2) {
        int compCiudad = r1.getCiudad().compareTo(r2.getCiudad());
        if (compCiudad > 0)
            return true;
        if (compCiudad == 0) {
            return r1.getFecha().isAfter(r2.getFecha());
        }
        return false;
    }

    private static void intercambiar(List<RegistroTemperatura> lista, int i, int j) {
        if (i >= 0 && j >= 0 && i < lista.size() && j < lista.size()) {
            RegistroTemperatura temp = lista.get(i);
            lista.set(i, lista.get(j));
            lista.set(j, temp);
        }
    }

    private static int getPivote(List<RegistroTemperatura> lista, int inicio, int fin) {
        int pivote = inicio;
        RegistroTemperatura ref = lista.get(pivote);

        for (int i = inicio + 1; i <= fin; i++) {
            if (esMayor(ref, lista.get(i))) {
                pivote++;
                if (i != pivote) {
                    intercambiar(lista, i, pivote);
                }
            }
        }
        if (inicio != pivote) {
            intercambiar(lista, inicio, pivote);
        }

        return pivote;
    }

    private static void ordenarRapido(List<RegistroTemperatura> lista, int inicio, int fin) {
        if (fin > inicio) {
            int pivote = getPivote(lista, inicio, fin);
            ordenarRapido(lista, inicio, pivote - 1);
            ordenarRapido(lista, pivote + 1, fin);
        }
    }

    public static void ordenarPorCiudadYFecha(List<RegistroTemperatura> lista) {
        ordenarRapido(lista, 0, lista.size() - 1);
    }
}