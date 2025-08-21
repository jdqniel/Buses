package cr.ed.ulacit;

import cr.ed.ulacit.dto.AutobusDTO;
import cr.ed.ulacit.dto.ParadaDTO;
import cr.ed.ulacit.dto.RutaDTO;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Un panel de Swing que renderiza el estado de la simulación de autobuses.
 * <p>
 * Esta clase es responsable de toda la representación gráfica:
 * <ul>
 *     <li>Dibuja una imagen de fondo como mapa.</li>
 *     <li>Dibuja la ruta de los autobuses como una serie de líneas y cuadrados.</li>
 *     <li>Dibuja cada autobús como un óvalo de color en su posición actual.</li>
 * </ul>
 * El panel se actualiza a través de los métodos {@code setRuta} y {@code setAutobuses},
 * que son llamados por la {@link ClienteGUI} cuando se reciben nuevos datos del servidor.
 * </p>
 */
public class MapaPanel extends JPanel {

    private RutaDTO ruta;
    private List<AutobusDTO> autobuses;
    private BufferedImage mapaImagen;

    /**
     * Constructor del panel del mapa.
     *
     * @param autobuses La lista inicial de autobuses a dibujar (puede estar vacía).
     * @param ruta      La ruta inicial a dibujar (puede ser nula).
     */
    public MapaPanel(List<AutobusDTO> autobuses, RutaDTO ruta) {
        this.autobuses = autobuses;
        this.ruta = ruta;
        cargarImagenDeMapa();
    }

    /**
     * Carga la imagen del mapa desde los recursos del proyecto.
     * Si no se encuentra la imagen, se imprime un error en la consola.
     */
    private void cargarImagenDeMapa() {
        try (InputStream is = getClass().getResourceAsStream("/cr/ed/ulacit/mapa_cr.png")) {
            if (is == null) {
                System.err.println("No se pudo encontrar el recurso del mapa. Asegúrate de que 'mapa_cr.png' esté en 'src/main/resources/cr/ed/ulacit'.");
                mapaImagen = null;
            } else {
                mapaImagen = ImageIO.read(is);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar la imagen del mapa: " + e.getMessage());
            mapaImagen = null;
        }
    }

    /**
     * El método principal de dibujado de Swing. Se llama automáticamente cuando el panel necesita ser repintado.
     *
     * @param g El contexto gráfico en el que dibujar.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Dibuja el mapa de fondo
        if (mapaImagen != null) {
            g2d.drawImage(mapaImagen, 0, 0, getWidth(), getHeight(), this);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibuja la ruta
        if (ruta != null) {
            g2d.setColor(new Color(0, 0, 255, 150));
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < ruta.getParadas().size() - 1; i++) {
                ParadaDTO p1 = ruta.getParadas().get(i);
                ParadaDTO p2 = ruta.getParadas().get(i + 1);
                g2d.drawLine(p1.getCoordX(), p1.getCoordY(), p2.getCoordX(), p2.getCoordY());
            }

            // Dibuja las paradas
            g2d.setStroke(new BasicStroke(1));
            for (ParadaDTO parada : ruta.getParadas()) {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(parada.getCoordX() - 5, parada.getCoordY() - 5, 10, 10);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(parada.getNombre(), parada.getCoordX() + 12, parada.getCoordY() + 5);
            }
        }

        // Dibuja los autobuses
        for (AutobusDTO bus : autobuses) {
            if (bus.getEstado() != EstadoAutobus.INACTIVO) {
                g2d.setColor(bus.getColor());
                g2d.fillOval(bus.getX() - 10, bus.getY() - 10, 20, 20);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(bus.getX() - 10, bus.getY() - 10, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(bus.getId()), bus.getX() - 3, bus.getY() + 4);
            }
        }
    }

    /**
     * Actualiza la ruta que se muestra en el mapa y solicita un repintado.
     *
     * @param ruta El nuevo DTO de la ruta.
     */
    public void setRuta(RutaDTO ruta) {
        this.ruta = ruta;
        repaint();
    }

    /**
     * Actualiza la lista de autobuses que se muestran en el mapa y solicita un repintado.
     *
     * @param autobuses La nueva lista de DTOs de autobuses.
     */
    public void setAutobuses(List<AutobusDTO> autobuses) {
        this.autobuses = autobuses;
        repaint();
    }
}
