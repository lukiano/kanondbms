/**
 * 
 */
package servidor.tabla.impl;

import java.util.Collection;
import java.util.NoSuchElementException;

import servidor.buffer.Bloque;
import servidor.buffer.BufferManager;
import servidor.catalog.Catalogo;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.excepciones.RegistroExistenteException;
import servidor.log.LSN;
import servidor.log.impl.LogHelper;
import servidor.tabla.Columna;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.util.ArregloBits;
import servidor.util.Iterador;

/**
 * @author lleggieri
 *
 */
public class PaginaImpl implements Pagina {

//	 esta implementacion no debe contener ninguna variable de estado.
	
	/**
	 * Cantidad de bytes reservados para el arreglo de registros libres.
	 */
	private static final int TAMANIO_BITSET = 16;
	
	/**
	 * Cantidad de bytes que ocupa el LSN.
	 */
	private static final int TAMANIO_LSN = Catalogo.LONGITUD_LONG;
	
	private static final int DATOS_RESERVADOS = TAMANIO_BITSET + TAMANIO_LSN;
	
	private Pagina.ID id;
	
	private Bloque contenido;
    
    private ArregloBits libres;
    
    private Columna[] columnas;
    
    private int longitudRegistro;
    
    private BufferManager bufferManager;
    
    public PaginaImpl(BufferManager bufferManager, Columna[] columnas, Pagina.ID id, Bloque bloque) {
    	// Se usan 16 bytes (2048 entradas maximo) para ver si una entrada esta ocupada o no. (TAMANIO_BITSET)
    	// Se asume que el bloque tiene un tamaño Bloque.TAMANIO y que es mayor a 16.
        this.id = id;
        this.bufferManager = bufferManager;
        this.columnas = columnas;
    	byte[] datos = bloque.dameDatos();
    	this.longitudRegistro = 0;
    	for (Columna columna : columnas) {
    		this.longitudRegistro += columna.campo().longitud();
    	}
    	int cantidadRegistros = (datos.length - DATOS_RESERVADOS) / this.longitudRegistro;
    	if (cantidadRegistros > TAMANIO_BITSET * Byte.SIZE) {
    		cantidadRegistros = TAMANIO_BITSET * Byte.SIZE;
    	}
    	this.libres = new ArregloBits(datos, cantidadRegistros);
    	this.contenido = bloque;
    }

    /**
     * @see servidor.tabla.Pagina#id()
     */
    public ID id() {
        return this.id;
    }
    
    /**
     * @see servidor.tabla.Pagina#aridad()
     */
    public int aridad() {
    	return this.columnas.length;
    }
    
