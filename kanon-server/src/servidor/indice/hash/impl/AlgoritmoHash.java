/**
 * 
 */
package servidor.indice.hash.impl;

import servidor.tabla.Campo;

/**
 * Clase de ayuda que provee un numero de hash segun una entrada de una columna.
 * El numero de hash va a ser modulo 7 (primo) para evitar que haya muchos buckets con un solo elemento
 * lo que se traduce en muchos archivos y muchas paginas en el BufferManager.
 * Ademas recordar que esto es academico.
 */
final class AlgoritmoHash {
	
	public static final int MODULO = 7;

	/**
	 * Constructor privado para evitar instanciamiento de esta clase.
	 */
	private AlgoritmoHash() {
	}

	/**
	 * Devuelve el hash del valor de acuerdo al tipo de ese valor.
	 * Me aprovecho del la funcion hashCode provista por Java.
	 * @param campo la columna correspondiente
	 * @param valor una entrada en esa columna
	 * @return el hash resultante. 
	 */
	public static int dameHash(Campo campo, Object valor) {
		switch (campo.tipo()) {
			case CHAR: {// valor es un char array.
				if (!(valor.getClass().equals(char[].class))) {
					throw new ClassCastException("El valor no corresponde al tipo CHAR");
				}
				int hash = hashCode((char[])valor);
				return modulo(hash, MODULO);
			}
			case INTEGER: {
				if (!(valor.getClass().equals(Integer.class))) {
					throw new ClassCastException("El valor no corresponde al tipo NUMERIC");
				}
				int hash = valor.hashCode();
				return modulo(hash, MODULO);
			}
			default:
				throw new RuntimeException("Tipo no reconocido: " + campo.tipo().name());
		}
	}
	
    public static int hashCode(char[] value) {
    	int hash = 0;
	    for (int i = 0; i < value.length; i++) {
	    	char c = value[i];
    		hash = 31 * hash + c;
//	    	if (c >= 0) {
//	    		hash = 31 * hash + c;
//	    	} else {
//	    		hash = 31 * hash + (Character.MAX_VALUE + c);
//	    	}
        }
	    return hash;
	}
	
	/**
	 * Calcula el modulo de un numero.
	 */
	private static int modulo(int valor, int modulo) {
		int divisor = valor / modulo; // division entera
		int resultado = valor - (divisor * modulo);
		if (resultado < 0) {
			resultado = modulo + resultado; // que el resultado siempre sea positivo
		}
		return resultado;
	}
	
}
