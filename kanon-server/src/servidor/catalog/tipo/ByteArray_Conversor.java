/**
 * 
 */
package servidor.catalog.tipo;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import servidor.tabla.Campo;

/**
 * Convierte de un tipo de datos a un arreglo de bytes.
 */
public final class ByteArray_Conversor extends Conversor {

    /**
     * @see servidor.catalog.tipo.Conversor#convertir(servidor.tabla.Campo, java.lang.Object)
     */
    @Override
    public Object convertir(Campo campo, Object valor) {
        switch (campo.tipo()) {
            case INTEGER:
            {
                Integer integer = (Integer) valor;
                long l = integer.longValue();
                BigInteger bigInteger = BigInteger.valueOf(l);
            	byte[] bs = bigInteger.toByteArray(); // BigInteger hace el trabajo por nosotros.
            	return ByteHelper.normalizarCadena(bs, campo.longitud());
            }
            case CHAR:
                try {
                	char[] cadena = (char[])valor;
					byte[] bs = new String(cadena).getBytes("ISO-8859-1");
					return ByteHelper.normalizarCadena(bs, campo.longitud());
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Imposible de realizar la conversion", e);
				}
            default:
                throw new RuntimeException("Imposible de realizar la conversion");
        }
    }

}
