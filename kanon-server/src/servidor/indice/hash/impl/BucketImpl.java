/**
 * 
 */
package servidor.indice.hash.impl;

import java.util.NoSuchElementException;

import servidor.buffer.Bloque;
import servidor.buffer.BufferManager;
import servidor.catalog.Catalogo;
import servidor.excepciones.RegistroExistenteException;
import servidor.indice.hash.Bucket;
import servidor.indice.hash.RegistroIndice;
import servidor.log.LSN;
import servidor.log.impl.LogHelper;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.util.ArregloBits;
import servidor.util.Iterador;

/**
 *
 */
class BucketImpl implements Bucket {

	/**
	 * cantidad de bytes reservados para el arreglo de registros libres.
	 */
	private static final int TAMANIO_BITSET = 64;
	
	/**
	 * Cantidad de bytes que ocupa el LSN.
	 */
	private static final int TAMANIO_LSN = Catalogo.LONGITUD_LONG;
	
	private static final int DATOS_RESERVADOS = TAMANIO_BITSET + TAMANIO_LSN;
	

	
	private static final int LONGITUD_REGISTRO = Catalogo.LONGITUD_INT + Catalogo.LONGITUD_INT;

	private Bucket.ID id;
	
	private Bloque contenido;
    
    private ArregloBits libres;
    
    private BufferManager bufferManager;
	
	/**
	 * 
	 */
	public BucketImpl(BufferManager bufferManager, Bucket.ID id, Bloque bloque) {
        this.id = id;
        this.bufferManager = bufferManager;
        byte[] datos = bloque.dameDatos();
    	int cantidadRegistros = (datos.length - DATOS_RESERVADOS) / LONGITUD_REGISTRO;
    	if (cantidadRegistros > TAMANIO_BITSET * Byte.SIZE) {
    		cantidadRegistros = TAMANIO_BITSET * Byte.SIZE;
    	}
    	this.libres = new ArregloBits(datos, cantidadRegistros);
    	this.contenido = bloque;
    	
	}

	/**
	 * @see servidor.indice.hash.Bucket#agregarRegistroIndice(servidor.indice.hash.RegistroIndice.ID, servidor.tabla.Registro.ID)
	 */
	public void agregarRegistroIndice(RegistroIndice.ID idRegistroIndice, Registro.ID idRegistro) throws RegistroExistenteException {
    	if (!this.libres.marcar(idRegistroIndice.numeroRegistroIndice())) {
    		throw new RegistroExistenteException("No se puede insertar elemento en el indice " + idRegistroIndice + " con " + idRegistro + " pues se encuentra ocupado con " + this.dameRegistroIndice(idRegistroIndice).registroReferenciado()); 
    	}
    	this.contenido.marcar();
    	byte[] campo = this.dameCampo(idRegistro);
    	int index = DATOS_RESERVADOS + LONGITUD_REGISTRO * idRegistroIndice.numeroRegistroIndice();
		System.arraycopy(campo, 0, this.contenido.dameDatos(), index, LONGITUD_REGISTRO);
	}

	/**
	 * @see servidor.indice.hash.Bucket#agregarRegistroIndice(servidor.tabla.Registro.ID)
	 */
	public RegistroIndice.ID agregarRegistroIndice(servidor.tabla.Registro.ID idRegistro) {
    	RegistroIndice.ID idRegistroIndiceLibre = this.dameIDRegistroIndiceLibre();
    	if (idRegistroIndiceLibre == null) {
    		throw new RuntimeException("El bucket " + this.id() + " esta lleno. Imposible insertar un nuevo indice.");
    	}
    	try {
			this.agregarRegistroIndice(idRegistroIndiceLibre, idRegistro);
		} catch (RegistroExistenteException ignorado) {
			// no va a pasar
		}
		return idRegistroIndiceLibre;
	}

	private byte[] dameCampo(Registro.ID idRegistro) {
		byte[] campo = new byte[LONGITUD_REGISTRO];
		int pagina = idRegistro.propietario().numeroPagina();
		int registro = idRegistro.numeroRegistro();
		
		byte[] numeroEnBytes = this.dameBytes(pagina);
		System.arraycopy(numeroEnBytes, 0, campo, 0, numeroEnBytes.length);
		
		numeroEnBytes = this.dameBytes(registro);
		System.arraycopy(numeroEnBytes, 0, campo, Catalogo.LONGITUD_INT, numeroEnBytes.length);
		
		return campo;
	}

	/**
	 * Convierte un numero a su representacion en Bytes.
	 * @param numero el numero a convertir.
	 * @return un arreglo de bytes con la representacion del numero.
	 * @see LogHelper#enteroAByteArray(int)
	 */
	private byte[] dameBytes(int numero) {
		return LogHelper.enteroAByteArray(numero);
	}
	
	/**
	 * @see servidor.indice.hash.Bucket#borrarRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	public boolean borrarRegistroIndice(RegistroIndice.ID idRegistroIndice) {
		int i = idRegistroIndice.numeroRegistroIndice();
		if (this.libres.marcado(i)) {
        	this.libres.desmarcar(i);
        	this.contenido.marcar();
        	// no se borra el bucket
//        	if (this.libres.vacio()) {
//        		this.bufferManager.borrarBloque(this.id());
//        	}
        	return true;
		}
		return false;
	}

	/**
	 * @see servidor.indice.hash.Bucket#bucketLleno()
	 */
	public boolean bucketLleno() {
		return this.libres.lleno();
	}

