package cr.ed.ulacit;

import java.awt.Color;

/**
 * Representa un autobús dentro de la simulación.
 * <p>
 * Esta clase gestiona el estado y la posición de un único autobús. Contiene la lógica para
 * moverse a lo largo de una {@link Ruta}, detenerse en las paradas y cambiar su estado
 * ({@link EstadoAutobus}) según el progreso de su viaje.
 * </p>
 * <p>
 * El movimiento se calcula de forma lineal entre dos paradas, basado en un valor de "progreso"
 * que va de 0.0 a 1.0.
 * </p>
 */
public class Autobus {

    private final int id;
    private final Color color;
    private double x, y;
    private int paradaActualIndex;
    private int paradaDestinoIndex;
    private double progreso;
    private EstadoAutobus estado;
    private long tiempoDetenido; // Tiempo en milisegundos

    /**
     * Constructor para un nuevo autobús.
     *
     * @param id             El identificador único del autobús.
     * @param color          El color para representar el autobús en la GUI.
     * @param paradaInicial La parada donde el autobús iniciará su recorrido.
     */
    public Autobus(int id, Color color, Parada paradaInicial) {
        this.id = id;
        this.color = color;
        this.x = paradaInicial.getCoordX();
        this.y = paradaInicial.getCoordY();
        this.paradaActualIndex = 0;
        this.paradaDestinoIndex = 1;
        this.progreso = 0.0;
        this.estado = EstadoAutobus.INACTIVO;
        this.tiempoDetenido = 0;
    }

    /**
     * Actualiza la posición del autobús a lo largo de su ruta.
     * <p>
     * Si el autobús está {@code EN_RUTA}, su posición (x, y) se interpola linealmente entre
     * la parada actual y la siguiente. Si el progreso alcanza o supera 1.0, el autobús
     * se considera {@code DETENIDO} en la parada de destino.
     * </p>
     *
     * @param ruta          La ruta que el autobús está siguiendo.
     * @param deltaProgreso El incremento en el progreso (un valor pequeño, ej. 0.01) para este tick de simulación.
     */
    public void mover(Ruta ruta, double deltaProgreso) {
        if (estado == EstadoAutobus.FINALIZADO || estado == EstadoAutobus.DETENIDO) {
            return;
        }

        this.progreso += deltaProgreso;

        if (this.progreso >= 1.0) {
            this.progreso = 1.0; // Asegurar que el progreso no exceda 1.0
            this.estado = EstadoAutobus.DETENIDO;
            this.tiempoDetenido = System.currentTimeMillis();

            Parada paradaAlcanzada = ruta.getParadaPorIndice(paradaDestinoIndex);
            this.x = paradaAlcanzada.getCoordX();
            this.y = paradaAlcanzada.getCoordY();
        }

        if (estado == EstadoAutobus.EN_RUTA) {
            Parada origen = ruta.getParadaPorIndice(paradaActualIndex);
            Parada destino = ruta.getParadaPorIndice(paradaDestinoIndex);

            this.x = origen.getCoordX() + (destino.getCoordX() - origen.getCoordX()) * progreso;
            this.y = origen.getCoordY() + (destino.getCoordY() - origen.getCoordY()) * progreso;
        }
    }

    /**
     * Cambia el estado del autobús de {@code DETENIDO} a {@code EN_RUTA} y lo dirige a la siguiente parada.
     * <p>
     * Si no hay más paradas en la ruta, el estado del autobús cambia a {@code FINALIZADO}.
     * </p>
     *
     * @param ruta La ruta que el autobús está siguiendo.
     */
    public void reanudarRuta(Ruta ruta) {
        this.estado = EstadoAutobus.EN_RUTA;
        this.progreso = 0.0;
        this.paradaActualIndex = this.paradaDestinoIndex;
        this.paradaDestinoIndex++;
        if (this.paradaDestinoIndex >= ruta.getLongitudRuta()) {
            this.estado = EstadoAutobus.FINALIZADO;
        }
    }

    /**
     * Pone el autobús en estado {@code EN_RUTA} al inicio de la simulación.
     */
    public void iniciarRuta() {
        this.estado = EstadoAutobus.EN_RUTA;
    }

    // --- Getters ---

    public int getId() { return id; }
    public Color getColor() { return color; }
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public EstadoAutobus getEstado() { return estado; }
    public long getTiempoDetenido() { return tiempoDetenido; }
    public int getParadaDestinoIndex() { return paradaDestinoIndex; }
}
