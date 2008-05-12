/**
 * 
 */
package servidor.buffer.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import servidor.buffer.Bloque;
import servidor.buffer.BufferManager;
import servidor.buffer.Bloque.ID;
import servidor.buffer.latch.LatchManager;
import servidor.buffer.pin.PinManager;
import servidor.buffer.politica.PoliticaReemplazo;
import servidor.fisico.DiskSpaceManager;
import servidor.fisico.FabricaDiskSpaceManager;
import servidor.log.FabricaRecoveryManager;

/**
 * Implementacion del BufferManager.
 * Usa LatchManager para el manejo de concurrencia en el acceso a bloques.
 * Usa PoliticaReemplazo para delegar el bloque a remover en caso de llenarse
 * el repositorio de bloques.
 * @author lleggieri
 */
public final class BufferManagerImpl implements BufferManager {
	
	/**
	 * La politica de reemplazo a utilizar.
	 */
	private PoliticaReemplazo politicaReemplazo;
    
    /**
     * Tamanio del pool de bloques.
     */
    private final int tamanioPool;
	
	/**
	 * Una instancia del Latch Manager.
	 */
	private LatchManager latchManager;
	
	/**
	 * Una instancia del Pin Manager.
	 */
	private PinManager pinManager;
	
	/**
	 * Una instancia del Disk Manager.
	 */
	private DiskSpaceManager diskSpaceManager;
	
	/**
	 * Mapa que guarda los bloques y su identificacion.
	 */
	private Map<ID, Bloque> pool;

    /**
     * Constructor de esta clase.
     * @param politicaReemplazo la politica de reemplazo que utilizara este administrador.
     */
    public BufferManagerImpl(PoliticaReemplazo politicaReemplazo, LatchManager latchManager, PinManager pinManager, int tamanioPool) {
    	this.diskSpaceManager = FabricaDiskSpaceManager.dameInstancia();
        this.politicaReemplazo = politicaReemplazo;
        this.latchManager = latchManager;
        this.pinManager = pinManager;
        this.tamanioPool = tamanioPool;
        this.pool = new ConcurrentHashMap<ID, Bloque>();
    }
    
    /**
     * @see servidor.buffer.BufferManager#getLatchManager()
     */
    public LatchManager getLatchManager() {
    	return this.latchManager;
    }
    
    private synchronized void quitarElementoSiMapaLleno() {
        ID idARemover = this.politicaReemplazo.aRemover();
        while (this.pinManager.estaPinneado(idARemover)) {
            idARemover = this.politicaReemplazo.proximoARemover(idARemover);
            if (idARemover == null) {
                throw new RuntimeException("The BufferManager has remained without free marks.");
            }
        }
        try {
            Bloque removido = this.pool.get(idARemover);
            if (removido.marcado()) {
            	FabricaRecoveryManager.dameInstancia().log().forzarADisco(); // primero log a disco segun WAL
                this.diskSpaceManager.guardarBloque(idARemover, removido);
            }
            removido.invalidar();
            this.politicaReemplazo.removido(idARemover);
            this.pool.remove(idARemover);
        } finally {
            this.pinManager.desPinnear(idARemover);
        }
    }


	/**
	 * @see servidor.buffer.BufferManager#dameBloque(servidor.buffer.Bloque.ID)
	 */
	public synchronized Bloque dameBloque(ID id) {
		Bloque bloque = this.pool.get(id);
		if (bloque == null) {
			bloque = this.diskSpaceManager.leerBloque(id);
			if (bloque == null) {
				return null;
			}
			this.pinManager.pinnear(id);
            if (this.pool.size() == this.tamanioPool) {
                this.quitarElementoSiMapaLleno();
            }
            this.pool.put(id, bloque);
			this.politicaReemplazo.accedido(id);
			return bloque;
		} else {
			this.pinManager.pinnear(id);
			this.politicaReemplazo.accedido(id);
			return bloque;
		}
	}

	/**
	 * @see servidor.buffer.BufferManager#dameBloqueSoloSiEnMemoria(servidor.buffer.Bloque.ID)
	 */
	public Bloque dameBloqueSoloSiEnMemoria(ID id) {
		Bloque bloque = this.pool.get(id);
		if (bloque == null) {
			return null;
		}
		this.pinManager.pinnear(id);
		this.politicaReemplazo.accedido(id);
		return bloque;
	}

	/**
	 * @see servidor.buffer.BufferManager#contieneBloque(servidor.buffer.Bloque.ID)
	 */
	public boolean contieneBloque(ID id) {
		return this.pool.containsKey(id);
	}

	/**
	 * @see servidor.buffer.BufferManager#liberarBloque(servidor.buffer.Bloque.ID)
	 */
	public void liberarBloque(ID id) {
		this.pinManager.desPinnear(id);
	}
	
	/**
	 * @see servidor.buffer.BufferManager#borrarBloque(servidor.buffer.Bloque.ID)
	 */
	public void borrarBloque(ID id) {
		Bloque bloque = this.pool.get(id);
		if (bloque != null) {
			synchronized (this) {
				bloque = this.pool.remove(id);
				if (bloque != null) { // puede ser que alguien lo haya removido
					bloque.invalidar();
					this.politicaReemplazo.removido(id);
				}
			}
		}
		this.diskSpaceManager.borrarBloque(id);
	}

	/**
	 * @see servidor.buffer.BufferManager#nuevoBloque(servidor.buffer.Bloque.ID)
	 */
	public synchronized Bloque nuevoBloque(ID id) {
		Bloque bloque = this.pool.get(id);
		if (bloque == null) {
			bloque = this.diskSpaceManager.nuevoBloque(id);
			this.pinManager.pinnear(id);
            if (this.pool.size() == this.tamanioPool) {
                this.quitarElementoSiMapaLleno();
            }
            this.pool.put(id, bloque);
    		this.politicaReemplazo.creado(id);
		}
		return bloque;
	}

    /**
     * @see servidor.buffer.BufferManager#guardarBloquesModificados()
     */
    public void guardarBloquesModificados() {
        for (Map.Entry<ID, Bloque> entrada : this.pool.entrySet()) {
            if (entrada.getValue().marcado()) {
            	FabricaRecoveryManager.dameInstancia().log().forzarADisco(); // primero log a disco segun WAL
                this.diskSpaceManager.guardarBloque(entrada.getKey(), entrada.getValue());
            }
        }
    }

	/**
	 * @see servidor.buffer.BufferManager#dameBloquesSucios()
	 */
	public Set<ID> dameBloquesSucios() {
		Set<ID> sucios = new HashSet<ID>();
		synchronized (this.pool) {
			for (Map.Entry<ID, Bloque> poolEntry : this.pool.entrySet()) {
				if (poolEntry.getValue().marcado()) {
					sucios.add(poolEntry.getKey());
				}
			}
		}
		return sucios;
	}

}
