/**
 * 
 */
package servidor.catalog.tipo;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import servidor.tabla.Campo;

/**
 * Convierte de un arreglo de bytes a un tipo de datos.
 */
public final class DeByteArray_Conversor extends Conversor {

    /**
     * @see servidor.catalog.tipo.Conversor#convertir(servidor.tabla.Campo, java.lang.Object)
     */
    @Override
    public Object convertir(Campo campo, Object valor) {
        switch (campo.tipo()) {
            case INTEGER:
	            {
	        		byte[] bs = ByteHelper.trimIniciales((byte[])valor);
					return new BigInteger(bs).intValue(); // BigIntener hace el trabajo por nosotros
	            }
            case CHAR:
            	try {
            		byte[] bs = ByteHelper.trimIniciales((byte[])valor);
					return new String(bs, "ISO-8859-1").toCharArray();
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Imposible de realizar la conversion", e);
				}
            default:
                throw new RuntimeException("Imposible de realizar la conversion");
        }
    }
    

}
