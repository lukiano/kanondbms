/**
 * 
 */
package servidor;

import java.io.IOException;

import servidor.conexion.FabricaServidorTcp;
import servidor.conexion.ServidorTcp;

/**
 * Clase que contiene el metodo que levanta el servidor.
 */
public class Main {
	
	/**
	 * Metodo llamado para levantar el servidor.
	 * @param args los argumentos del programa, que son ignorados.
	 * @see ServidorTcp
	 */
	public static void main(String[] args) {
        try {
    		ServidorTcp servidorTcp = FabricaServidorTcp.dameInstancia();
    		servidorTcp.iniciarServidor();
        } catch (IOException e) {
        	throw new RuntimeException(e);
        }
	}
	
}
