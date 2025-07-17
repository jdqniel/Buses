package cr.ed.ulacit;

/**
 * Representa una parada de autobús en el mapa.
 * <p>
 * Esta clase es un modelo de datos simple (POJO) que contiene la información esencial de una parada:
 * su identificador, su nombre y sus coordenadas (x, y) para la representación gráfica en el {@link MapaPanel}.
 * <p>
 * Diseño: Se optó por la inmutabilidad para los atributos (ID, nombre, coordenadas) asignándolos
 * únicamente en el constructor. Esto garantiza que una vez creada una parada, su estado no puede ser
 * modificado, lo que simplifica el manejo de datos en un entorno de simulación.
 */
public class Parada {
    private final int id;
    private final String nombre;
    private final int coordX; // Coordenada X en el panel gráfico
    private final int coordY; // Coordenada Y en el panel gráfico

    /**
     * Constructor para crear una nueva parada.
     *
     * @param id     El identificador único de la parada.
     * @param nombre El nombre descriptivo de la parada (ej. "Terminal Tica Bus").
     * @param coordX La coordenada X en el píxel del panel donde se dibujará la parada.
     * @param coordY La coordenada Y en el píxel del panel donde se dibujará la parada.
     */
    public Parada(int id, String nombre, int coordX, int coordY) {
        this.id = id;
        this.nombre = nombre;
        this.coordX = coordX;
        this.coordY = coordY;
    }

    // --- Getters ---

    /** @return El ID único de la parada. */
    public int getId() { return id; }

    /** @return El nombre de la parada. */
    public String getNombre() { return nombre; }

    /** @return La coordenada X para el dibujo. */
    public int getCoordX() { return coordX; }

    /** @return La coordenada Y para el dibujo. */
    public int getCoordY() { return coordY; }

    /**
     * Proporciona una representación en cadena de la parada, útil para depuración.
     * @return Una cadena con los detalles de la parada.
     */
    @Override
    public String toString() {
        return "Parada{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", coordX=" + coordX +
                ", coordY=" + coordY +
                '}';
    }
}
