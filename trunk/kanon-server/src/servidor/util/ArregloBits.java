package servidor.util;


/**
 * Clase que guarda el establecimiento de bits en un arreglo de bytes.
 * Estos bits se pueden marcar o desmarcar. Son usados para saber que registros se encuentran libres en una pagina o bucket.
 * @author luciano
 *
 */
public final class ArregloBits {

    private static final String INDEX_OUT_RANGE = "Index out of range.";

    /**
	 * El arreglo de bytes donde se guardan los bits.
	 */
	private byte[] bytes;
	
	/**
	 * longitud en BITS
	 */
	private int longitud;
	

	/**
	 * Constructor que utiliza todo el arreglo de bytes para guardar bits.
	 * @param bytes un arreglo de bytes.
	 */
	public ArregloBits(byte[] bytes) {
		this(bytes, bytes.length * Byte.SIZE);
	}

	/**
	 * Constructor que guarda de bits determinada en el arreglo de bytes.
	 * Se guardan al principio del arreglo.
	 * @param bytes un arreglo de bytes.
	 * @param longitud la cantidad de bits disponibles.
	 */
	public ArregloBits(byte[] bytes, int longitud) {
		this.bytes = bytes;
		this.longitud = longitud;
	}

	/**
	 * @return devuelve la cantidad de bits distintos disponibles.
	 */
	public int tamanio() {
		return this.longitud;
	}
	
	/**
	 * @return true si todos los bits estan limpios (desactivados).
	 */
	public boolean vacio() {
		int longitudBytes = this.longitud / Byte.SIZE;
		for (int i = 0; i < longitudBytes; i++) {
			if (this.bytes[i] != 0) {
				return false;
			}
		}
		int resto = longitudBytes * Byte.SIZE;
		for (int i = resto; i < this.longitud; i++) {
			if (this.marcado(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true si todos los bits se encuentran activados. 
	 */
	public boolean lleno() {
		int longitudBytes = this.longitud / Byte.SIZE;
		for (int i = 0; i < longitudBytes; i++) {
			if ((this.bytes[i] & 0xFF) != 0xFF) {
				return false;
			}
		}
		int resto = longitudBytes * Byte.SIZE;
		for (int i = resto; i < this.longitud; i++) {
			if (!this.marcado(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Activa un bit de los disponibles.
	 * @param indice el bit a activar.
	 * @return true si el bit no se encontraba marcado ya.
	 * @throws ArrayIndexOutOfBoundsException si el indice se encuentra fuera del rango.
	 */
	public boolean marcar(int indice) {
		if (indice < 0 || indice > this.tamanio()) {
			throw new ArrayIndexOutOfBoundsException(INDEX_OUT_RANGE);
		}
		int pos = indice / Byte.SIZE;
		int resto = indice - (pos * Byte.SIZE);
		byte b = this.bytes[pos];
		b |= this.potencia2(resto);
		synchronized (this.bytes) {
			byte old_b = this.bytes[pos];
			this.bytes[pos] = b;
			return old_b != b;
		}
		
	}

	/**
	 * Desactiva un bit de los disponibles
	 * @param indice el bit a desactivar.
	 * @return true si el bit no se encontraba limpio ya.
	 * @throws ArrayIndexOutOfBoundsException si el indice se encuentra fuera del rango.
	 */
	public boolean desmarcar(int indice) {
		if (indice < 0 || indice > this.tamanio()) {
			throw new ArrayIndexOutOfBoundsException(INDEX_OUT_RANGE);
		}
		int pos = indice / Byte.SIZE;
		int resto = indice - (pos * Byte.SIZE);
		byte b = this.bytes[pos];
		b &= (255 - this.potencia2(resto));
		synchronized (this.bytes) {
			byte old_b = this.bytes[pos];
			this.bytes[pos] = b;
			return old_b != b;
		}
	}

	/**
	 * Metodo para saber si un bit se encuentra activado.
	 * @param indice la posicion del bit que se desea conocer su estado.
	 * @return true si el bit se encuentra activo, false sino.
	 * @throws ArrayIndexOutOfBoundsException si el indice se encuentra fuera del rango.
	 */
	public boolean marcado(int indice) {
		if (indice < 0 || indice > this.tamanio()) {
			throw new ArrayIndexOutOfBoundsException(INDEX_OUT_RANGE);
		}
		int pos = indice / Byte.SIZE;
		int resto = indice - (pos * Byte.SIZE);
		byte b = this.bytes[pos];
		return (b | this.potencia2(resto)) == b;
	}

	/**
	 * Devuelve la potencia i-esima de 2.
	 * @param i la posicion se asume entre 0 y 7
	 * @return la potencia i-esima de 2.
	 */
	private byte potencia2(int i) {
		switch (i) {
			case 0: return 1;
			case 1: return 2;
			case 2: return 4;
			case 3: return 8;
			case 4: return 16;
			case 5: return 32;
			case 6: return 64;
			case 7: return (byte)128;
			default: throw new IndexOutOfBoundsException("The variable is not between 0 and 7.");
		}
	}
	
	/**
	 * Caso de test para la funcionalidad el Arreglo de Bits.
	 * @param args argumentos del programa, que son ignorados.
	 */
	public static void main(String[] args) {
		ArregloBits arregloBits = new ArregloBits(new byte[1]);
		arregloBits.marcar(4);
		System.out.println(arregloBits.marcado(4));
		System.out.println();
		arregloBits.marcar(5);
		System.out.println(arregloBits.marcado(5));
		System.out.println(arregloBits.marcado(4));
		System.out.println();
		arregloBits.desmarcar(4);
		System.out.println(arregloBits.marcado(5));
		System.out.println(arregloBits.marcado(4));
		System.out.println();

		arregloBits.marcar(7);
		System.out.println(arregloBits.marcado(7));
		System.out.println(arregloBits.marcado(5));
		System.out.println();
		arregloBits.marcar(0);
		System.out.println(arregloBits.marcado(7));
		System.out.println(arregloBits.marcado(0));
		System.out.println();
		arregloBits.desmarcar(5);
		System.out.println(arregloBits.marcado(7));
		System.out.println(arregloBits.marcado(5));
		System.out.println(arregloBits.marcado(0));
		System.out.println();
		arregloBits.desmarcar(7);
		System.out.println(arregloBits.marcado(7));
		System.out.println(arregloBits.marcado(5));
		System.out.println(arregloBits.marcado(0));
		System.out.println();
	}
	
}
