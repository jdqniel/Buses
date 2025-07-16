package cr.ed.ulacit;

import java.util.Random; // Para velocidades variables

public class Autobus implements Runnable {
    private final int id;
    private final String color;
    private double velocidadActual;
    private Parada posicionActual;
    private long tiempoLlegadaEstimado;
    private boolean enMovimiento;
    private final Ruta ruta; // Referencia a la ruta que sigue
    private volatile boolean running = true; // Para controlar el ciclo del hilo

    public Autobus(int id, String color, Ruta ruta) {
        this.id = id;
        this.color = color;
        this.ruta = ruta;
        this.posicionActual = ruta.getParadas().get(0); // Inicia en la primera parada
        this.enMovimiento = false;
        this.velocidadActual = 0; // Se inicializará al arrancar
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public Parada getPosicionActual() {
        return posicionActual;
    }

    public double getVelocidadActual() {
        return velocidadActual;
    }

    public long getTiempoLlegadaEstimado() {
        return tiempoLlegadaEstimado;
    }

    public boolean isEnMovimiento() {
        return enMovimiento;
    }

    public void setPosicionActual(Parada posicionActual) {
        this.posicionActual = posicionActual;
    }

    public void setVelocidadActual(double velocidadActual) {
        this.velocidadActual = velocidadActual;
    }

    public void setEnMovimiento(boolean enMovimiento) {
        this.enMovimiento = enMovimiento;
    }

    public void setTiempoLlegadaEstimado(long tiempoLlegadaEstimado) {
        this.tiempoLlegadaEstimado = tiempoLlegadaEstimado;
    }

    public void stopRunning() {
        this.running = false;
    }

    @Override
    public void run() {
        Random random = new Random();
        int paradaActualIndex = 0;

        while (running && paradaActualIndex < ruta.getParadas().size()) {
            Parada siguienteParada = ruta.getParadas().get(paradaActualIndex);
            this.setPosicionActual(siguienteParada);
            System.out.println("Autobús " + id + " ha llegado a: " + siguienteParada.getNombre());

            // Simular tiempo en la parada (pausa aleatoria)
            try {
                long tiempoEnParada = 1000 + random.nextInt(2000); // Entre 1 y 3 segundos
                Thread.sleep(tiempoEnParada);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Autobús " + id + " interrumpido mientras esperaba en parada.");
                running = false;
            }

            paradaActualIndex++;

            if (paradaActualIndex < ruta.getParadas().size()) {
                Parada destinoParada = ruta.getParadas().get(paradaActualIndex);
                System.out.println("Autobús " + id + " saliendo hacia: " + destinoParada.getNombre());

                // Simular el tiempo de viaje a la siguiente parada
                try {
                    double factorVelocidad = 1.0 + (random.nextDouble() * 0.5 - 0.25); // +/- 25% de la velocidad base
                    long tiempoViaje = (long) (5000 * factorVelocidad); // Por ejemplo, 5 segundos base
                    this.setVelocidadActual(tiempoViaje); // Guarda el "tiempo de viaje" como una métrica de velocidad
                    this.setEnMovimiento(true);
                    Thread.sleep(tiempoViaje);
                    this.setEnMovimiento(false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Autobús " + id + " interrumpido durante el viaje.");
                    running = false;
                }
            } else {
                System.out.println("Autobús " + id + " ha completado la ruta.");
            }
        }
    }
}