package servidor.transaccion;

/**
 * Enumeracion con los distintos niveles de aislamiento transaccionales soportados por el Lock Manager.
 */
public enum Aislamiento {
	
	/**
	 * Las transacciones pueden ver cambios no committeados por otras.
	 * Una consulta sobre un registro ejecutada dos veces puede devolver distintos resultados.
	 * Una consulta sobre un predicado ejecutada dos veces puede devolver elementos que antes no existian.
	 * No hay locks de lectura.
	 * Los locks de escritura se liberan luego del commit.
	 */
	READ_UNCOMMITTED,
	
	/**
	 * Las transacciones solo pueden ver cambios committeados.
	 * Una consulta sobre un registro ejecutada dos veces puede devolver distintos resultados.
	 * Una consulta sobre un predicado ejecutada dos veces puede devolver elementos que antes no existian.
	 * Los locks de lectura se liberan en el momento.
	 * Los locks de escritura se liberan luego del commit.
	 */
	READ_COMMITTED,
	
	/**
	 * Las transacciones solo pueden ver cambios committeados.
	 * Una consulta sobre un registro ejecutada dos veces va a devolver el mismo resultado.
	 * Una consulta sobre un predicado ejecutada dos veces puede devolver elementos que antes no existian.
	 * Los locks de lectura se liberan luego del commit.
	 * Los locks de escritura se liberan luego del commit.
	 */
	REPEATABLE_READ,
	
	/**
	 * Las transacciones solo pueden ver cambios committeados.
	 * Una consulta sobre un registro ejecutada dos veces va a devolver el mismo resultado.
	 * Una consulta sobre un predicado ejecutada dos veces va a devolver el mismo resultado.
	 * Los locks de lectura se liberan luego del commit.
	 * Los locks de escritura se liberan luego del commit.
	 * Si una consulta sobre un predicado utiliza un indice, este es bloqueado hasta luego del commit.
	 * Si una consulta sobre un predicado no utiliza ningun indice, toda la tabla es bloqueada hasta luego del commit.
	 */
	SERIALIZABLE;

}