    public boolean esValida() {
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
     * @see servidor.tabla.Pagina#paginaLlena()
     */
    public boolean paginaLlena() {
        return this.libres.lleno();
    }

    /**
     * @see servidor.tabla.OperaRegistros#registros()
     */
    /*
     * Fijarse en el arreglo de bits cuales son los registros ocupados
     * y devolver un iterador que se vaya fijando y devolviendo el proximo ocupado.
     * Decidir si devolver copia SNAPSHOT (toma los valores al momento de llamar el metodo)
     * o directa del arreglo (si luego de empezar a iterar la pagina sufre una modificacion
     * esta seria devuelta en el iterador). Tomar en cuenta el aislamiento
     */     
    public Iterador<Registro.ID> registros() {
        return new Iterador<Registro.ID>() {
        	
        	private int contador = 0;
		
			public void cerrar() {
				// no hace nada
			}
		
			public Registro.ID proximo() {
				if (this.contador >= PaginaImpl.this.libres.tamanio()) {
					throw new NoSuchElementException("Ya no quedan elementos.");
				}
				Registro.ID idRegistro = Registro.ID.nuevoID(PaginaImpl.this.id(), this.contador);
				this.contador++;
				return idRegistro;
			}
		
			public boolean hayProximo() {
				while (this.contador < PaginaImpl.this.libres.tamanio()) {
					if (PaginaImpl.this.libres.marcado(this.contador)) {
						return true;
					}
					this.contador++;
				}
				return false;
			}
		
		};
    }

    /**
     * @see servidor.tabla.OperaRegistros#registro(servidor.tabla.Registro.ID)
     */
    /* 
     *Para obtener un registro:
     * 1) Fijarse en el arreglo de bits si el registro esta ocupado o no
     * 2) Si no esta ocupado, devolver null
     * 3) calcular a partir del numero la posicion en el arreglo de datos
     * 4) desde esa posicion hasta una cantidad 'longitudRegistro' obtener los bytes
     * 5) Convertir esos bytes a datos y crear un registro con los mismos.
     * 6) devolver el registro
     */     
    public Registro registro(Registro.ID idRegistro) {
    	if (this.libres.marcado(idRegistro.numeroRegistro())) {
        	return this.dameRegistro(idRegistro);
    	}
    	return null;
    }

	/**
	 * @param idRegistro el ID del registro deseado.
	 * @return Una implementacion con los valores correspondientes segun el ID.
	 */
	private RegistroImpl dameRegistro(Registro.ID idRegistro) {
		int index = DATOS_RESERVADOS + this.longitudRegistro * idRegistro.numeroRegistro();
		byte[] campo = new byte[this.longitudRegistro];
		System.arraycopy(this.contenido.dameDatos(), index, campo, 0, this.longitudRegistro);
		return new RegistroImpl(this, idRegistro, campo, this.columnas);
	}

    /*
     * Para actualizar un registro:
     * 1) Fijarse en el arreglo de bits si el registro esta ocupado o no
     * 2) Si no esta ocupado, crear un registro con los valores
     * 3) Si esta ocupado:
     * 3a) calcular a partir del numero la posicion en el arreglo de datos
     * 3b) desde esa posicion hasta una cantidad 'longitudRegistro' obtener los bytes
     * 3c) Convertir esos bytes a datos y crear un registro con los mismos.
     * 3d) Modificar el registro con los nuevos valores
     * 4) Convertir el registro modificado a bytes
     * 5) Reemplazar la porcion de la pagina con los nuevos bytes
     * 6) Marcar que la pagina se modifico
     * Notar que la concurrencia es manejada por el bloqueo pesimista en los niveles de aislamiento
     * READ_COMMITTED y REPETEABLE_READ. Tambien en SERIALIZABLE si es implementado.
     */     
    /**
     * @see servidor.tabla.OperaRegistros#actualizarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
     */
    public void actualizarRegistro(Registro.ID idRegistro, Collection<Valor> valores) {
    	if (!this.libres.marcado(idRegistro.numeroRegistro())) {
    		this.libres.marcar(idRegistro.numeroRegistro());
    	}
    	RegistroImpl nuevoRegistro = this.dameRegistro(idRegistro);
        for (Valor valor : valores) {
        	int pos = valor.posicion();
    		Conversor conversor = Conversor.conversor(this.columnas[pos].campo().tipo());
    		Valor nuevoValor = Valor.nuevoValor(pos, 
    				this.columnas[pos].campo(), 
    				conversor.convertir(valor.campo(), valor.contenido()));
    		nuevoRegistro.establecerValor(nuevoValor);
        }
        this.contenido.marcar();
    	byte[] campo = this.dameCampo(nuevoRegistro);
    	int index = DATOS_RESERVADOS + this.longitudRegistro * idRegistro.numeroRegistro();
		System.arraycopy(campo, 0, this.contenido.dameDatos(), index, this.longitudRegistro);
    }

    private byte[] dameCampo(Registro registro) {
    	Conversor conversor = Conversor.conversorABytes();
    	byte[] campo = new byte[this.longitudRegistro];
    	int index = 0;
    	for (int i = 0; i < this.columnas.length; i++) {
    		Object valor = registro.valor(i);
    		int longitudCampo = this.columnas[i].campo().longitud(); 
    		if (valor != null) {
    			byte[] bs = (byte[]) conversor.convertir(this.columnas[i].campo(), valor);
    			int longitudReal; 
    			if (bs.length < longitudCampo) {
        			longitudReal = bs.length; 
    			} else {
    				longitudReal = longitudCampo;
    			}
    			System.arraycopy(bs, 0, campo, index, longitudReal);
    		}
    		index += longitudCampo; 
    	}
		return campo;
	}

    /*
     * 1) Obtener un numero de registro del arreglo de bits
     * 2) Si no hay (la pagina esta llena) lanzar excepcion
     * 3) Crear un registro nuevo con los valores
     * 4) Convertir el registro modificado a bytes
     * 5) Reemplazar la porcion de la pagina con los nuevos bytes
     * 6) Marcar que la pagina se modifico
     */
    /**
     * @see servidor.tabla.OperaRegistros#insertarRegistro(servidor.tabla.Registro.ID, java.util.Collection)
     */
    public void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException {
    	if (!this.libres.marcar(idRegistro.numeroRegistro())) {
    		throw new RegistroExistenteException("No se puede insertar elemento en el registro " + idRegistro + " con " + valores + " pues se encuentra ocupado con " + this.dameRegistro(idRegistro).getValores()); 
    	}
    	RegistroImpl nuevoRegistro = new RegistroImpl(this, idRegistro, this.columnas.length);	
    	for (Valor valor : valores) {
    		int pos = valor.posicion();
    		Conversor conversor = Conversor.conversor(this.columnas[pos].campo().tipo());
    		Valor nuevoValor = Valor.nuevoValor(pos, 
    				this.columnas[pos].campo(), 
    				conversor.convertir(valor.campo(), valor.contenido()));
    		nuevoRegistro.establecerValor(nuevoValor);
    	}
    	this.contenido.marcar();
    	byte[] campo = this.dameCampo(nuevoRegistro);
    	int index = DATOS_RESERVADOS + this.longitudRegistro * idRegistro.numeroRegistro();
		System.arraycopy(campo, 0, this.contenido.dameDatos(), index, this.longitudRegistro);
    }

    public Registro.ID dameIdRegistroLibre() {
    	int numeroRegistroLibre = this.dameRegistroLibre();
    	if (numeroRegistroLibre == -1) {
    		return null;
    	}
    	return Registro.ID.nuevoID(this.id(), numeroRegistroLibre);
    }

	/**
	 * 
	 */
	private int dameRegistroLibre() {
		if (!this.libres.lleno()) {
    		for (int i = 0; i < this.libres.tamanio(); i++) {
    			if (!this.libres.marcado(i)) {
    				return i;
    			}
    		}
    	}
		return -1;
	}

    /**
     * @see servidor.tabla.OperaRegistros#borrarRegistro(servidor.tabla.Registro.ID)
     */
    /*
     * 1) Fijarse en el arreglo de bits si el registro esta ocupado o no
     * 2) Si no lo esta, terminar
     * 3) marcar en el arreglo que el registro esta libre.
     * 4) Marcar que la pagina se modifico
     * No hace falta poner en 0 los bytes correspondientes.
     * Eso se podria hacer si de desea seguridad (WIPING)
     */
    public boolean borrarRegistro(Registro.ID idRegistro) {
        if (this.libres.marcado(idRegistro.numeroRegistro())) {
        	this.libres.desmarcar(idRegistro.numeroRegistro());
        	this.contenido.marcar();
        	// No se borra la pagina
//        	if (this.libres.vacio()) {
//        		this.bufferManager.borrarBloque(this.id());
//        	}
        	return true;
        }
        return false;
    }
    
    /**
     * @see servidor.tabla.OperaRegistros#liberarRegistro(servidor.tabla.Registro.ID)
     */
    public void liberarRegistro(Registro.ID idRegistro) {
    	// liberar registro no tiene propósito dentro de una página
    }

	public servidor.tabla.Registro.ID insertarRegistro(Collection<Valor> valores) {
		Registro.ID idRegistro = this.dameIdRegistroLibre();
		if (idRegistro == null) {
			throw new RuntimeException("La pagina " + this.id() + " se encuentra llena");
		}
		try {
			this.insertarRegistro(idRegistro, valores);
		} catch (RegistroExistenteException ignorado) {
			// no va a pasar
		}
		return idRegistro;
	}

	/**
	 * @see servidor.tabla.Pagina#actualizarRecoveryLSN(servidor.log.LSN)
	 */
	public void actualizarRecoveryLSN(LSN nuevoLSN) {
		byte[] lsnByte = LogHelper.LSNAByteArray(nuevoLSN);
		System.arraycopy(lsnByte, 0, this.contenido.dameDatos(), TAMANIO_BITSET, TAMANIO_LSN);
		this.contenido.marcar();
	}

	/**
	 * @see servidor.tabla.Pagina#recoveryLSN()
	 */
	public LSN recoveryLSN() {
		byte[] lsnByte = new byte[TAMANIO_LSN];
		System.arraycopy(this.contenido.dameDatos(), TAMANIO_BITSET, lsnByte, 0, TAMANIO_LSN);
		return LogHelper.byteArrayALSN(lsnByte);
	}

}
