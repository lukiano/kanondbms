package servidor.tabla.impl;
import java.util.Collection;

import servidor.catalog.tipo.Tipo;
import servidor.tabla.Campo;
import servidor.tabla.Columna;

/**
 * Implementacion basica de una Columna de una Tabla.
 * @author Julian R Berlin
 */
public final class ColumnaImpl implements Columna {

	/**
	 * Nombre de esta columna.
	 */
	private String nombre;
	
	/**
	 * Campo (tipo de datos) de esta columna.
	 */
	private Campo campo; 
	
	/**
	 * Posicion de esta columna dentro de la tabla.
	 */
	private int orden;
	
	/**
	 * Condiciones sobre la columna (ej, claves).
	 */
	private Collection constraint = null;

	/**
	 * Construye una columna a partir del nombre, longitud, orden y tipo especificado
	 * 
	 * @param nombre - Un String conteniendo el nombre de la columna 
	 * @param longitud - Un entero conteniendo el tamaño de la columna
	 * @param orden - Un entero conteniendo el orden de la columna dentro de la tabla
	 * @param tipo - Un TipoDeDato conteniendo el tipo de dato de la columna
	 * @see Tipo
	 */
	public ColumnaImpl(String nombre, int longitud, int orden, Tipo tipo) {
		this.nombre = nombre;
		this.campo = new CampoImpl(tipo, longitud);
		this.orden = orden;
		this.constraint = null;
	}

	/**
	 * Construye una columna a partir del nombre, orden y campo especificado
	 * 
	 * @param nombre - Un String conteniendo el nombre de la columna 
	 * @param campo - El campo con el tipo de datos y longitud del mismo en la columna.
	 * @param orden - Un entero conteniendo el orden de la columna dentro de la tabla
	 * @see Campo
	 */
	public ColumnaImpl(String nombre, Campo campo, int orden) {
		this.nombre = nombre;
		this.campo = campo;
		this.orden = orden;
		this.constraint = null;
	}

	/**
	 * Contruye una columna a partir del nombre, longitud, orden y tipo especificado. Por el momento las constraints son ignoradas.
	 * 
	 * @param nombre - Un String conteniendo el nombre de la columna 
	 * @param longitud - Un entero conteniendo el tamaño de la columna
	 * @param orden - Un entero conteniendo el orden de la columna dentro de la tabla
	 * @param tipo - Un TipoDeDato conteniendo el tipo de dato de la columna
	 * @param constraint - Una Collection de contraint representando las constraint de la columna
	 */
	public ColumnaImpl(String nombre, int longitud, int orden, Tipo tipo, Collection<String> constraint) {
		this(nombre, longitud, orden, tipo);
		//setConstraint(constraint);
	}

	/**
	 * Construye una ColumnaImp a partir de una ColumnaImp especificada
	 *  
	 * @param columna
	 */
	public ColumnaImpl(ColumnaImpl columna) {
		this.constraint = columna.constraint;
		this.campo = columna.campo;
		this.nombre = columna.nombre;
		this.orden = columna.orden;
	}

	/**
	 * @return el campo de esta columna.
	 */
	public Campo campo() {
		return this.campo;
	}
	
	/**
	 * @return Regresa el nombre.
	 */
	public String nombre() {
		return nombre;
	}

	/**
	 * @return Regresa las constraint
	 */
	public Collection getConstraint() {
		return constraint;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
    {
        String cTipo=this.campo.tipo().name();
        
		StringBuilder ret = new StringBuilder("(" + this.orden + ") " + this.nombre + " "
						 + cTipo + "(" + this.campo.longitud() + ")");
		return ret.toString();
	}
}