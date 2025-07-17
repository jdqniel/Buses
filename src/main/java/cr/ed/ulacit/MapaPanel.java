package cr.ed.ulacit;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * El panel de visualización principal para la simulación.
 * <p>
 * Diseño: Esta clase extiende {@link JPanel} y su única responsabilidad es dibujar el estado actual
 * de la simulación. No contiene ninguna lógica de simulación; simplemente recibe los datos
 * (la lista de autobuses y la ruta) y los renderiza.
 * - Carga de recursos: La imagen del mapa se carga desde el classpath de recursos, lo que desacopla
 *   el código de la ubicación del archivo en el sistema de ficheros.
 * - Renderizado: El método {@link #paintComponent(Graphics)} se encarga de todo el dibujo.
 *   Se utiliza {@link Graphics2D} para acceder a opciones de renderizado avanzadas como el antialiasing,
 *   mejorando la calidad visual de las líneas y formas.
 */
public class MapaPanel extends JPanel {

    private final Ruta ruta;
    private final List<Autobus> autobuses;
    private BufferedImage mapaImagen;

    /**
     * Constructor del panel del mapa.
     *
     * @param autobuses La lista de autobuses a dibujar.
     * @param ruta La ruta (con sus paradas) a dibujar.
     */
    public MapaPanel(List<Autobus> autobuses, Ruta ruta) {
        this.autobuses = autobuses;
        this.ruta = ruta;
        cargarImagenDeMapa();
    }

    /**
     * Carga la imagen de fondo del mapa desde los recursos del proyecto.
     * Si no se encuentra, imprime un error pero permite que la simulación continúe sin fondo.
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
     * El corazón del renderizado. Este método es llamado por Swing cada vez que el panel necesita ser redibujado.
     * Dibuja el mapa de fondo, luego la ruta, las paradas y finalmente los autobuses.
     *
     * @param g El contexto gráfico proporcionado por Swing para dibujar.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. Dibuja la imagen de fondo del mapa
        if (mapaImagen != null) {
            g2d.drawImage(mapaImagen, 0, 0, getWidth(), getHeight(), this);
        }

        // Activa el antialiasing para suavizar las formas y líneas
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 2. Dibuja la ruta y las paradas
        if (ruta != null) {
            // Dibuja las líneas de la ruta
            g2d.setColor(new Color(0, 0, 255, 150)); // Azul semitransparente
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < ruta.getParadas().size() - 1; i++) {
                Parada p1 = ruta.getParadas().get(i);
                Parada p2 = ruta.getParadas().get(i + 1);
                g2d.drawLine(p1.getCoordX(), p1.getCoordY(), p2.getCoordX(), p2.getCoordY());
            }

            // Dibuja las paradas como cuadrados y sus nombres
            g2d.setStroke(new BasicStroke(1));
            for (Parada parada : ruta.getParadas()) {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(parada.getCoordX() - 5, parada.getCoordY() - 5, 10, 10);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(parada.getNombre(), parada.getCoordX() + 12, parada.getCoordY() + 5);
            }
        }

        // 3. Dibuja los autobuses
        for (Autobus bus : autobuses) {
            // Solo dibuja los autobuses que están en ruta o han finalizado
            if (bus.getEstado() != Autobus.EstadoAutobus.INACTIVO) {
                g2d.setColor(bus.getColor());
                g2d.fillOval(bus.getX() - 10, bus.getY() - 10, 20, 20); // Círculo del bus
                g2d.setColor(Color.BLACK);
                g2d.drawOval(bus.getX() - 10, bus.getY() - 10, 20, 20); // Borde del círculo
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(bus.getId()), bus.getX() - 3, bus.getY() + 4); // ID del bus
            }
        }
    }
}
