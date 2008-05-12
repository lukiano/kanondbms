package servidor.buffer;

import servidor.Id;
import servidor.indice.hash.Bucket;
import servidor.tabla.Pagina;



/**
 * Interfaz que representa un bloque de datos que se puede guardar en el Buffer Manager.
 * @see Pagina
 * @see Bucket
 */
public interface Bloque extends Marcable, Validable {
	
	/**
	 * Constante con el tamanio en bytes de un bloque.
	 * Se ha optado por usar bloques de 64KB de datos.
	 */
	int TAMANIO = 65536; // bloques de 64K
	
	/**
	 * Metodo para obtener el contenido del bloque.
	 * @return el contenido en binario del bloque.
	 */
	byte[] dameDatos();
	
	/**
	 * Interfaz base de las clases que representan a un bloque que se puede guardar en el Buffer Manager.
	 *
	 */
	public interface ID extends Id {}
}
