/**
 * 
 */
package servidor.fisico.impl;

import java.io.IOException;
import java.io.InputStream;

import servidor.buffer.Bloque;

/**
 * Implementacion basica de un bloque. Guarda un arreglo de bytes.
 */
public class BloqueImpl implements Bloque {
	
    private static final String INCONSISTENCY_DATA = "Inconsistency of data when creating block.";
    
	/**
	 * Variable para saber si el bloque se encuentra marcado.
	 */
	private boolean marca = false;
	
	/**
	 * Variable para saber si el bloque es valido.
	 */
	private boolean valido = true;
	
	/**
	 * Arreglo de bytes donde se guardan los datos.
	 */
	private byte[] datos;
	
	/**
	 * Constructor que crea un bloque con un arreglo de bytes del tamanio por omision.
	 * @see Bloque#TAMANIO
	 */
	public BloqueImpl() {
		this(TAMANIO);
	}

	/**
	 * Constructor que crea un bloque con un arreglo de bytes de tamanio determinado.
	 * @param tamanio el tamanio del arreglo de bytes de este bloque.
	 */
	public BloqueImpl(int tamanio) {
		this.datos = new byte[tamanio];
	}

	/**
	 * Constructor que crea un bloque con un arreglo de bytes del tamanio por omision y
	 * lo llena con la entrada del flujo.
	 * @param inputStream una entrada de datos que llenaran el arreglo de bytes.
	 * @throws RuntimeException si inputStream es NULL.
	 * @throws RuntimeException si ocurre un error de I/O.
	 */
	public BloqueImpl(InputStream inputStream) {
		if (inputStream == null) {
			throw new RuntimeException(INCONSISTENCY_DATA);
		}
		this.datos = new byte[TAMANIO];
		try {
			inputStream.read(this.datos);
		} catch (IOException e) {
			throw new RuntimeException(INCONSISTENCY_DATA, e);
		}
	}

	/**
	 * Constructor que crea un bloque a partir de un arreglo de bytes.
	 * @param datos el arreglo de bytes que contendra el nuevo bloque.
	 * @throws RuntimeException si el arreglo de bytes es de un tamanio distinto al tamanio por omision.
	 */
	public BloqueImpl(byte[] datos) {
		if( datos == null || datos.length != TAMANIO) {
			throw new RuntimeException(INCONSISTENCY_DATA);			
		}
		this.datos = datos.clone();
	}

	/**
	 * @see servidor.buffer.Bloque#dameDatos()
	 */
	public byte[] dameDatos() {
		return this.datos;
	}

	/**
	 * @see servidor.buffer.Marcable#desMarcar()
	 */
	public void desMarcar() {
		this.marca = false;
	}

	/**
	 * @see servidor.buffer.Marcable#marcado()
	 */
	public boolean marcado() {
		return this.marca;
	}

	/**
	 * @see servidor.buffer.Marcable#marcar()
	 */
	public void marcar() {
		this.marca = true;
	}

	/**
	 * @see servidor.buffer.Validable#invalidar()
	 */
	public void invalidar() {
		this.valido = false;
	}

	/**
	 * @see servidor.buffer.Validable#valido()
	 */
	public boolean valido() {
		return this.valido;
	}

}
