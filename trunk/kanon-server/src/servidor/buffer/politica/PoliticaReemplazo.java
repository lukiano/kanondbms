package servidor.buffer.politica;

import servidor.buffer.Bloque.ID;

/**
 * Interfaz para el uso de politicas de reemplazo de bloques en el Buffer Manager.
 */
public interface PoliticaReemplazo {

    /**
     * Informa a la politica de reemplazo que un bloque fue accedido.
     * @param id el ID del bloque accedido.
     */
    void accedido(ID id);
    
    /**
     * Informa a la politica de reemplazo que un bloque fue creado.
     * @param id el ID del nuevo bloque creado.
     */
    void creado(ID id);
    
    /**
     * Informa a la politica de reemplazo que un bloque fue quitado.
     * @param id el ID del bloque removido.
     */
    void removido(ID id);
    
    /**
     * Metodo para preguntarle a la politica de reemplazo cual es,
     * segun su criterio, el ID del proximo bloque a remover.
     * @return el ID del bloque a remover segun el criterio de esta politica.
     */
    ID aRemover();
    
    /**
     * Metodo para preguntarle a la politica de reemplazo cual es,
     * segun su criterio, el ID del bloque proximo al
     * que tiene el ID pasado por parametro.
     * @param id el ID de un bloque.
     * @return el ID del bloque proximo al pasado por parametro.
     */
    ID proximoARemover(ID id);
    
}
