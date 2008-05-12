package servidor.indice.hash;

import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.util.Iterador;

/*
 * Creo que para el alcance de nuestro TP un Hash va a ser mucho mas simple que el arbol.
 * 
 * Seguimos con la idea de hacer un indice para cada columna de cada tabla.
 * Al usar la query (ya sea select, update o delete) bloqueamos la entrada en el hash.
 * Se usa el indice correspondiente al primer elemento del WHERE que sea una igualdad.
 * 
 * Un update puede requerir bloquear 2 entradas (el valor viejo para borrarlo y el nuevo para agregarlo).
 * En este caso habria que bloquear ambas entradas a la vez para evitar deadlock.
 * Usar mecanismo de bloqueo condicional.
 * 
 * Para cada entrada usamos los buckets con overflow. Mantenemos simple y no hacemos hash dinamico.
 * 
 * Si una query no usa ningun indice, 2 casos:
 * a) se bloquean los registros a medida que se van leyendo, como se hace ahora.
 * b) bloquear de una toda la tabla. Es mas rapido, menos concurrente y permite aislamiento SERIALIZABLE. 
 * 
 * Hay que bloquear las entradas de hash Y los registros de la tabla.
 *  
 * Contras de Hash vs Arbol:
 *  - no se puede utilizar para queries de rangos.
 *  
 * Pros: 
 *  - Sirve para queries con igualdad.
 *  - Al bloquear las entradas del hash, podemos implementar simple el aislamiento SERIALIZABLE.
 *  - Algoritmo de hash simple.
 *  - Etc.  
 *
 * Formato:
 *  La tabla de hash va a ocupar 1 pagina.
 *  Las entradas de la tabla son de la forma <hash, ID de primer bucket de ese hash>
 *  
 *  IDEA: Si el ID del bucket es igual al hash, entonces la tabla de hash no hace falta.
 *  Entonces bloqueamos en vez el primer bucket.
 *  ID bucket: ID tabla + nombre columna + hash + nro bucket.
 *
 * Formato de un bucket: una lista con los ID de los registros cuyo valor en la columna
 * especificada concuerda con el hash del bucket.
 * Hay un arreglo de bits que indican los lugares de la lista libres
 * (pueden quedar huecos en el medio de la lista, pero nos evitamos reordenar y el iterador
 * va a ser inteligente y saltea los huecos)
 * Cuando se llena un bucket se crea otro (de la misma manera que una pagina).
 * 
 * NOTA: El iterador que devuelva el indice va a ser de aquellos registros
 * cuyo valor de hash en la columna concuerde con el hash del valor especificado.
 * O sea, Si pido aquellos registros cuya columna 1 sea igual a "valor1" y
 * "valor2" tiene el mismo hash que "valor1" entonces los indices me van a dar
 * no solo aquellos registros cuyo valor en la columna sea "valor1" sino tambien
 * los que tienen "valor2". 
 * Una optimizacion podria ser guardar el valor propio en el Bucket, eso haria
 * que solo se devuelvan los registros de valor "valor1" y aumentaria la concurrencia.
 * 
 * Una entrada en un bucket ocupa:
 * nro pagina + nro registro dentro de pagina. (La tabla a la que pertenece ya la conozco)
 * 4 bytes + 4 bytes = 8 bytes
 * Si un bucket tiene 4096 bytes:
 * 504 entradas: 504 bits del ArregloBits (63 bytes) y 504*8 bytes para las entradas
 * = 63 + 4032 = 4095.
 * 
 * Al no ser dinamico se puede dar el caso que una entrada de hash tenga muchos buckets,
 * pero aun asi va a ser mas concurrente que escanear toda la tabla. 
 * Ademas la idea del TP es otra, esto es solo un agregado para mejorar un poco la concurrencia
 * y proveer nivel aislamiento serializable.
 * 
 * Un hash tambien es util para las tablas del catalogo.
 * 
 * El numero de hash va a ser modulo 8 para evitar que haya muchos buckets con un solo elemento.
 * 
 * El sistema de ARIES nos va a permitir que los indices sean
 * tan transaccionales como las tablas comunes.
 * 
 * La interfaz Hash es una sola para cada tabla.
 * Adentro hay un HashColumna para cada columna de la tabla.
 * Cada HashColumna tiene Buckets segun el modulo.
 * 
 * La interfaz Hash no tiene estado.
 */
public interface HashManager {

	void agregarRegistro(Registro registro);
	
	boolean borrarRegistro(Registro registro);
	
	void actualizarRegistro(Registro viejoRegistro, Registro nuevoRegistro);
	
	Iterador<Registro.ID> dameRegistros(Tabla.ID idTabla, int columna, Object valor);
	
}