	/**
	 * @see servidor.indice.hash.Bucket#dameRegistrosIndice()
	 */
	public Iterador<RegistroIndice.ID> dameRegistrosIndice() {
        return new Iterador<RegistroIndice.ID>() {
        	
        	private int contador = 0;
		
			public void cerrar() {
				// no hace nada
			}
		
			public RegistroIndice.ID proximo() {
				if (this.contador >= BucketImpl.this.libres.tamanio()) {
					throw new NoSuchElementException("Ya no quedan elementos.");
				}
				RegistroIndice.ID idRegistroIndice = RegistroIndice.ID.nuevoID(BucketImpl.this.id(), this.contador);
				this.contador++;
				return idRegistroIndice;
			}
		
			public boolean hayProximo() {
				while (this.contador < BucketImpl.this.libres.tamanio()) {
					if (BucketImpl.this.libres.marcado(this.contador)) {
						return true;
					}
					this.contador++;
				}
				return false;
			}
		
		};
	}

	/**
	 * Convierte un campo de indices (que contiene los bytes de la pagina y registro)
	 * en un ID de registro. El ID de la tabla es el mismo que el del Bucket.
	 * @param idRegistroIndice el ID del registroIndice.
	 * @return una implementacion de RegistroIndice con los datos que corresponden segun el ID.
	 */
	public RegistroIndice dameRegistroIndice(RegistroIndice.ID idRegistroIndice) {
		if (!this.libres.marcado(idRegistroIndice.numeroRegistroIndice())) {
			return null;
		}
    	byte[] campo = new byte[LONGITUD_REGISTRO];
    	int indice = DATOS_RESERVADOS + LONGITUD_REGISTRO * idRegistroIndice.numeroRegistroIndice();
		System.arraycopy(this.contenido.dameDatos(), indice, campo, 0, LONGITUD_REGISTRO);
		int pagina;
		int registro;
		
		byte[] paginaBytes = new byte[Catalogo.LONGITUD_INT];
		System.arraycopy(campo, 0, paginaBytes, 0, Catalogo.LONGITUD_INT);
		pagina = LogHelper.byteArrayAEntero(paginaBytes);

		byte[] registroBytes = new byte[Catalogo.LONGITUD_INT];
		System.arraycopy(campo, Catalogo.LONGITUD_INT, registroBytes, 0, Catalogo.LONGITUD_INT);
		registro = LogHelper.byteArrayAEntero(registroBytes);

		Tabla.ID idTabla = this.id.propietario().propietario();
		Pagina.ID idPagina = Pagina.ID.nuevoID(idTabla, pagina);
		return new RegistroIndiceImpl(idRegistroIndice, Registro.ID.nuevoID(idPagina, registro), this);
	}
	
	/**
	 * @see servidor.indice.hash.Bucket#liberarRegistroIndice(servidor.indice.hash.RegistroIndice.ID)
	 */
	public void liberarRegistroIndice(RegistroIndice.ID idRegistroIndice) {
		// no hace nada
	}

	/**
	 * @see servidor.indice.hash.Bucket#esValido()
	 */
	public boolean esValido() {
    	// == porque debe ser la misma instancia
    	Bloque bloque = this.bufferManager.dameBloqueSoloSiEnMemoria(this.id);
    	if (bloque == null) {
    		return false;
    	}
    	try {
    		return this.contenido == bloque;
    	} finally {
    		this.bufferManager.liberarBloque(this.id);
    	}
	}

	/**
	 * @see servidor.indice.hash.Bucket#dameIDRegistroIndiceLibre()
	 */
	public RegistroIndice.ID dameIDRegistroIndiceLibre() {
		if (!this.libres.lleno()) {
    		for (int i = 0; i < this.libres.tamanio(); i++) {
    			if (!this.libres.marcado(i)) {
    				return RegistroIndice.ID.nuevoID(this.id(), i);
    			}
    		}
    	}
		return null;
	}

	/**
	 * @see servidor.indice.hash.Bucket#id()
	 */
	public ID id() {
		return this.id;
	}

	/**
	 * @see servidor.indice.hash.Bucket#actualizarRecoveryLSN(servidor.log.LSN)
	 */
	public void actualizarRecoveryLSN(LSN nuevoLSN) {
		byte[] lsnByte = LogHelper.LSNAByteArray(nuevoLSN);
		System.arraycopy(lsnByte, 0, this.contenido.dameDatos(), TAMANIO_BITSET, TAMANIO_LSN);
		this.contenido.marcar();
	}

	/**
	 * @see servidor.indice.hash.Bucket#recoveryLSN()
	 */
	public LSN recoveryLSN() {
		byte[] lsnByte = new byte[TAMANIO_LSN];
		System.arraycopy(this.contenido.dameDatos(), TAMANIO_BITSET, lsnByte, 0, TAMANIO_LSN);
		return LogHelper.byteArrayALSN(lsnByte);
	}

}
