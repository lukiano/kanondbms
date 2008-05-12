/**
 * 
 */
package servidor.ejecutor.xql;

import servidor.ejecutor.Resultado;
import Zql.ZStatement;

/**
 * Clase abstracta para las clases que decoran un XStatement para proveer funcionalidad extra.
 * Se adhiere al Design Pattern Decorator.
 */
public abstract class AbstractXStatementDecorador implements XStatement {
	
	/**
	 * Variable con el XStatement decorado.
	 */
	private XStatement statement;

	/**
	 * Constructor de la clase.
	 * @param statement el XStatement a decorar.
	 */
	public AbstractXStatementDecorador(XStatement statement) {
		this.statement = statement;
	}

	/**
	 * @see servidor.ejecutor.xql.XStatement#execute()
	 */
	public Resultado execute() {
		return this.statement.execute();
	}

	/**
	 * @see servidor.ejecutor.xql.XStatement#necesitaTransaccion()
	 */
	public boolean necesitaTransaccion() {
		return this.statement.necesitaTransaccion();
	}

	/**
	 * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
	 */
	public void zqlToXql(ZStatement zql) {
		this.statement.zqlToXql(zql);
	}

}
