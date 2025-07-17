package cr.ed.ulacit;

import java.util.List;

/**
 * Representa una ruta de autobús, compuesta por una secuencia ordenada de paradas.
 * <p>
 * Diseño: Esta clase actúa como un contenedor para una lista de objetos {@link Parada}.
 * Su principal responsabilidad es definir el trayecto que los autobuses deben seguir.
 * Al igual que {@code Parada}, se ha diseñado de forma inmutable (la lista de paradas se asigna
 * en el constructor y no puede ser modificada posteriormente) para mantener la consistencia
 * de la simulación.
 */
public class Ruta {
    private final String nombreRuta;
    private final List<Parada> paradas;

    /**
     * Constructor para crear una nueva ruta.
     *
     * @param nombreRuta El nombre de la ruta (ej. "San José - Paso Canoas").
     * @param paradas    La lista ordenada de objetos {@link Parada} que componen la ruta.
     */
    public Ruta(String nombreRuta, List<Parada> paradas) {
        this.nombreRuta = nombreRuta;
        this.paradas = paradas;
    }

    // --- Getters ---

    /** @return El nombre de la ruta. */
    public String getNombreRuta() {
        return nombreRuta;
    }

    /** @return La lista de paradas de la ruta. */
    public List<Parada> getParadas() {
        return paradas;
    }

    /**
     * Obtiene una parada específica de la ruta por su índice.
     *
     * @param index El índice de la parada en la lista (0-based).
     * @return El objeto {@link Parada} en el índice especificado, o {@code null} si el índice es inválido.
     */
    public Parada getParadaPorIndice(int index) {
        if (index >= 0 && index < paradas.size()) {
            return paradas.get(index);
        }
        return null;
    }

    /**
     * Devuelve el número total de paradas en la ruta.
     *
     * @return El tamaño de la lista de paradas.
     */
    public int getLongitudRuta() {
        return paradas.size();
    }
}
