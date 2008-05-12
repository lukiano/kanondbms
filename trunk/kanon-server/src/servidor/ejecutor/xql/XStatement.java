package servidor.ejecutor.xql;

import servidor.ejecutor.Resultado;
import Zql.ZStatement;

/**
 * Intefaz para la ejecucion de las distintas sentencias soportadas por el motor.
 * Delega del Ejecutor las implementaciones de cada una.
 * Se forma a partir de un ZQL, y obtiene los datos necesarios para ejecutarse.
 * @author victor
 * @date 21/11/2005
 *
 */
public interface XStatement {
    
    /**
     * Llena las variables de este XQL en base al ZQL asociado.
     * @param zql el ZStatement asociado.
     */
    void zqlToXql(ZStatement zql);
    
    /**
     * Realiza la ejecucion de la sentencia.
     * @return el resultado de la ejecucion. Ya sea un mensaje o una tabla (para las consultas).
     */
    Resultado execute();
    
    /**
     * Permite saber si la sentencia a ejecutar necesita estar encerrada dentro de una transaccion (Ej, DML).
     * @return true si una transaccion es necesaria.
     */
    boolean necesitaTransaccion(); 

}
