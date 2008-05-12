/**
 * 
 */
package servidor.buffer.latch.impl;

import java.util.Collection;

import servidor.buffer.latch.LatchManager;
import servidor.catalog.Valor;
import servidor.excepciones.RegistroExistenteException;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.tabla.impl.AbstractTablaDecorador;

/**
 * Decorador de una Tabla el cual obtiene un Latch antes de modificar una pagina, y lo libera luego de la modificacion.
 * Sirve para controlar la concurrencia en la escritura de las paginas de la tabla.
 */
public class TablaDecoradoraConLatch extends AbstractTablaDecorador {

	/**
	 * El Latch Manager sobre el cual se realizan los latches.
	 */
	private LatchManager latchManager;
	
	/**
	 * @param tablaDecorada la tabla a decorar.
	 * @param latchManager el Latch Manager sobre el cual se realizan los latches.
	 */
	public TablaDecoradoraConLatch(Tabla tablaDecorada, LatchManager latchManager) {
		super(tablaDecorada);
		this.latchManager = latchManager;
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#actualizarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	@Override
	public void actualizarRegistro(Registro.ID idRegistro, Collection<Valor> valores) {
		Pagina.ID idPagina = idRegistro.propietario();
		this.latchManager.latch(idPagina);
		try {
			super.actualizarRegistro(idRegistro, valores);
		} finally {
			this.latchManager.unLatch(idPagina);	
		}
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#borrarRegistro(servidor.tabla.Registro.ID)
	 */
	@Override
	public boolean borrarRegistro(Registro.ID idRegistro) {
		Pagina.ID idPagina = idRegistro.propietario();
		this.latchManager.latch(idPagina);
		try {
			return super.borrarRegistro(idRegistro);
		} finally {
			this.latchManager.unLatch(idPagina);	
		}
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#insertarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	@Override
	public void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
		Pagina.ID idPagina = idRegistro.propietario();
		this.latchManager.latch(idPagina);
		try {
			super.insertarRegistro(idRegistro, valores);
		} finally {
			this.latchManager.unLatch(idPagina);	
		}
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#liberarRegistro(servidor.tabla.Registro.ID)
	 */
	@Override
	public void liberarRegistro(Registro.ID idRegistro) {
		// XXX: parche:
		// si es llamado para las queries, no pasa nada. Si es llamado por un insertar fallido, libera el latch
		this.latchManager.unLatch(idRegistro.propietario()); 
		super.liberarRegistro(idRegistro);
	}

	/**
	 * @see servidor.tabla.impl.AbstractTablaDecorador#insertarRegistro(java.util.Collection)
	 */
	@Override
	public servidor.tabla.Registro.ID insertarRegistro(Collection<Valor> valores) {
		Registro.ID idRegistro = this.dameIdRegistroLibre();
		Pagina.ID idPagina = idRegistro.propietario();
		this.latchManager.latch(idPagina);
		try {
			super.insertarRegistro(idRegistro, valores);
		} catch (RegistroExistenteException e) {
			throw new RuntimeException(e);
		} finally {
			this.latchManager.unLatch(idPagina);	
		}
		return idRegistro;
	}

}
