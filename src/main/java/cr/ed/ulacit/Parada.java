package cr.ed.ulacit;

public class Parada {
    private int id;
    private String nombre;
    // private Coordenada ubicacion;

    public Parada(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return "Parada{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}