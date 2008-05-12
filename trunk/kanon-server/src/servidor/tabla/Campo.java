/**
 * 
 */
package servidor.tabla;

import servidor.catalog.tipo.Tipo;

/**
 * Interfaz que representa a un campo en el motor de la base de datos.
 * Un campo esta compuesto por un tipo de datos y una longitud la cual es ignorada si el tipo de datos es de longitud fija.
 */
public interface Campo {

	/**
	 * @return el tipo de datos de este campo.
	 */
	Tipo tipo();
	
	/**
	 * @return la longitud para los tipos de longitud variable.
	 */
	int longitud();
	
}
