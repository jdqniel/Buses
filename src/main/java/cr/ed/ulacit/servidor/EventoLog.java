package cr.ed.ulacit.servidor;

import java.io.Serializable;

/**
 * Representa un único evento de log en la simulación.
 * <p>
 * Esta clase es un DTO (Data Transfer Object) simple y serializable, diseñado para encapsular
 * un mensaje de log y la hora en que ocurrió. Se utiliza para enviar notificaciones de eventos
 * desde el servidor a los clientes de una manera estructurada.
 * </p>
 */
public class EventoLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String hora;
    private final String mensaje;

    /**
     * Constructor para un nuevo evento de log.
     *
     * @param hora    La hora de la simulación en que ocurrió el evento (formato HH:mm:ss).
     * @param mensaje El texto descriptivo del evento.
     */
    public EventoLog(String hora, String mensaje) {
        this.hora = hora;
        this.mensaje = mensaje;
    }

    // --- Getters ---

    public String getHora() {
        return hora;
    }

    public String getMensaje() {
        return mensaje;
    }

    /**
     * Devuelve una representación formateada del log, ideal para mostrar en la GUI del cliente.
     * @return Una cadena con la hora y el mensaje, ej. "[05:01:00] Autobús 1 ha iniciado su ruta."
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", hora, mensaje);
    }
}
