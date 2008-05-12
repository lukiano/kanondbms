package servidor.tabla;

/**
 * Interfaz de las fabricas que crean implementaciones de Tabla.
 * Design Pattern Factory.
 */
public interface FabricaTabla {
	
	/**
	 * Metodo para obtener una implementacion de una Tabla.
	 * @param idTabla el numero identificador de la tabla.
	 * @param nombreTabla el nombre de la tabla.
	 * @param columnas arreglo ordenado con los datos de las columnas de la tabla.
	 * @return una tabla que se ajusta a los datos pasados por parametro.
	 */
	Tabla dameTabla(int idTabla, String nombreTabla, Columna[] columnas);

}
