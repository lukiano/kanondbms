/**
 * 
 */
package servidor.tabla.impl;

import java.util.Collection;

import servidor.catalog.Valor;
import servidor.excepciones.RegistroExistenteException;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.transaccion.Estado;
import servidor.transaccion.Transaccion;
import servidor.transaccion.TransactionManager;

/**
 * @author lleggieri
 *
 */
final class TablaDecoradoraConNestedTopAction extends AbstractTablaDecorador {

    private TransactionManager transactionManager;
    
    private Log log;
    
	/**
	 * @param tablaDecorada
	 */
	public TablaDecoradoraConNestedTopAction(Tabla tablaDecorada, 
            TransactionManager transactionManager,
            Log log) {
		super(tablaDecorada);
		this.log = log;
        this.transactionManager = transactionManager;
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#actualizarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	@Override
	public void actualizarRegistro(Registro.ID idRegistro, Collection<Valor> valores) {
		this.verificarDentroTransaccion();
		LSN nta = this.dameUltimoLSN();
		super.actualizarRegistro(idRegistro, valores);
		this.escribirNTA(nta);
	}

	private void verificarDentroTransaccion() {
		if (!this.transactionManager.estadoActual().equals(Estado.EN_CURSO)) {
			throw new RuntimeException("No se pueden modificar registros fuera de una Transaccion");
		}
	}
	
	private LSN dameUltimoLSN() {
    	Transaccion transaccionActual = this.transactionManager.dameTransaccion(); 
    	return transaccionActual.ultimoLSN();
	}
	
	private void escribirNTA(LSN nta) {
    	Transaccion transaccionActual = this.transactionManager.dameTransaccion(); 
		LSN clrLSN = this.log.escribirDummyCLR(transaccionActual, nta);
		// actualizo el ultimo LSN de la transaccion
		transaccionActual.establecerUltimoLSN(clrLSN);
		transaccionActual.establecerUndoNextLSN(clrLSN);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#borrarRegistro(servidor.tabla.Registro.ID)
	 */
	@Override
	public boolean borrarRegistro(Registro.ID idRegistro) {
		this.verificarDentroTransaccion();
		LSN nta = this.dameUltimoLSN();
		boolean ret = super.borrarRegistro(idRegistro);
		this.escribirNTA(nta);
		return ret;
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#insertarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	@Override
	public void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
		this.verificarDentroTransaccion();
		LSN nta = this.dameUltimoLSN();
		super.insertarRegistro(idRegistro, valores);
		this.escribirNTA(nta);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#insertarRegistro(java.util.Collection)
	 */
	@Override
	public Registro.ID insertarRegistro(Collection<Valor> valores) {
		this.verificarDentroTransaccion();
		LSN nta = this.dameUltimoLSN();
		Registro.ID idRegistro = super.insertarRegistro(valores);
		this.escribirNTA(nta);
		return idRegistro;
	}

}
