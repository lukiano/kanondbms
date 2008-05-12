/**
 * 
 */
package servidor.ejecutor.xql;

import servidor.conexion.FabricaServidorTcp;
import servidor.conexion.ServidorTcp;
import servidor.ejecutor.Resultado;
import servidor.transaccion.Estado;
import servidor.transaccion.FabricaTransactionManager;
import servidor.transaccion.Transaccion;
import servidor.transaccion.TransactionManager;

/**
 * Clase decoradora que encapsula a la sentencia que se ejecuta dentro de una transaccion en caso de no haber ninguna
 * o establece un Savepoint si ya la habia. Esto provee de atomicidad a las sentencias, pues se pueden abortar las
 * operaciones sobre la base de datos que se hayan realizado durante la ejecucion en caso de surgir una excepcion. 
 */
public class XStatement_TransaccionAutomatica_Decorador extends
		AbstractXStatementDecorador {

    /**
     * Variable con el Servidor de conexiones para saber si el motor esta siendo apagado (comun o crash).
     */
    private ServidorTcp servidorTcp;

	/**
	 * Constructor de la clase.
	 * @param statement el XStatement a decorar.
	 */
	public XStatement_TransaccionAutomatica_Decorador(XStatement statement) {
		super(statement);
		this.servidorTcp = FabricaServidorTcp.dameInstancia();
	}

	/**
	 * @see servidor.ejecutor.xql.AbstractXStatementDecorador#execute()
	 */
	@Override
	public Resultado execute() {
        // Si no existe ninguna transaccion activa, se crea una transaccion que encapsule la ejecucion de la sentencia 
        TransactionManager transactionManager = FabricaTransactionManager.dameInstancia();
        boolean transaccionAutomatica = false;
        
        if (this.necesitaTransaccion()) {
        	if (transactionManager.estadoActual() == Estado.NINGUNA) {
                // se crea una transaccion si no habia ninguna activa
                transactionManager.iniciarTransaccion();
                transaccionAutomatica = true;
            } else {
            	// se crea un savepoint en la transaccion activa por si hay que abortar la operacion
            	transactionManager.dameTransaccion().establecerSavepoint(Transaccion.AUTO_SAVEPOINT);
            }
        }
                
        Resultado resultado = new Resultado(); // aqui se alojara el resultado de la operacion
        try {
            resultado = super.execute();
            if (transaccionAutomatica && !this.servidorTcp.crash()) {
                // si se creo una transaccion entonces se commitea (salvo cuando ocurre un crash)
                transactionManager.commitTransaccion();
            }
            return resultado;
        } catch (RuntimeException e) {
        	if (transactionManager.estadoActual() == Estado.EN_CURSO && !this.servidorTcp.crash()) {
                if (transaccionAutomatica) {
                	transactionManager.abortarUltimaTransaccion();
                } else if (this.necesitaTransaccion() && !transaccionAutomatica) {
                	transactionManager.abortarUltimaTransaccionHasta(Transaccion.AUTO_SAVEPOINT);
                }
        	}
            throw e;
        }
	}

}
