package cr.ed.ulacit.servidor;

import cr.ed.ulacit.Autobus;
import cr.ed.ulacit.EstadoAutobus;
import cr.ed.ulacit.Parada;
import cr.ed.ulacit.Ruta;
import cr.ed.ulacit.dto.AutobusDTO;
import cr.ed.ulacit.dto.ParadaDTO;
import cr.ed.ulacit.dto.RutaDTO;
import cr.ed.ulacit.dto.UpdatePayload;

import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * El núcleo de la simulación de autobuses. Este servidor gestiona el estado de la simulación,
 * acepta conexiones de clientes y transmite actualizaciones en tiempo real.
 * <p>
 * Responsabilidades:
 * <ul>
 *     <li>Inicializar la ruta y los autobuses.</li>
 *     <li>Ejecutar un bucle de simulación para actualizar el estado de los autobuses.</li>
 *     <li>Escuchar y aceptar conexiones de clientes TCP.</li>
 *     <li>Enviar el estado completo de la simulación (posiciones de autobuses y eventos) a todos los clientes conectados.</li>
 *     <li>Registrar eventos clave de la simulación.</li>
 * </ul>
 * </p>
 */
public class Servidor {

    private static final int PUERTO = 12345;
    private static final int INTERVALO_SALIDA_BUS = 15000; // 15 segundos
    private static final int TIEMPO_PARADA = 5000; // 5 segundos
    private static final int TICK_SIMULACION = 50; // 50 ms

    private final List<Autobus> autobuses = new ArrayList<>();
    private final Ruta ruta;
    private final List<ClientHandler> clientes = new CopyOnWriteArrayList<>();
    private final List<EventoLog> logEventos = Collections.synchronizedList(new ArrayList<>());
    private final Calendar calendarioSimulacion;
    private final SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
    private int proximoAutobusEnSalir = 0;
    private long ultimoTiempoSalida = -1;

    public static void main(String[] args) {
        new Servidor().iniciar();
    }

    /**
     * Constructor del servidor. Inicializa la ruta, los autobuses y el calendario de simulación.
     */
    public Servidor() {
        this.ruta = inicializarRuta();
        this.calendarioSimulacion = Calendar.getInstance();
        this.calendarioSimulacion.set(Calendar.HOUR_OF_DAY, 5);
        this.calendarioSimulacion.set(Calendar.MINUTE, 0);
        this.calendarioSimulacion.set(Calendar.SECOND, 0);
        inicializarAutobuses();
    }

