/**
 * 
 */
package servidor.ejecutor.xql;

import servidor.ejecutor.Resultado;
import Zql.ZStatement;

/**
 * XQL que se corresponde con ZLockTable. No se encuentra implementado.
 */
public class XLockTable implements XStatement {

	/**
	 * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
	 */
	public void zqlToXql(ZStatement zql) {
		// no implementado
	}

	/**
	 * @see servidor.ejecutor.xql.XStatement#execute()
	 */
	public Resultado execute() {
        throw new RuntimeException("Not implemented.");
	}

    /**
     * @see servidor.ejecutor.xql.XStatement#necesitaTransaccion()
     */
    public boolean necesitaTransaccion() {
        return false;
    }

}
