package cr.ed.ulacit.dto;

import cr.ed.ulacit.EstadoAutobus;

import java.awt.Color;
import java.io.Serializable;

public class AutobusDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final Color color;
    private final int x;
    private final int y;
    private final EstadoAutobus estado;

    public AutobusDTO(int id, Color color, int x, int y, EstadoAutobus estado) {
        this.id = id;
        this.color = color;
        this.x = x;
        this.y = y;
        this.estado = estado;
    }

    public int getId() { return id; }
    public Color getColor() { return color; }
    public int getX() { return x; }
    public int getY() { return y; }
    public EstadoAutobus getEstado() { return estado; }
}
