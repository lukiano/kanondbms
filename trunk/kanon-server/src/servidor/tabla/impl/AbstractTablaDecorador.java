/**
 * 
 */
package servidor.tabla.impl;

import java.util.Collection;

import servidor.catalog.Valor;
import servidor.excepciones.RegistroExistenteException;
import servidor.tabla.Columna;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.util.Iterador;

/**
 * Clase abstracta para las clases que decoran una tabla para proveer funcionalidad extra.
 * Se adhiere al Design Pattern Decorator.
 */
public abstract class AbstractTablaDecorador implements Tabla {
	
	/**
	 * Variable con la tabla decorada.
	 */
	private Tabla tablaDecorada;

	/**
	 * Constructor de la clase. Esta clase no se puede instanciar por si misma.
	 * @param tablaDecorada la tabla a decorar.
	 */
	protected AbstractTablaDecorador(Tabla tablaDecorada) {
		this.tablaDecorada = tablaDecorada;
	}

	/**
	 * @see servidor.tabla.Tabla#id()
	 */
	public ID id() {
		return this.tablaDecorada.id();
	}

	/**
	 * @see servidor.tabla.Tabla#columnas()
	 */
	public Columna[] columnas() {
		return this.tablaDecorada.columnas();
	}

	/**
	 * @see servidor.tabla.OperaRegistros#registros()
	 */
	public Iterador<Registro.ID> registros() {
		return this.tablaDecorada.registros();
	}

	/**
	 * @see servidor.tabla.OperaRegistros#registrosDesde(servidor.tabla.Registro.ID)
	 */
	public Iterador<servidor.tabla.Registro.ID> registrosDesde(servidor.tabla.Registro.ID idRegistro) {
		return this.tablaDecorada.registrosDesde(idRegistro);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#registro(servidor.tabla.Registro.ID)
	 */
	public Registro registro(servidor.tabla.Registro.ID idRegistro) {
		return this.tablaDecorada.registro(idRegistro);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#liberarRegistro(servidor.tabla.Registro.ID)
	 */
	public void liberarRegistro(servidor.tabla.Registro.ID idRegistro) {
		this.tablaDecorada.liberarRegistro(idRegistro);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#actualizarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	public void actualizarRegistro(servidor.tabla.Registro.ID idRegistro,
			Collection<Valor> valores) {
		this.tablaDecorada.actualizarRegistro(idRegistro, valores);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#dameIdRegistroLibre()
	 */
	public Registro.ID dameIdRegistroLibre() {
		return this.tablaDecorada.dameIdRegistroLibre();
	}

	/**
	 * @see servidor.tabla.OperaRegistros#insertarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
	 */
	public void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
		this.tablaDecorada.insertarRegistro(idRegistro, valores);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#insertarRegistro(java.util.Collection)
	 */
	public Registro.ID insertarRegistro(Collection<Valor> valores) {
		return this.tablaDecorada.insertarRegistro(valores);
	}

	/**
	 * @see servidor.tabla.OperaRegistros#borrarRegistro(servidor.tabla.Registro.ID)
	 */
	public boolean borrarRegistro(servidor.tabla.Registro.ID idRegistro) {
		return this.tablaDecorada.borrarRegistro(idRegistro);
	}

}
