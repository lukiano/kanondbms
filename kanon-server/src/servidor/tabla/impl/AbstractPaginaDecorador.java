/**
 * 
 */
package servidor.tabla.impl;

import java.util.Collection;

import servidor.catalog.Valor;
import servidor.excepciones.RegistroExistenteException;
import servidor.log.LSN;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.util.Iterador;

/**
 * Clase abstracta para las clases que decoran una pagina para proveer funcionalidad extra.
 * Se adhiere al Design Pattern Decorator.
 */
public abstract class AbstractPaginaDecorador implements Pagina {
	
	/**
	 * Variable con la pagina decorada.
	 */
	private Pagina paginaDecorada;

	/**
	 * Constructor de la clase. Esta clase no se puede instanciar por si misma.
	 * @param paginaDecorada la pagina a decorar.
	 */
	public AbstractPaginaDecorador(Pagina paginaDecorada) {
		this.paginaDecorada = paginaDecorada;
	}

	/**
	 * @see servidor.tabla.Pagina#actualizarRecoveryLSN(servidor.log.LSN)
	 */
	public void actualizarRecoveryLSN(LSN nuevoLSN) {
		this.paginaDecorada.actualizarRecoveryLSN(nuevoLSN);
	}

	/**
	 * @see servidor.tabla.Pagina#aridad()
	 */
	public int aridad() {
		return this.paginaDecorada.aridad();
	}

	/**
	 * @see servidor.tabla.Pagina#esValida()
	 */
	public boolean esValida() {
		return this.paginaDecorada.esValida();
	}

	/**
	 * @see servidor.tabla.Pagina#id()
	 */
	public ID id() {
		return this.paginaDecorada.id();
	}

	/**
	 * @see servidor.tabla.Pagina#paginaLlena()
	 */
	public boolean paginaLlena() {
		return this.paginaDecorada.paginaLlena();
	}

	/**
	 * @see servidor.tabla.Pagina#recoveryLSN()
	 */
	public LSN recoveryLSN() {
		return this.paginaDecorada.recoveryLSN();
	}

	/**
	 * @see servidor.tabla.OperaRegistros#actualizarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	public void actualizarRegistro(servidor.tabla.Registro.ID idRegistro,
			Collection<Valor> valores) {
		this.paginaDecorada.actualizarRegistro(idRegistro, valores);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#borrarRegistro(servidor.tabla.Registro.ID)
	 */
	public boolean borrarRegistro(servidor.tabla.Registro.ID idRegistro) {
		return this.paginaDecorada.borrarRegistro(idRegistro);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#dameIdRegistroLibre()
	 */
	public servidor.tabla.Registro.ID dameIdRegistroLibre() {
		return this.paginaDecorada.dameIdRegistroLibre();
	}

	/**
	 * @see servidor.tabla.OperaRegistros#insertarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	public void insertarRegistro(servidor.tabla.Registro.ID idRegistro,
			Collection<Valor> valores) throws RegistroExistenteException {
		this.paginaDecorada.insertarRegistro(idRegistro, valores);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#insertarRegistro(java.util.Collection)
	 */
	public servidor.tabla.Registro.ID insertarRegistro(Collection<Valor> valores) {
		return this.paginaDecorada.insertarRegistro(valores);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#liberarRegistro(servidor.tabla.Registro.ID)
	 */
	public void liberarRegistro(servidor.tabla.Registro.ID idRegistro) {
		this.paginaDecorada.liberarRegistro(idRegistro);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#registro(servidor.tabla.Registro.ID)
	 */
	public Registro registro(servidor.tabla.Registro.ID idRegistro) {
		return this.paginaDecorada.registro(idRegistro);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#registros()
	 */
	public Iterador<servidor.tabla.Registro.ID> registros() {
		return this.paginaDecorada.registros();
	}

	/**
	 * @see servidor.tabla.OperaRegistros#registrosDesde(servidor.tabla.Registro.ID)
	 */
	public Iterador<servidor.tabla.Registro.ID> registrosDesde(servidor.tabla.Registro.ID idRegistro) {
		return this.paginaDecorada.registrosDesde(idRegistro);
	}

}
