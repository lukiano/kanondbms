/**
 * 
 */
package servidor.buffer.politica.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import servidor.buffer.Bloque.ID;
import servidor.buffer.politica.PoliticaReemplazo;

/**
 * Politica de reemplazo usando un algoritmo LFU.
 * Se va sumando las veces que se accede cada bloque.
 * Se remueve aquel que tiene menos accesos.
 */
public class PR_LFU implements PoliticaReemplazo {
    
    /**
     * Indica para cada ID cuantas veces fue accedido.
     */
    protected Map<ID, Integer> bloques;
    
    /**
     * Mapa inverso al anterior. Guarda, para cada numero, aquellos IDs cuya frecuencia corresponde.
     */
    protected Map<Integer, Set<ID>> frecuencias;
    
    /**
     * Guarda de manera ordenada la cantidad de frecuencias distintas guardadas.
     * Ayuda en la eleccion de que elemento remover.
     */
    protected SortedSet<Integer> orden;

    /**
     * Constructor de la clase.
     */
    public PR_LFU() {
        this.bloques = new HashMap<ID, Integer>();
        this.frecuencias = new HashMap<Integer, Set<ID> >();
        this.orden = new TreeSet<Integer>();
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#accedido(servidor.buffer.Bloque.ID)
     */
    public synchronized void accedido(ID id) {
        Integer valorViejo = this.removerValor(id);
        if (valorViejo == null) {
        	this.agregarValor(id, 0);	
        } else {
        	Integer valorNuevo = valorViejo + 1;
        	this.agregarValor(id, valorNuevo);	
        }
    }

	/**
	 * Se remueve un bloque de los mapas.
	 * @param id el identificador a remover.
	 * @return la frecuencia de acceso que tenia ese bloqueo NULL si el ID no se encontraba en los mapas.
	 */
	private Integer removerValor(ID id) {
		Integer valorViejo = this.bloques.get(id);
		
		if (valorViejo != null) {
	        Set<ID> frecuenciasViejas = this.frecuencias.get(valorViejo);
	        if (frecuenciasViejas != null) {
	            frecuenciasViejas.remove(id);
	            if (frecuenciasViejas.isEmpty()) {
	                this.frecuencias.remove(valorViejo);
	            }
	        }
	        if (this.frecuencias.get(valorViejo) == null) {
	        	this.orden.remove(valorViejo);
	        }
		}
		return valorViejo;
	}

	/**
	 * Se agrega un bloque a los mapas con una frecuencia determinada.
	 * @param id el identificador a remover.
	 * @param valor el valor de la frecuencia de acceso.
	 */
	private void agregarValor(ID id, Integer valor) {
		this.orden.add(valor);
        Set<ID> frecuencias = this.frecuencias.get(valor);
        if (frecuencias == null) {
        	// no habia otros ID con la misma frecuencia.
            frecuencias = this.nuevoConjuntoFrecuencias();
            this.frecuencias.put(valor, frecuencias);
        }
        frecuencias.add(id);
        this.bloques.put(id, valor);
	}

    /**
     * @return un conjunto donde guardar las frecuencias.
     */
    protected Set<ID> nuevoConjuntoFrecuencias() {
        return new HashSet<ID>();
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#creado(servidor.buffer.Bloque.ID)
     */
    public synchronized void creado(ID id) {
        this.agregarValor(id, 0);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#removido(servidor.buffer.Bloque.ID)
     */
    public synchronized void removido(ID id) {
    	this.removerValor(id);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#aRemover()
     */
    public synchronized ID aRemover() {
    	Integer primerClave = this.orden.first();
        return this.elemento(primerClave);
    }

    /**
     * @see servidor.buffer.politica.PoliticaReemplazo#proximoARemover(servidor.buffer.Bloque.ID)
     */
    public ID proximoARemover(ID id) {
        Integer frecuenciaParametro = this.bloques.get(id);
        // Se obtienen aquellas frecuencias mayores o iguales a la que corresponde al parametro.
        SortedSet<Integer> conjuntoProximasFrecuencias = this.orden.tailSet(frecuenciaParametro);
        // Se realiza una copia del conjunto ordenado por si cambia mientras se recorre.
        conjuntoProximasFrecuencias = new TreeSet<Integer>(conjuntoProximasFrecuencias);
        // Se recorre el conjunto en el orden de las frecuencias.
        for (Integer frecuenciaProxima : conjuntoProximasFrecuencias) {
            Set<ID> ids = this.frecuencias.get(frecuenciaProxima);
            for (ID idEnFrecuencia : ids) {
                if (!idEnFrecuencia.equals(id)) {
                	// se retorna el primer ID que se encuentre y no sea igual al pasado por parametro.
                	// Puede ser otro ID con la misma frecuencia, o alguna mayor.
                    return idEnFrecuencia; 
                }
            }
        }
        // no hubo match.
        return null;
    }

	/**
	 * Se devuelve un ID que corresponda con la frecuencia de acceso. Se sabe que 
	 * al menos va a existir uno
	 * @param frecuencia un numero que indica la frecuencia.
	 * @return un ID que corresponda a la frecuencia pasada por parametro.
	 */
	private ID elemento(Integer frecuencia) {
		return this.frecuencias.get(frecuencia).iterator().next();
	}

}
