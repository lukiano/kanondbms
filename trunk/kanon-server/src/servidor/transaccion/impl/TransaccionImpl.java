package servidor.transaccion.impl;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import servidor.log.LSN;
import servidor.transaccion.Aislamiento;
import servidor.transaccion.Estado;
import servidor.transaccion.Transaccion;

/**
 * Implementacion de una transaccion, guarda todos los datos necesarios para cumplir con la interfaz.
 */
class TransaccionImpl implements Transaccion {
	
    /**
     * Variable con el estado de la transaccion. 
     */
    private Estado estado;
    
    /**
     * Variable con el nivel de aislamiento de la transaccion.
     */
    private Aislamiento aislamiento;
    
    /**
     * identificador de la transaccion.
     */
    private ID id;
	 
    /**
     * Fecha de inicio de la transaccion.
     */
    private Date inicio;
    
    /**
     * Padre de la tranasccion, en caso de ser anidada. Puede ser NULL
     */
    private Transaccion padre;
    
    /**
     * Thread propietario de la transaccion. Depende de la conexion que lo creo.
     */
    private Thread propietario;
    
    /**
     * LSN del ultimo registro del log correspondiente a esta transaccion.
     */
    private LSN ultimoLSN;
    
    /**
     * Conjunto ordenado con los Undo Next LSN de la transaccion.
     */
    private SortedSet<LSN> undoNextLSN;
    
    /**
     * Mapa con los savepoints establecidos de la transaccion y sus correspondientes LSN.
     */
    private Map<String, LSN> savepoints;
	
	/**
	 * Constructor de la clase para una transaccion de alto nivel.
	 * @param id el identificador de la transaccion.
	 * @param propietario el thread propietario.
	 * @param aislamiento el nivel de aislamiento elegido.
	 */
	public TransaccionImpl(int id, Thread propietario, Aislamiento aislamiento) {
		this.inicio = new Date();
		this.id = ID.nuevoID(id);
        this.estado = Estado.EN_CURSO;
        this.aislamiento = aislamiento;
        this.propietario = propietario;
        this.undoNextLSN = new TreeSet<LSN>();
        this.savepoints = new HashMap<String, LSN>();
        this.ultimoLSN = LSN.LSN_NULO;
        this.undoNextLSN.add(LSN.LSN_NULO);
	}
	
	/**
	 * Constructor de la clase para una transaccion anidada.
	 * Se hereda el nivel de aislamiento y el thread propietario.
	 * @param id el identificador de la transaccion.
	 * @param padre la transaccion padre.
	 */
	public TransaccionImpl(int id, Transaccion padre) {
		this(id, padre.threadPropietario(), padre.aislamiento());
		this.padre = padre;
	}

	/**
	 * @see servidor.transaccion.Transaccion#id()
	 */
	public ID id() {
		return this.id;
	}

	/**
	 * @see servidor.transaccion.Transaccion#establecerUltimoLSN(servidor.log.LSN)
	 */
	public void establecerUltimoLSN(LSN lsn) {
		this.ultimoLSN = lsn;
	}
	
	/**
	 * @see servidor.transaccion.Transaccion#ultimoLSN()
	 */
	public LSN ultimoLSN() {
		return this.ultimoLSN;
	}
	
	/**
	 * @see servidor.transaccion.Transaccion#fechaInicio()
	 */
	public Date fechaInicio() {
		return this.inicio;
	}

    /**
     * @see servidor.transaccion.Transaccion#estado()
     */
    public Estado estado() {
        return this.estado;
    }
    
    /**
     * @see servidor.transaccion.Transaccion#aislamiento()
     */
    public Aislamiento aislamiento() {
    	return this.aislamiento;
    }
    
    /**
     * @see servidor.transaccion.Transaccion#padre()
     */
    public Transaccion padre() {
    	return this.padre;
    }
    
    /**
     * @see servidor.transaccion.Transaccion#threadPropietario()
     */
    public Thread threadPropietario() {
    	return this.propietario;
    }

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Transaccion) {
			return ((Transaccion)obj).id().equals(this.id());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.id().hashCode();
	}

	/**
	 * @see servidor.transaccion.Transaccion#undoNextLSN()
	 */
	public SortedSet<LSN> undoNextLSN() {
		return this.undoNextLSN;
	}

	/**
	 * @see servidor.transaccion.Transaccion#dameSavepoint(java.lang.String)
	 */
	public LSN dameSavepoint(String nombreSavepoint) {
		LSN savepoint = this.savepoints.get(nombreSavepoint);
		if (savepoint == null) {
			savepoint = LSN.LSN_NULO;
		}
		return savepoint;
	}

	/**
	 * @see servidor.transaccion.Transaccion#establecerSavepoint(java.lang.String)
	 */
	public void establecerSavepoint(String nombreSavepoint) {
		this.savepoints.put(nombreSavepoint, this.ultimoLSN());
	}

	/**
	 * @see servidor.transaccion.Transaccion#establecerUndoNextLSN(servidor.log.LSN)
	 */
	public void establecerUndoNextLSN(LSN lsn) {
		if (this.undoNextLSN.size() > 1) {
			throw new RuntimeException("Events to the transaction cannot be added during rollback.");
		}
		this.undoNextLSN.clear();
		this.undoNextLSN.add(lsn);
	}
    
}