    /**
     * Inicia los dos hilos principales del servidor: uno para la lógica de simulación
     * y otro para aceptar conexiones de clientes.
     */
    private void iniciar() {
        Thread hiloSimulacion = new Thread(this::iniciarLoopSimulacion);
        hiloSimulacion.setDaemon(true);
        hiloSimulacion.start();

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor de simulación de autobuses iniciado en el puerto " + PUERTO);
            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + socketCliente.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(socketCliente, this);
                clientes.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error fatal en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * El bucle principal de la simulación. Se ejecuta continuamente para actualizar el estado
     * y notificar a los clientes.
     */
    private void iniciarLoopSimulacion() {
        while (true) {
            List<EventoLog> nuevosEventos = actualizarSimulacion();
            notificarAClientes(nuevosEventos);
            try {
                Thread.sleep(TICK_SIMULACION);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("El hilo de simulación fue interrumpido.");
            }
        }
    }

    /**
     * Actualiza el estado de todos los autobuses y genera eventos de log.
     *
     * @return Una lista de los nuevos eventos generados en este tick.
     */
    private List<EventoLog> actualizarSimulacion() {
        long tiempoActual = System.currentTimeMillis();
        calendarioSimulacion.add(Calendar.SECOND, 1); // Avanza el tiempo de la simulación
        String horaActual = formatHora.format(calendarioSimulacion.getTime());
        List<EventoLog> nuevosEventos = new ArrayList<>();

        // Lógica para que los autobuses salgan de la terminal
        if (proximoAutobusEnSalir < autobuses.size() && (ultimoTiempoSalida == -1 || (tiempoActual - ultimoTiempoSalida) > INTERVALO_SALIDA_BUS)) {
            Autobus proximo = autobuses.get(proximoAutobusEnSalir);
            if (proximo.getEstado() == EstadoAutobus.INACTIVO) {
                proximo.iniciarRuta();
                ultimoTiempoSalida = tiempoActual;
                proximoAutobusEnSalir++;
                nuevosEventos.add(new EventoLog(horaActual, "El autobús " + proximo.getId() + " ha iniciado su ruta."));
            }
        }

        double velocidadBase = 0.01;
        for (Autobus bus : autobuses) {
            EstadoAutobus estadoAnterior = bus.getEstado();

            if (bus.getEstado() == EstadoAutobus.EN_RUTA) {
                double velocidadIndividual = velocidadBase * (1.0 + Math.random() * 0.5 - 0.25);
                bus.mover(ruta, velocidadIndividual);
            } else if (bus.getEstado() == EstadoAutobus.DETENIDO) {
                if (System.currentTimeMillis() - bus.getTiempoDetenido() > TIEMPO_PARADA) {
                    bus.reanudarRuta(ruta);
                }
            }

            // Comprobar si hubo un cambio de estado para generar un evento
            if (estadoAnterior != bus.getEstado()) {
                switch (bus.getEstado()) {
                    case DETENIDO:
                        Parada parada = ruta.getParadaPorIndice(bus.getParadaDestinoIndex() - 1); // La parada en la que se detuvo
                        nuevosEventos.add(new EventoLog(horaActual, "El autobús " + bus.getId() + " ha llegado a la parada " + parada.getNombre() + "."));
                        break;
                    case EN_RUTA:
                        if (estadoAnterior == EstadoAutobus.DETENIDO) {
                            nuevosEventos.add(new EventoLog(horaActual, "El autobús " + bus.getId() + " ha salido de la parada."));
                        }
                        break;
                    case FINALIZADO:
                        nuevosEventos.add(new EventoLog(horaActual, "El autobús " + bus.getId() + " ha finalizado su ruta."));
                        break;
                }
            }
        }
        logEventos.addAll(nuevosEventos);
        return nuevosEventos;
    }

    /**
     * Envía el estado actualizado de la simulación a todos los clientes conectados.
     *
     * @param nuevosEventos Los eventos que ocurrieron en el último tick de simulación.
     */
    private void notificarAClientes(List<EventoLog> nuevosEventos) {
        if (clientes.isEmpty()) return;

        List<AutobusDTO> estadoActualAutobuses = autobuses.stream()
                .map(bus -> new AutobusDTO(bus.getId(), bus.getColor(), bus.getX(), bus.getY(), bus.getEstado()))
                .collect(Collectors.toList());

        UpdatePayload payload = new UpdatePayload(estadoActualAutobuses, nuevosEventos);

        for (ClientHandler cliente : clientes) {
            cliente.enviarActualizacion(payload);
        }
    }

    /**
     * Crea un DTO de la ruta para ser enviado a los nuevos clientes.
     * @return Un objeto {@link RutaDTO} con la información de la ruta.
     */
    public RutaDTO getRutaDTO() {
        List<ParadaDTO> paradasDTO = ruta.getParadas().stream()
                .map(p -> new ParadaDTO(p.getId(), p.getNombre(), p.getCoordX(), p.getCoordY()))
                .collect(Collectors.toList());
        return new RutaDTO(ruta.getNombreRuta(), paradasDTO);
    }

    /**
     * Elimina un cliente de la lista de clientes activos (ej. cuando se desconecta).
     * @param clientHandler El handler del cliente a eliminar.
     */
    public void removerCliente(ClientHandler clientHandler) {
        clientes.remove(clientHandler);
        System.out.println("Cliente desconectado: " + clientHandler.getSocket().getInetAddress());
    }

    /**
     * Configura la ruta inicial con todas sus paradas.
     * @return Un objeto {@link Ruta} completamente inicializado.
     */
    private Ruta inicializarRuta() {
        List<Parada> paradas = new ArrayList<>();
        paradas.add(new Parada(1, "Terminal Tica Bus San José", 1100, 150));
        paradas.add(new Parada(2, "Barrio Los Ángeles", 1050, 180));
        paradas.add(new Parada(3, "Autopista José María Castro Madriz", 1000, 210));
        paradas.add(new Parada(4, "Escobal", 950, 240));
        paradas.add(new Parada(5, "Soda el Higueron", 900, 270));
        paradas.add(new Parada(6, "Carretera Pacífica Fernández Oreamuno #2", 850, 300));
        paradas.add(new Parada(7, "Pochotal", 800, 330));
        paradas.add(new Parada(8, "Carretera Pacífica Fernández Oreamuno", 750, 360));
        paradas.add(new Parada(9, "Pocares", 700, 390));
        paradas.add(new Parada(10, "Llamarón", 650, 420));
        paradas.add(new Parada(11, "Portalón", 600, 450));
        paradas.add(new Parada(12, "Guapil", 550, 480));
        paradas.add(new Parada(13, "Tica Bus Uvita", 500, 510));
        paradas.add(new Parada(14, "Ojo de Agua", 450, 540));
        paradas.add(new Parada(15, "Olla Cero", 400, 570));
        paradas.add(new Parada(16, "Parada Río Esquinas", 350, 600));
        paradas.add(new Parada(17, "Kilometro 30", 300, 630));
        paradas.add(new Parada(18, "Sucursal Dos Pinos, Río Claro", 250, 660));
        paradas.add(new Parada(19, "Terminal municipal ciudad Nelly", 200, 690));
        paradas.add(new Parada(20, "Terminal de transporte", 150, 720));
        return new Ruta("Ruta San José - Paso Canoas", paradas);
    }

    /**
     * Crea las instancias de los autobuses al inicio de la simulación.
     */
    private void inicializarAutobuses() {
        Color[] colores = {
                Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA,
                Color.CYAN, Color.PINK, new Color(128, 0, 128), new Color(139, 69, 19), Color.GRAY
        };
        for (int i = 0; i < 10; i++) {
            autobuses.add(new Autobus(i + 1, colores[i % colores.length], ruta.getParadaPorIndice(0)));
        }
    }
}
