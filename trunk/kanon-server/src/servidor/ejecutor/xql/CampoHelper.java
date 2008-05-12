package servidor.ejecutor.xql;

import Zql.ZConstant;
import servidor.catalog.Catalogo;
import servidor.catalog.tipo.Tipo;
import servidor.tabla.Campo;
import servidor.tabla.impl.CampoImpl;

/**
 * Metodos de ayuda para obtener un campo adecuado a partir de constantes (en formato ZQL o Java). 
 */
class CampoHelper {

	/**
	 * Constructor privado de la clase para evitar instanciamiento.
	 */
	private CampoHelper() {};
	
	/**
	 * Constance con un campo que contenga el tipo de datos numerico.
	 */
	private static final Campo CAMPO_NUMERIC = new CampoImpl(Tipo.NUMERIC, Catalogo.LONGITUD_INT);
	
	/**
	 * Devuelve un campo apropiado segun la constante ZQL.
	 * @param constante una constante ZQL.
	 * @return un campo apropiado (ej, tipo de datos numerico para una constante numerica, tipo de datos CHAR para una constante con texto).
	 */
	public static Campo dameCampo(ZConstant constante) {
		if (constante.getType() == ZConstant.NUMBER) {
			return CAMPO_NUMERIC;
		} else if (constante.getType() == ZConstant.STRING) {
			return new CampoImpl(Tipo.CHAR, constante.getValue().length());
        } else {
            throw new RuntimeException("Constante no soportada: " + constante);    
		}
	}

	/**
	 * Devuelve un campo apropiado segun la constante Java.
	 * @param constante una constante Java.
	 * @return un campo apropiado (ej, tipo de datos numerico para un Integer, tipo de datos CHAR para un String).
	 */
	public static Campo dameCampo(Object constante) {
		if (constante instanceof Integer) {
			return CAMPO_NUMERIC;
		} else if (constante instanceof String) {
			return new CampoImpl(Tipo.CHAR, ((String)constante).length());
        } else {
            throw new RuntimeException("Clase de valor no soportada: " + constante.getClass());    
		}
	}

	/**
	 * Realiza la comparacion de campos por su tipo.
	 * @param campo1 un campo.
	 * @param campo2 otro campo.
	 * @return true si ambos campos son del mismo tipo.
	 */
	public static boolean camposDelMismoTipo(Campo campo1, Campo campo2) {
		return campo1.tipo().equals(campo2.tipo());
	}

}
