package cr.ed.ulacit.servidor;

import cr.ed.ulacit.dto.RutaDTO;
import cr.ed.ulacit.dto.UpdatePayload;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Gestiona la comunicación con un único cliente conectado al servidor.
 * <p>
 * Cada instancia de esta clase se ejecuta en su propio hilo, dedicándose a un solo cliente.
 * Su principal responsabilidad es enviar la información inicial de la ruta y luego transmitir
 * las actualizaciones periódicas del estado de la simulación ({@link UpdatePayload}).
 * </p>
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Servidor servidor;
    private ObjectOutputStream objectOutputStream;

    /**
     * Constructor para el manejador de cliente.
     *
     * @param socket   El socket del cliente que se ha conectado.
     * @param servidor La instancia del servidor principal, usada para obtener datos y gestionar la desconexión.
     */
    public ClientHandler(Socket socket, Servidor servidor) {
        this.socket = socket;
        this.servidor = servidor;
        try {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error al crear el stream de salida para el cliente: " + e.getMessage());
        }
    }

    /**
     * El método principal del hilo. Envía la información de la ruta al cliente una vez
     * y luego entra en un bucle para mantener la conexión viva, permitiendo que el servidor
     * envíe actualizaciones.
     */
    @Override
    public void run() {
        try {
            // Enviar la información de la ruta una sola vez al conectar
            RutaDTO ruta = servidor.getRutaDTO();
            objectOutputStream.writeObject(ruta);
            objectOutputStream.flush();

            // El servidor se encargará de empujar las actualizaciones a través de enviarActualizacion().
            // Este bucle mantiene el hilo y la conexión vivos para detectar una desconexión.
            while (!socket.isClosed()) {
                Thread.sleep(5000); // Pausa para no consumir CPU innecesariamente.
            }

        } catch (IOException e) {
            // Esto suele ocurrir si el cliente se desconecta. No es un error crítico del servidor.
            System.out.println("Cliente desconectado (IO): " + socket.getInetAddress());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Hilo del cliente interrumpido: " + socket.getInetAddress());
        } finally {
            servidor.removerCliente(this);
            try {
                socket.close();
            } catch (IOException e) {
                // Ignorar errores al cerrar el socket, ya que probablemente ya esté cerrado.
            }
        }
    }

    /**
     * Envía un paquete de actualización (payload) al cliente.
     * <p>
     * Este método es llamado por el hilo principal del servidor. Si el envío falla
     * (por ejemplo, porque el cliente cerró la aplicación), se encarga de limpiar
     * la conexión y notificar al servidor para que elimine al cliente de la lista de activos.
     * </p>
     *
     * @param payload El objeto {@link UpdatePayload} que contiene el estado más reciente de la simulación.
     */
    public void enviarActualizacion(UpdatePayload payload) {
        try {
            if (objectOutputStream != null) {
                objectOutputStream.writeObject(payload);
                objectOutputStream.flush();
                objectOutputStream.reset(); // Esencial para prevenir que la caché de ObjectOutputStream reutilice objetos antiguos.
            }
        } catch (IOException e) {
            System.err.println("Error al enviar actualización al cliente " + socket.getInetAddress() + ". Eliminando cliente.");
            servidor.removerCliente(this);
            try {
                socket.close();
            } catch (IOException ioException) {
                // Ignorar.
            }
        }
    }

    /**
     * @return El socket asociado a este cliente.
     */
    public Socket getSocket() {
        return this.socket;
    }
}
