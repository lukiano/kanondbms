/**
 * 
 */
package servidor.catalog.tipo;

import servidor.tabla.Campo;

/**
 * Convierte de un tipo de datos a texto.
 */
public final class Texto_Conversor extends Conversor {

    /**
     * @see servidor.catalog.tipo.Conversor#convertir(servidor.tabla.Campo, java.lang.Object)
     */
    @Override
    public Object convertir(Campo campo, Object valor) {
        switch (campo.tipo()) {
            case INTEGER:
                Integer integer = (Integer) valor;
                return integer.toString();
            case CHAR:
            	return new String((char[])valor);
            default:
                throw new RuntimeException("Imposible de realizar la conversion");
        }
    }

}
