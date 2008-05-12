/**
 * 
 */
package servidor.buffer.politica.impl;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Politica de reemplazo usando un algoritmo MFU.
 * Se va sumando las veces que se accede cada bloque.
 * Se remueve aquel que tiene mas accesos.
 * Las estructuras se heredan del algoritmo LFU.
 */
public class PR_MFU extends PR_LFU {
	
	/**
	 * Comparador inverso de las frecuencias.
	 */
	private static class ReverseComparator implements Comparator<Integer> {
		
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Integer i1, Integer i2) {
			return i2.compareTo(i1);
		}
	}

	/**
	 * Constructor de la clase. Las estructuras se inicializan igual que en LFU,
	 * pero el orden de las frecuencias utiliza un comparador inverso.
	 */
	public PR_MFU() {
		super();
        this.orden = new TreeSet<Integer>(new ReverseComparator());
	}

}
