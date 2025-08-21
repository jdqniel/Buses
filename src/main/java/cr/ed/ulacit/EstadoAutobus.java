package cr.ed.ulacit;

/**
 * Define los posibles estados de un {@link Autobus} durante la simulación.
 */
public enum EstadoAutobus {
    /**
     * El autobús aún no ha iniciado su ruta. Está en la terminal, esperando para salir.
     */
    INACTIVO,
    /**
     * El autobús se está moviendo entre dos paradas.
     */
    EN_RUTA,
    /**
     * El autobús ha llegado a una parada y está detenido temporalmente.
     */
    DETENIDO,
    /**
     * El autobús ha completado todas las paradas de su ruta.
     */
    FINALIZADO
}
