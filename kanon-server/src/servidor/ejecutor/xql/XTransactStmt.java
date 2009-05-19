/**
 * 
 */
package servidor.ejecutor.xql;

import servidor.ejecutor.Resultado;
import servidor.transaccion.Aislamiento;
import servidor.transaccion.Estado;
import servidor.transaccion.FabricaTransactionManager;
import servidor.transaccion.TransactionManager;
import Zql.ZStatement;
import Zql.ZTransactStmt;

/**
 * XQL para la ejecucion de las sentencias relacionadas con el manejo explicito de transacciones y nivel de aislamiento.
 * @see servidor.parser.impl.TransaccionParser
 *
 */
public class XTransactStmt implements XStatement {

	/**
	 * Una cadena con el comando a ejecutar. Este comando se encuentra codificado en un lenguaje interno.
	 */
	private String comando;
	
	/**
	 * @see servidor.ejecutor.xql.XStatement#execute()
	 */
	public Resultado execute() {
		TransactionManager transactionManager = FabricaTransactionManager.dameInstancia();
		Resultado resultado = new Resultado();
        
        if (this.comando.equals("BEGIN")) {
            transactionManager.iniciarTransaccion();
            resultado.setMensaje("Transaction begun.");
        } else if (this.comando.equals("COMMIT")) {
            transactionManager.commitTransaccion();
            resultado.setMensaje("Transaction committed.");
        } else if (this.comando.startsWith("ROLLBACK")) {
        	if (this.comando.equals("ROLLBACK")) {
                transactionManager.abortarUltimaTransaccion();
                resultado.setMensaje("Transaction aborted.");
        	} else {
            	String[] sentencias = this.comando.split(" ");
            	String savepoint = sentencias[1];
                transactionManager.abortarUltimaTransaccionHasta(savepoint);
                resultado.setMensaje("Transaction aborted up to '" + savepoint + "'.");
        	}
        } else if (this.comando.startsWith("SAVEPOINT")) {
        	if (transactionManager.estadoActual() != Estado.EN_CURSO) {
        		throw new RuntimeException("Unable to set a savepoint outside a transaction.");
        	}
        	String[] sentencias = this.comando.split(" ");
        	String nombreSavepoint = sentencias[1];
            transactionManager.dameTransaccion().establecerSavepoint(nombreSavepoint);
            resultado.setMensaje("Savepoint '" + nombreSavepoint + "' set.");
        } else if (this.comando.startsWith("ISOLATION")) {
        	String[] sentencias = this.comando.split(" ");
        	String nombreAislamiento = sentencias[1];
        	Aislamiento aislamiento;
        	try {
        		aislamiento = Aislamiento.valueOf(nombreAislamiento);
        	} catch (IllegalArgumentException e) {
        		throw new RuntimeException("Unknown isolation level: " + nombreAislamiento);
        	}
            transactionManager.establecerAislamiento(aislamiento);
            resultado.setMensaje("Isolation " + nombreAislamiento + " set for future transactions.");
        } else {
            throw new RuntimeException("Transaction command not recognized: " + this.comando);
        }
		return resultado;
	}

	/**
	 * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
	 */
	public void zqlToXql(ZStatement zql) {
		ZTransactStmt stmt = (ZTransactStmt) zql;
		this.comando = stmt.getComment();
	}

    /**
     * @see servidor.ejecutor.xql.XStatement#necesitaTransaccion()
     */
    public boolean necesitaTransaccion() {
        return false;
    }

}
