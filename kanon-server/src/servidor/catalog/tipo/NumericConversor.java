/**
 * 
 */
package servidor.catalog.tipo;

import servidor.tabla.Campo;

/**
 * Convierte de un tipo de datos a un numero Integer.
 */
public final class NumericConversor extends Conversor {

    /**
     * @see servidor.catalog.tipo.Conversor#convertir(servidor.tabla.Campo, java.lang.Object)
     */
    @Override
    public Object convertir(Campo campo, Object valor) {
        switch (campo.tipo()) {
            case NUMERIC:
                return valor;
            case CHAR:
            	char[] cs = (char[]) valor;
                return Integer.valueOf(new String(cs));
            default:
                throw new RuntimeException("Imposible de realizar la conversion");
        }
    }

}
