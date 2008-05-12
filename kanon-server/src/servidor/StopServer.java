/**
 * 
 */
package servidor;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import servidor.conexion.ServidorTcp;

/**
 * Clase que contiene el metodo que baja limpiamente el servidor.
 */
public class StopServer {

	/**
	 * Metodo llamado para bajar el servidor.
	 * El servidor escucha por un determinado puerto a un comando determinado para bajarse.
	 * Este programa se conecta con ese puerto y le envia el comando.
	 * @param args los argumentos del programa, que son ignorados.
	 */
	public static void main(String[] args) {
		try {
			String host = InetAddress.getLocalHost().getHostName();
			int port = ServidorTcp.STOP_PORT;
			if (args.length >= 2) {
				host = args[0];
				port = Integer.valueOf(args[1]);
			} else if (args.length == 1) {
				host = args[0];
			}
    		Socket socket = new Socket(host, port);
    		PrintStream printStream = new PrintStream(socket.getOutputStream());
    		try {
    			printStream.println("stop");
    		} finally {
    			printStream.close();
    		}
    		System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
