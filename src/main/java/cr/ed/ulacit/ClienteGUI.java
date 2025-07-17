package cr.ed.ulacit;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Clase principal de la aplicación que configura la ventana (JFrame) y orquesta la simulación.
 * <p>
 * Diseño: Esta clase tiene varias responsabilidades clave:
 * 1.  **Configuración de la UI**: Crea la ventana principal, el {@link MapaPanel} para la visualización
 *     y un {@link JTextArea} para el registro de eventos. Utiliza un {@link BorderLayout} para organizar estos componentes.
 * 2.  **Motor de Simulación**: Un {@link Timer} de Swing actúa como el "corazón" de la simulación. Se dispara
 *     periódicamente (cada 50 ms) para actualizar el estado y repintar la pantalla, creando la animación.
 * 3.  **Gestión de Estado**: Inicializa y mantiene la lista de autobuses y la ruta. Controla la lógica de
 *     salidas escalonadas, activando un autobús cada 15 segundos de tiempo real.
 * 4.  **Manejo de Eventos**: Implementa {@link Autobus.AutobusListener} para recibir notificaciones de los
 *     autobuses (llegadas a paradas, fin de ruta) y las muestra en el área de registro.
 * 5.  **Simulación de Tiempo**: Mantiene un {@link Calendar} para simular el paso del tiempo en la aplicación,
 *     lo que permite registrar los eventos con una marca de tiempo coherente.
 */
public class ClienteGUI extends JFrame implements Autobus.AutobusListener {

    private final MapaPanel mapaPanel;
    private final JTextArea logArea;
    private final List<Autobus> autobuses = new ArrayList<>();
    private final Ruta ruta;
    private final Calendar calendarioSimulacion;
    private final SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
    private int proximoAutobusEnSalir = 0;
    private long ultimoTiempoSalida = -1;

    /**
     * Constructor que inicializa la GUI y la simulación.
     */
    public ClienteGUI() {
        setTitle("Simulador de Autobuses - Versión Autónoma");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Inicializar los datos de la simulación
        this.ruta = inicializarRuta();
        this.calendarioSimulacion = Calendar.getInstance();
        this.calendarioSimulacion.set(Calendar.HOUR_OF_DAY, 5); // La simulación empieza a las 5:00 AM
        this.calendarioSimulacion.set(Calendar.MINUTE, 0);
        this.calendarioSimulacion.set(Calendar.SECOND, 0);

        inicializarAutobuses();

        // 2. Configurar los componentes de la UI
        mapaPanel = new MapaPanel(autobuses, ruta);
        add(mapaPanel, BorderLayout.CENTER);

        logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.SOUTH);

        // 3. Iniciar el motor de la simulación
        Timer timer = new Timer(50, e -> {
            actualizarSimulacion();
            mapaPanel.repaint();
        });
        timer.start();
    }

    /**
     * Crea y define la ruta completa con sus 20 paradas y coordenadas ajustadas a la ventana.
     * @return Un objeto {@link Ruta} con toda la información del trayecto.
     */
    private Ruta inicializarRuta() {
        List<Parada> paradas = new ArrayList<>();
        // Coordenadas re-escaladas para una ventana de 1200x800
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
     * Crea las 10 instancias de {@link Autobus}, asignándoles un color y un listener.
     * Inicialmente, todos los autobuses están en estado INACTIVO.
     */
    private void inicializarAutobuses() {
        Color[] colores = {
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA,
            Color.CYAN, Color.PINK, new Color(128, 0, 128), new Color(139, 69, 19), Color.GRAY
        };
        for (int i = 0; i < 10; i++) {
            autobuses.add(new Autobus(i + 1, colores[i], ruta.getParadaPorIndice(0), this));
        }
    }

    /**
     * Este método se ejecuta en cada "tick" del temporizador y avanza la lógica de la simulación.
     */
    private void actualizarSimulacion() {
        long tiempoActual = System.currentTimeMillis();
        calendarioSimulacion.add(Calendar.MINUTE, 1); // Avanza el reloj de la simulación
        String horaActual = formatHora.format(calendarioSimulacion.getTime());

        // Lógica para la salida escalonada de autobuses cada 15 segundos
        if (proximoAutobusEnSalir < autobuses.size() && (ultimoTiempoSalida == -1 || (tiempoActual - ultimoTiempoSalida) > 15000)) {
            Autobus proximo = autobuses.get(proximoAutobusEnSalir);
            proximo.iniciarRuta();
            ultimoTiempoSalida = tiempoActual;
            registrarEvento(String.format("[%s] ¡Salida! Autobús %d ha iniciado su ruta desde %s.", horaActual, proximo.getId(), ruta.getParadaPorIndice(0).getNombre()));
            proximoAutobusEnSalir++;
        }

        // Mueve cada autobús que esté activo
        double velocidadBase = 0.01;
        for (Autobus bus : autobuses) {
            if (bus.getEstado() == Autobus.EstadoAutobus.EN_RUTA) {
                double velocidadIndividual = velocidadBase * (1.0 + Math.random() * 0.5 - 0.25); // Añade variabilidad
                bus.mover(ruta, velocidadIndividual, horaActual);
            }
        }
    }

    /**
     * Añade un mensaje al panel de registro de eventos de forma segura para hilos.
     * @param mensaje El texto a registrar.
     */
    private void registrarEvento(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(mensaje + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
        });
    }

    // --- Implementación de AutobusListener ---

    @Override
    public void onLlegadaAParada(Autobus autobus, Parada parada, String hora) {
        registrarEvento(String.format("[%s] Autobús %d llegó a: %s", hora, autobus.getId(), parada.getNombre()));
    }

    @Override
    public void onRutaFinalizada(Autobus autobus, String hora) {
        registrarEvento(String.format("[%s] ¡Ruta Completada! Autobús %d ha llegado a su destino final.", hora, autobus.getId()));
    }

    /**
     * El punto de entrada de la aplicación.
     */
    public static void main(String[] args) {
        // Asegura que la GUI se cree y se muestre en el Event Dispatch Thread (EDT) de Swing.
        SwingUtilities.invokeLater(() -> {
            ClienteGUI gui = new ClienteGUI();
            gui.setVisible(true);
        });
    }
}
