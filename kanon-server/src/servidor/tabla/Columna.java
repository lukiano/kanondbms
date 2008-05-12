package servidor.tabla;


/**
 * Interfaz que representa a una columna de una tabla.
 */
public interface Columna {
	
	/**
	 * @return el nombre de la columna.
	 */
	String nombre();
	
	/**
	 * @return el campo de la columna.
	 * @see Campo
	 */
	Campo campo();

}
