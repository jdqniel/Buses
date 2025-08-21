package cr.ed.ulacit;

import cr.ed.ulacit.dto.RutaDTO;
import cr.ed.ulacit.dto.UpdatePayload;
import cr.ed.ulacit.servidor.EventoLog;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Collections;

/**
 * La interfaz gráfica de usuario (GUI) para el cliente de la simulación.
 * <p>
 * Esta clase se encarga de:
 * <ul>
 *     <li>Crear la ventana principal de la aplicación.</li>
 *     <li>Inicializar el panel del mapa ({@link MapaPanel}) y el área de texto para los logs.</li>
 *     <li>Conectarse al servidor TCP.</li>
 *     <li>Recibir actualizaciones del servidor en un hilo separado para no bloquear la GUI.</li>
 *     <li>Actualizar el mapa y el log de eventos con los datos recibidos.</li>
 * </ul>
 * </p>
 */
public class ClienteGUI extends JFrame {

    private static final String HOST = "127.0.0.1";
    private static final int PUERTO = 12345;

    private final MapaPanel mapaPanel;
    private final JTextArea logArea;

    /**
     * Constructor de la GUI del cliente. Configura la ventana y los componentes Swing.
     */
    public ClienteGUI() {
        setTitle("Simulador de Autobuses - Cliente TCP");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        mapaPanel = new MapaPanel(Collections.emptyList(), null);
        add(mapaPanel, BorderLayout.CENTER);

        logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.SOUTH);
    }

    /**
     * Inicia la conexión con el servidor en un hilo separado para no bloquear la GUI.
     */
    public void conectarAlServidor() {
        Thread connectionThread = new Thread(() -> {
            try {
                Socket socket = new Socket(HOST, PUERTO);
                registrarEventoConTimestamp("Conectado al servidor en " + HOST + ":" + PUERTO);

                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                // El primer objeto enviado por el servidor es la información de la ruta
                final RutaDTO ruta = (RutaDTO) objectInputStream.readObject();
                SwingUtilities.invokeLater(() -> mapaPanel.setRuta(ruta));

                // Inicia el bucle para escuchar actualizaciones continuas del servidor
                escucharActualizaciones(objectInputStream);

            } catch (IOException | ClassNotFoundException e) {
                registrarEventoConTimestamp("Error al conectar o comunicarse con el servidor: " + e.getMessage());
                e.printStackTrace();
            }
        });
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    /**
     * Bucle principal que se ejecuta en un hilo de fondo para recibir objetos del servidor.
     *
     * @param objectInputStream El stream del que se leen los datos del servidor.
     */
    private void escucharActualizaciones(ObjectInputStream objectInputStream) {
        try {
            while (true) {
                // Lee el payload que contiene tanto los autobuses como los eventos
                final UpdatePayload payload = (UpdatePayload) objectInputStream.readObject();

                // Actualiza la UI en el Event Dispatch Thread (EDT) para garantizar la seguridad del hilo en Swing
                SwingUtilities.invokeLater(() -> {
                    mapaPanel.setAutobuses(payload.getAutobuses());
                    for (EventoLog evento : payload.getEventos()) {
                        registrarEvento(evento.toString());
                    }
                });
            }
        } catch (IOException | ClassNotFoundException e) {
            registrarEventoConTimestamp("Se ha perdido la conexión con el servidor: " + e.getMessage());
        }
    }

    /**
     * Añade un mensaje al área de log de la GUI.
     * Este método es seguro para ser llamado desde cualquier hilo.
     *
     * @param mensaje El mensaje a registrar.
     */
    private void registrarEvento(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(mensaje + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Un método de conveniencia para registrar eventos del sistema (como conexiones/desconexiones)
     * que no vienen con un timestamp del servidor.
     */
    private void registrarEventoConTimestamp(String mensaje) {
        // Formato simple para consistencia
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss").format(java.time.LocalTime.now());
        registrarEvento(String.format("[%s] %s", timestamp, mensaje));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteGUI gui = new ClienteGUI();
            gui.setVisible(true);
            gui.conectarAlServidor();
        });
    }
}
