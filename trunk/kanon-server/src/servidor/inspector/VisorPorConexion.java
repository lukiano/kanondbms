/**
 * 
 */
package servidor.inspector;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 */
public class VisorPorConexion implements Visor {
	
	private ServerSocket visor_socket;
	
	private Socket cliente_socket;
	
	private ObjectOutputStream objectOutputStream;
	
	private String nombre;

	/**
	 * 
	 */
	public VisorPorConexion(int port, String nombre) {
		try {
			this.visor_socket = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.nombre = nombre;
	}

	/**
	 * @see servidor.inspector.Visor#mostrarMensaje(java.lang.String[])
	 */
	public synchronized void mostrarMensaje(String[] mensaje) {
		boolean mensajeMostrado = false;
		while (!mensajeMostrado) {
			try {
				if (this.cliente_socket == null) {
					this.cliente_socket = this.visor_socket.accept();
					this.objectOutputStream = new ObjectOutputStream(this.cliente_socket.getOutputStream());
				}
				this.objectOutputStream.writeObject(mensaje);
				mensajeMostrado = true;
				this.objectOutputStream.flush();
			} catch (IOException e) {
				if (this.cliente_socket != null) {
					try {
						this.cliente_socket.close();
					} catch (IOException ignored) {}
				}
				this.cliente_socket = null;
			}
		}
	}

	/**
	 * @see servidor.inspector.Visor#nombre()
	 */
	public String nombre() {
		return this.nombre;
	}

	public void cerrar() {
		try {
			if (this.objectOutputStream != null) {
				this.objectOutputStream.close();
			}
			if (this.cliente_socket != null) {
				this.cliente_socket.close();
			}
			this.visor_socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
