/**
 * 
 */
package servidor.tabla;

import java.util.Collection;

import servidor.catalog.Valor;
import servidor.excepciones.RegistroExistenteException;
import servidor.util.Iterador;


/**
 * Interfaz base de aquellas que permiten operaciones con registros.
 */
public interface OperaRegistros {

    /**
     * Método para recorrer todos los registros.
     * @return un iterador con todos los registros.
     */
    Iterador<Registro.ID> registros();

    /**
     * Método para recorrer todos los registros a partir de uno determinado.
     * @return un iterador con registros.
     */
    
    Iterador<Registro.ID> registrosDesde(Registro.ID idRegistro);
    
    /**
     * Método para obtener un registro determinado.
     * @param idRegistro el Id del registro requerido.
     * @return el registro que concuerda con el Id pasado por parámetro.
     */
    Registro registro(Registro.ID idRegistro);
    
    /**
     * Metodo para liberar los recursos asociados a un registro.
     * @param idRegistro el Id del registro a liberar.
     */
    void liberarRegistro(Registro.ID idRegistro);
    
    /**
     * Método para actualizar un registro determinado.
     * @param idRegistro el Id del registro a actualizar.
     * @param valores los nuevos valores para el registro.
     */
    void actualizarRegistro(Registro.ID idRegistro, Collection<Valor> valores);
    
    /**
     * Puede devolver NULL.
     * @return un Id de Registro que no este siendo usado al momento de llamar a este metodo.
     */
    Registro.ID dameIdRegistroLibre();
    
    /**
     * Inserta un nuevo registro en el ID especificado.
     * @param valores los valores del nuevo registro.
     * @throws RegistroExistenteException si ya existe un registro con ese ID.
     */
    void insertarRegistro(Registro.ID idRegistro, Collection<Valor> valores) throws RegistroExistenteException;
    
    /**
     * Inserta un nuevo registro.
     * @param valores los valores del nuevo registro.
     * @return el Id del nuevo registro insertado.
     */
    Registro.ID insertarRegistro(Collection<Valor> valores);
    
    /**
     * Borra un registro.
     * @param idRegistro el Id del registro a borrar.
     * @return true si se borro el registro.
     */
    boolean borrarRegistro(Registro.ID idRegistro);
    
}
