package servidor;

/**
 * Interfaz base de los identificadores de los distintos objetos del motor.
 */
public interface Id {
	
	/**
	 * Los ID deben devolver una cadena legible que represente el objeto que identifican.
	 * @return una cadena legible que represente el objeto identificado.
	 */
	String toString();
	
}
