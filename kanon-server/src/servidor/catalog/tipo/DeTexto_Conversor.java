/**
 * 
 */
package servidor.catalog.tipo;

import servidor.tabla.Campo;

/**
 * Convierte de un texto a un tipo de datos.
 */
public final class DeTexto_Conversor extends Conversor {

    /**
     * @see servidor.catalog.tipo.Conversor#convertir(servidor.tabla.Campo, java.lang.Object)
     */
    @Override
    public Object convertir(Campo campo, Object valor) {
        switch (campo.tipo()) {
            case INTEGER:
                return Integer.parseInt((String)valor);
            case CHAR:
            	char[] cadena = ((String)valor).toCharArray();
            	return cadena;
            default:
                throw new RuntimeException("Imposible de realizar la conversion");
        }
    }

}
