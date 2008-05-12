package servidor.catalog.tipo;

/**
 * Clase con metodos de ayuda para el manejo de arreglo de caracteres.
 */
public class CharHelper {

	/**
	 * Constructor privado para evitar instanciamiento.
	 */
	private CharHelper() {};
	
	/**
	 * Acorta o agranda un arreglo de caracteres hasta una longitud determinada.
	 * @param cadena el arreglo de caracteres.
	 * @param longitudCampo la longitud del nuevo arreglo.
	 * @return un arreglo de caracteres con el contenido expandido a la nueva longitud (rellenado con 0x0000) o acortado
	 * si la longitud nueva es mas chica que la del arreglo original.
	 */
	public static char[] normalizarCadena(char[] cadena, int longitudCampo) {
		if (cadena.length < longitudCampo) {
			char[] cs = new char[longitudCampo];
			System.arraycopy(cadena, 0, cs, 0, cadena.length);
			char[] espacios = espacios(longitudCampo - cadena.length); 
			System.arraycopy(espacios, 0, cs, cadena.length, espacios.length);
			return cs;
		} else if (cadena.length > longitudCampo) {
			char[] cs = new char[longitudCampo];
			System.arraycopy(cadena, 0, cs, 0, longitudCampo);
			return cs;
		} else {
			return cadena;
		}
	}

    /**
     * Crea un arreglo de bytes de una longitud determinada llenos con 0x00.
     * @param longitud la longitud del nuevo arreglo de bytes.
     * @return un arreglo de bytes llenos con 0x00.
     */
    public static char[] espacios(int longitud) {
    	char[] espacios = new char[longitud];
    	for (int i = 0; i < longitud; i++) {
    		espacios[i] = ' ';
    	}
    	return espacios;
    }

}
