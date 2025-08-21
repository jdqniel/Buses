package cr.ed.ulacit.dto;

import java.io.Serializable;
import java.util.List;

public class RutaDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nombreRuta;
    private final List<ParadaDTO> paradas;

    public RutaDTO(String nombreRuta, List<ParadaDTO> paradas) {
        this.nombreRuta = nombreRuta;
        this.paradas = paradas;
    }

    public String getNombreRuta() { return nombreRuta; }
    public List<ParadaDTO> getParadas() { return paradas; }
}
