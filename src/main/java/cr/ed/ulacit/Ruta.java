package cr.ed.ulacit;

import java.util.List;

public class Ruta {
    private String nombreRuta;
    private List<Parada> paradas;

    public Ruta(String nombreRuta, List<Parada> paradas) {
        this.nombreRuta = nombreRuta;
        this.paradas = paradas;
    }

    public String getNombreRuta() {
        return nombreRuta;
    }

    public List<Parada> getParadas() {
        return paradas;
    }

    public Parada getParadaPorIndice(int index) {
        if (index >= 0 && index < paradas.size()) {
            return paradas.get(index);
        }
        return null; // O lanzar una excepción
    }

    public int getLongitudRuta() {
        return paradas.size();
    }
}