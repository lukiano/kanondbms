/**
 * 
 */
package servidor.indice.hash.impl;

import servidor.excepciones.RegistroExistenteException;
import servidor.indice.hash.Bucket;
import servidor.indice.hash.RegistroIndice;
import servidor.log.LSN;
import servidor.log.Log;
import servidor.tabla.Registro;
import servidor.transaccion.Transaccion;
import servidor.transaccion.TransactionManager;

/**
 * @author luciano
 *
 */
class BucketDecoradorConLog extends AbstractBucketDecorador {
	
	private Log log;
	
	private TransactionManager transactionManager;

	public BucketDecoradorConLog(Bucket bucket, Log log, TransactionManager transactionManager) {
		super(bucket);
		this.log = log;
		this.transactionManager = transactionManager;
	}

	/**
	 * TODO: Documentar esto, segun ARIES (pag 114, en Transaction Table)
	 * @param lsn el LSN del ultimo evento operado sobre este Bucket.
	 */
	private void actualizarLSN(LSN lsn) {
		super.actualizarRecoveryLSN(lsn);
		Transaccion transaccion = this.transactionManager.dameTransaccion(); 
		transaccion.establecerUltimoLSN(lsn);
		transaccion.establecerUndoNextLSN(lsn);
	}

	private Transaccion dameTransaccionActual() {
		return this.transactionManager.dameTransaccion();
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#agregarRegistroIndice(servidor.indice.hash.RegistroIndice.ID, servidor.tabla.Registro.ID)
	 */
	@Override
	public void agregarRegistroIndice(RegistroIndice.ID idRegistroIndice, Registro.ID idRegistro) throws RegistroExistenteException {
		LSN lsn = this.log.escribirInsertIndex(this.dameTransaccionActual(), idRegistroIndice, idRegistro);
		this.actualizarLSN(lsn);
		super.agregarRegistroIndice(idRegistroIndice, idRegistro);
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#agregarRegistroIndice(servidor.tabla.Registro.ID)
	 */
	@Override
	public servidor.indice.hash.RegistroIndice.ID agregarRegistroIndice(Registro.ID idRegistro) {
		RegistroIndice.ID idRegistroIndice = super.dameIDRegistroIndiceLibre();
		try {
			this.agregarRegistroIndice(idRegistroIndice, idRegistro);
		} catch (RegistroExistenteException ignorado) {
			// no va a pasar
		}
		return idRegistroIndice;
	}

	/**
	 * @see servidor.indice.hash.impl.AbstractBucketDecorador#borrarRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	@Override
	public boolean borrarRegistroIndice(RegistroIndice.ID idRegistroIndice) {
		RegistroIndice registroIndice = super.dameRegistroIndice(idRegistroIndice);
		LSN lsn;
		try {
			 lsn = this.log.escribirDeleteIndex(this.dameTransaccionActual(), registroIndice);
		} finally {
			super.liberarRegistroIndice(idRegistroIndice);
		}
		this.actualizarLSN(lsn);
		return super.borrarRegistroIndice(idRegistroIndice);
	}

}
