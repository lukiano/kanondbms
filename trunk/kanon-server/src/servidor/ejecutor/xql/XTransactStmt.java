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
            resultado.setMensaje("Transaccion iniciada exitosamente.");
        } else if (this.comando.equals("COMMIT")) {
            transactionManager.commitTransaccion();
            resultado.setMensaje("Transaccion confirmada exitosamente.");
        } else if (this.comando.startsWith("ROLLBACK")) {
        	if (this.comando.equals("ROLLBACK")) {
                transactionManager.abortarUltimaTransaccion();
                resultado.setMensaje("Transaccion abortada exitosamente.");
        	} else {
            	String[] sentencias = this.comando.split(" ");
            	String savepoint = sentencias[1];
                transactionManager.abortarUltimaTransaccionHasta(savepoint);
                resultado.setMensaje("Transaccion abortada hasta '" + savepoint + "'.");
        	}
        } else if (this.comando.startsWith("SAVEPOINT")) {
        	if (transactionManager.estadoActual() != Estado.EN_CURSO) {
        		throw new RuntimeException("Imposible establecer un savepoint fuera de una transaccion.");
        	}
        	String[] sentencias = this.comando.split(" ");
        	String nombreSavepoint = sentencias[1];
            transactionManager.dameTransaccion().establecerSavepoint(nombreSavepoint);
            resultado.setMensaje("Savepoint con nombre '" + nombreSavepoint + "' establecido exitosamente.");
        } else if (this.comando.startsWith("ISOLATION")) {
        	String[] sentencias = this.comando.split(" ");
        	String nombreAislamiento = sentencias[1];
        	Aislamiento aislamiento;
        	try {
        		aislamiento = Aislamiento.valueOf(nombreAislamiento);
        	} catch (IllegalArgumentException e) {
        		throw new RuntimeException("Aislamiento no reconocido: " + nombreAislamiento);
        	}
            transactionManager.establecerAislamiento(aislamiento);
            resultado.setMensaje("Aislamiento " + nombreAislamiento + " establecido exitosamente para futuras transacciones.");
        } else {
            throw new RuntimeException("Comando de transaccion no reconocido: " + this.comando);
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
