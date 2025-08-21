package cr.ed.ulacit.dto;

import cr.ed.ulacit.servidor.EventoLog;

import java.io.Serializable;
import java.util.List;

/**
 * Un objeto de transferencia de datos (DTO) que encapsula una actualización completa del estado de la simulación.
 * <p>
 * Esta clase se utiliza para enviar un "paquete" de datos desde el servidor a los clientes en cada tick
 * de la simulación. Agrupar los datos (estado de los autobuses y nuevos eventos) en un solo objeto
 * simplifica la comunicación de la red, ya que solo se necesita una operación de escritura de objeto por actualización.
 * </p>
 */
public class UpdatePayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<AutobusDTO> autobuses;
    private final List<EventoLog> eventos;

    /**
     * Constructor para el payload de actualización.
     *
     * @param autobuses La lista actual del estado de todos los autobuses.
     * @param eventos   La lista de nuevos eventos ocurridos desde la última actualización.
     */
    public UpdatePayload(List<AutobusDTO> autobuses, List<EventoLog> eventos) {
        this.autobuses = autobuses;
        this.eventos = eventos;
    }

    // --- Getters ---

    public List<AutobusDTO> getAutobuses() {
        return autobuses;
    }

    public List<EventoLog> getEventos() {
        return eventos;
    }
}
