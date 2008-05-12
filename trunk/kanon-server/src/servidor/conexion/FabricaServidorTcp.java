package servidor.conexion;

/**
 * Fabrica que devuelve una instancia unica del servidor de conexiones en el motor.
 * 
 */
public class FabricaServidorTcp {
	
	/**
	 * Variable donde se guarda la instancia unica.
	 */
	private static ServidorTcp instancia;

	/**
	 * Constructor privado para evitar instanciamiento.
	 */
	private FabricaServidorTcp() {
		super();
	}

	/**
	 * Devuelve la instancia unica del Servidor de Conexiones.
	 * @return la instancia unica del Servidor de Conexiones.
	 */
	public static synchronized ServidorTcp dameInstancia() {
		if (instancia == null) {
			instancia = new ServidorTcp();
		}
		return instancia;
	}

}
