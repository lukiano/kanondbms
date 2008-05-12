/**
 * 
 */
package servidor.buffer.politica.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import servidor.buffer.Bloque.ID;
import servidor.buffer.politica.PoliticaReemplazo;

/**
 * Politica de reemplazo usando un algoritmo LRU.
 * Es la politica por omision utilizada en el motor.
 */
public class PR_LRU implements PoliticaReemplazo {
    
    /**
     * Mapa que guarda, para cada vez, que ID fue accedido. El valor es unico por ID
     * y se guardan en orden de menor a mayor donde menor es mas viejo y mayor es mas reciente.
     */
    protected SortedMap<Long, ID> longs;
    
    /**
     * Mapa que guarda para cada ID en que momento fue accedido por ultima vez.
     */
    protected Map<ID, Long> ubicacion;
    
    /**
     * Contador incremental para saber cual fue el acceso mas reciente.
     * Es mas atomico que usar la fecha actual.
     */
    private AtomicLong atomicLong = new AtomicLong();

    /**
     * Constructor de la clase. Inicializa los mapas.
     */
    public PR_LRU() {
        super();
        this.longs = new TreeMap<Long, ID>();
        this.ubicacion = new HashMap<ID, Long>(); 
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#accedido(servidor.buffer.Bloque.ID)
     */
    public synchronized void accedido(ID id) {
    	Long clave = Long.valueOf(this.atomicLong.incrementAndGet());
    	if (this.ubicacion.containsKey(id)) {
    		// Se remueve la vieja ubicacion
    		Long viejaClave = this.ubicacion.remove(id);
    		this.longs.remove(viejaClave);
    	}
    	// Se agrega (o reagrega) con la ubicacion nueva (mas reciente).
        this.ubicacion.put(id, clave);
        this.longs.put(clave, id);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#creado(servidor.buffer.Bloque.ID)
     */
    public synchronized void creado(ID id) {
    	// al crear tambien se accede.
    	this.accedido(id);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#removido(servidor.buffer.Bloque.ID)
     */
    public synchronized void removido(ID id) {
        Long clave = this.ubicacion.remove(id);
        if (clave == null) {
        	throw new RuntimeException("Inconsistencia en la Politica de reemplazo");
        }
    	this.longs.remove(clave);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#aRemover()
     */
    public synchronized ID aRemover() {
        Long primerClave = this.longs.firstKey();
        // se devuelve el ID con ubicacion menor (mas vieja).
        return this.longs.get(primerClave);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#proximoARemover(servidor.buffer.Bloque.ID)
     */
    public ID proximoARemover(ID id) {
        Long claveParametro = this.ubicacion.get(id);
        if (claveParametro == null) {
        	// el ID pasado por parametro no existe => se devuelve el primero a remover.
        	return this.aRemover();
        }
        // Se copia el mapa con los ID cuya ubicacion es mayor a la del ID pasado por parametro.
        SortedMap<Long, ID> temporalMap = new TreeMap<Long, ID>(this.longs.tailMap(claveParametro));
        temporalMap.remove(claveParametro);
        
        if (temporalMap.isEmpty()) {
        	// no hay ningun ID posterior.
        	return null;
        }
        // Se devuelve el primer ID posterior.
        Long claveProxima = temporalMap.firstKey();
        return this.longs.get(claveProxima);
    }
    
}
