/**
 * 
 */
package servidor.conexion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Thread que escucha por un puerto determinado el pedido de apagado del motor.
 */
class StopperThread implements Runnable {
	
	/**
	 * el puerto por donde se escucha el pedido de baja del servidor.
	 */
	private int port;
	
	/**
	 * la instancia del servidor de conexiones asociada a este thread.
	 */
	private ServidorTcp servidorTcp;
	
	private BufferedReader bufferedReader;
	
	/**
	 * Socket por donde se escucha el pedido de baja el servidor.
	 */
	private ServerSocket stopServerSocket;
	
	/**
	 * Conexion que se crea cuando se realiza una conexion al puerto.
	 */
	private Socket socket;
	
	/**
	 * Constructor de la clase.
	 * @param port el puerto por donde se escucha el pedido de baja del servidor.
	 * @param servidorTcp la instancia del servidor de conexiones asociada a este thread.
	 */
	public StopperThread(int port, ServidorTcp servidorTcp) {
		this.port = port;
		this.servidorTcp = servidorTcp;
	}

	/**
	 * Metodo principal del thread el cual se queda escuchando el pedido de baja del servidor.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			this.stopServerSocket = new ServerSocket(this.port);
    		this.socket = stopServerSocket.accept();
    		// se conecto alguien al puerto
    		this.bufferedReader = new BufferedReader(
    				new InputStreamReader(socket.getInputStream()));
    		try {
    			boolean apagado = false;
    			while (!apagado) {
        			String comando = bufferedReader.readLine();
        			if ("stop".equals(comando)) {
        				apagado = true;
            			this.servidorTcp.apagar(false);
        			}
    			}
    		} finally {
    			bufferedReader.close();
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo que cierra las conexiones activas.
	 * @throws IOException si ocurre un error de I/O al cerrar las conexiones.
	 */
	public void close() throws IOException {
		if (this.bufferedReader != null) {
			this.bufferedReader.close();
		}
		if (this.socket != null) {
			this.socket.close();
		}
		if (this.stopServerSocket != null) {
			this.stopServerSocket.close();
		}
	}
	
}
