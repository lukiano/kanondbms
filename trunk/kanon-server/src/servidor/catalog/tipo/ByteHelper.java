package servidor.catalog.tipo;

/**
 * Clase con metodos de ayuda para el manejo de arreglo de bytes.
 */
public class ByteHelper {

	/**
	 * Constructor privado para evitar instanciamiento.
	 */
	private ByteHelper() {};
	
	/**
	 * Acorta o agranda un arreglo de bytes hasta una longitud determinada.
	 * @param cadena el arreglo de bytes.
	 * @param longitudCampo la longitud del nuevo arreglo.
	 * @return un arreglo de bytes con el contenido expandido a la nueva longitud (rellenado con 0x00) o acortado
	 * si la longitud nueva es mas chica que la del arreglo original.
	 */
	public static byte[] normalizarCadena(byte[] cadena, int longitudCampo) {
		if (cadena.length < longitudCampo) {
			byte[] arreglo = new byte[longitudCampo];
			byte[] espacios = espacios(longitudCampo - cadena.length); 
			System.arraycopy(espacios, 0, arreglo, 0, espacios.length);
			System.arraycopy(cadena, 0, arreglo, espacios.length, cadena.length);
			return arreglo;
		} else if (cadena.length > longitudCampo) {
			byte[] arreglo = new byte[longitudCampo];
			// inserto los ultimos bytes
			System.arraycopy(cadena, (cadena.length - longitudCampo), arreglo, 0, longitudCampo);
			return arreglo;
		} else {
			return cadena;
		}
	}
	
    /**
     * Elimina los bytes 0x00 restantes de una arreglo de bytes.
     * @param arreglo un arreglo de bytes.
     * @return un nuevo arreglo de bytes donde se eliminaron los bytes 0x00 al final del mismo en caso que hubiera habido.
     */
    public static byte[] trimRestantes(byte[] arreglo, int desde) {
    	
    	int indice = arreglo.length;
    	for (int i = desde; i < arreglo.length; i++) {
    		if (arreglo[i] == (byte)0) {
    			indice = i;
    			break;
    		}
    	}
    	if (indice == arreglo.length) {
    		return arreglo;
    	}
  		byte[] retBS = new byte[indice];
   		System.arraycopy(arreglo, 0, retBS, 0, indice);
   		return retBS;
    }

    /**
     * Elimina los bytes 0x00 iniciales de una arreglo de bytes.
     * @param arreglo un arreglo de bytes.
     * @return un nuevo arreglo de bytes donde se eliminaron los bytes 0x00 al comienzo del mismo en caso que hubiera habido.
     */
    public static byte[] trimIniciales(byte[] arreglo) {
    	
    	int indice = 0;
    	for (int i = 0; i < arreglo.length; i++) {
    		if (arreglo[i] != (byte)0) {
    			indice = i;
    			break;
    		}
    	}
    	int cantidad = arreglo.length - indice;
    	byte[] retBS = new byte[cantidad];
   		System.arraycopy(arreglo, indice, retBS, 0, cantidad);
   		return retBS;
    }

    
    /**
     * Crea un arreglo de bytes de una longitud determinada llenos con 0x00.
     * @param longitud la longitud del nuevo arreglo de bytes.
     * @return un arreglo de bytes llenos con 0x00.
     */
    public static byte[] espacios(int longitud) {
    	byte[] espacios = new byte[longitud];
    	for (int i = 0; i < longitud; i++) {
    		espacios[i] = 0;
    	}
    	return espacios;
    }

}
