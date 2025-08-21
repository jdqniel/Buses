package cr.ed.ulacit.dto;

import java.io.Serializable;

public class ParadaDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String nombre;
    private final int coordX;
    private final int coordY;

    public ParadaDTO(int id, String nombre, int coordX, int coordY) {
        this.id = id;
        this.nombre = nombre;
        this.coordX = coordX;
        this.coordY = coordY;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getCoordX() { return coordX; }
    public int getCoordY() { return coordY; }
}
