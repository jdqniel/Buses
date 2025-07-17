package cr.ed.ulacit;

import java.awt.Color;

/**
 * Representa un autobús en la simulación.
 * <p>
 * Diseño: Esta clase encapsula toda la lógica y el estado de un único autobús.
 * - Estado: Utiliza un {@code enum} ({@link EstadoAutobus}) para gestionar su ciclo de vida (INACTIVO, EN_RUTA, FINALIZADO),
 *   lo que hace el código más legible y robusto que usar booleanos o enteros.
 * - Movimiento: El método {@link #mover(Ruta, double, String)} calcula la posición (x, y) del autobús
 *   interpolando linealmente entre su parada actual y la siguiente. El {@code progreso} (de 0.0 a 1.0)
 *   determina su ubicación exacta en el tramo.
 * - Comunicación: Emplea un patrón de Listener ({@link AutobusListener}) para notificar a la {@link ClienteGUI}
 *   sobre eventos importantes (llegada a parada, fin de ruta) sin acoplarse directamente a ella.
 *   Esto permite que la lógica de la simulación y la de la interfaz de usuario estén separadas.
 */
public class Autobus {

    /** Define los posibles estados de un autobús durante su ciclo de vida en la simulación. */
    public enum EstadoAutobus { INACTIVO, EN_RUTA, FINALIZADO }

    /**
     * Interfaz para notificar a los observadores sobre eventos clave del autobús.
     * Este es un ejemplo del patrón de diseño Observer, que permite a la GUI reaccionar
     * a los eventos de la simulación sin que el autobús conozca los detalles de la GUI.
     */
    public interface AutobusListener {
        /**
         * Se invoca cuando el autobús llega a una parada.
         * @param autobus La instancia del autobús que llegó.
         * @param parada La parada a la que ha llegado.
         * @param hora La hora de la simulación en la que ocurrió el evento.
         */
        void onLlegadaAParada(Autobus autobus, Parada parada, String hora);

        /**
         * Se invoca cuando el autobús completa su ruta.
         * @param autobus La instancia del autobús que finalizó.
         * @param hora La hora de la simulación en la que ocurrió el evento.
         */
        void onRutaFinalizada(Autobus autobus, String hora);
    }

    private final int id;
    private final Color color;
    private double x, y; // Coordenadas actuales para el dibujo
    private int paradaActualIndex;
    private int paradaDestinoIndex;
    private double progreso; // Progreso (0.0 a 1.0) hacia la siguiente parada
    private EstadoAutobus estado;
    private final AutobusListener listener;

    /**
     * Constructor para un nuevo autobús.
     *
     * @param id El ID único del autobús.
     * @param color El color para su representación gráfica.
     * @param paradaInicial La parada donde el autobús comienza.
     * @param listener El objeto (normalmente la GUI) que escuchará los eventos de este autobús.
     */
    public Autobus(int id, Color color, Parada paradaInicial, AutobusListener listener) {
        this.id = id;
        this.color = color;
        this.x = paradaInicial.getCoordX();
        this.y = paradaInicial.getCoordY();
        this.paradaActualIndex = 0;
        this.paradaDestinoIndex = 1;
        this.progreso = 0.0;
        this.estado = EstadoAutobus.INACTIVO;
        this.listener = listener;
    }

    /**
     * Actualiza la posición del autobús a lo largo de su ruta.
     * Si el autobús llega a una parada, notifica al listener.
     *
     * @param ruta La ruta que sigue el autobús.
     * @param deltaProgreso El incremento en el progreso hacia la siguiente parada.
     * @param horaActual La hora actual de la simulación para registrar eventos.
     */
    public void mover(Ruta ruta, double deltaProgreso, String horaActual) {
        if (estado != EstadoAutobus.EN_RUTA || paradaDestinoIndex >= ruta.getLongitudRuta()) {
            return; // No se mueve si no está en ruta o ya ha terminado
        }

        this.progreso += deltaProgreso;

        // Si el progreso es 1.0 o más, ha llegado a la siguiente parada
        if (this.progreso >= 1.0) {
            this.progreso = 0.0; // Reinicia el progreso
            this.paradaActualIndex = this.paradaDestinoIndex;
            this.paradaDestinoIndex++;

            Parada paradaAlcanzada = ruta.getParadaPorIndice(paradaActualIndex);
            this.x = paradaAlcanzada.getCoordX();
            this.y = paradaAlcanzada.getCoordY();

            // Notificar al listener sobre la llegada
            if (listener != null) {
                listener.onLlegadaAParada(this, paradaAlcanzada, horaActual);
            }

            // Comprobar si ha finalizado la ruta
            if (paradaDestinoIndex >= ruta.getLongitudRuta()) {
                this.estado = EstadoAutobus.FINALIZADO;
                if (listener != null) {
                    listener.onRutaFinalizada(this, horaActual);
                }
                return;
            }
        }

        // Interpolación lineal para calcular la posición (x, y) entre paradas
        Parada origen = ruta.getParadaPorIndice(paradaActualIndex);
        Parada destino = ruta.getParadaPorIndice(paradaDestinoIndex);

        this.x = origen.getCoordX() + (destino.getCoordX() - origen.getCoordX()) * progreso;
        this.y = origen.getCoordY() + (destino.getCoordY() - origen.getCoordY()) * progreso;
    }

    /** Cambia el estado del autobús a EN_RUTA para que comience a moverse. */
    public void iniciarRuta() {
        this.estado = EstadoAutobus.EN_RUTA;
    }

    // --- Getters ---

    public int getId() { return id; }
    public Color getColor() { return color; }
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public EstadoAutobus getEstado() { return estado; }
}
