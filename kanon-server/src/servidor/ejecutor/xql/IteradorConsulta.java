package servidor.ejecutor.xql;

import Zql.ZExp;
import Zql.ZTuple;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;

/**
 * Interfaz para procesar una tabla, iterando sobre los registros de la misma y ejecutando un comando en aquellos que cumplen la condicion.
 * Segun la condicion puede que no sea necesario iterar por toda la tabla.
 */
public interface IteradorConsulta {
	
	/**
	 * @param tabla la tabla a procesar.
	 * @param expresionWhere una expresion ZQL que representa a las condiciones del Where de la sentencia.
	 * @param comando el comando que va a ser ejecutado para cada coincidencia.
	 */
	void ejecutarParaCadaCoincidencia(Tabla tabla, ZExp expresionWhere, Comando comando);

	/**
	 * Interfaz que representa a un comando que se va a ejecutar para cada registro que devuelva la iteracion y que cumpla con las condiciones del Where.
	 * Sigue el Design Pattern Command.
	 */
	public interface Comando {
		
		/**
		 * Metodo que ejecuta la accion en un registro.
		 * @param tabla la tabla que esta siendo analizada.
		 * @param registro un registro dentro de la tabla.
		 * @param tuple la tupla con los valores del registro modificados segun la expresion de la sentencia (ej, proyeccion para una consulta o valores nuevo para un Update). 
		 */
		void ejecutarAccion(Tabla tabla, Registro registro, ZTuple tuple);
		
	}
}
