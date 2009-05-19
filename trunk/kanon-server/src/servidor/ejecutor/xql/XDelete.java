package servidor.ejecutor.xql;

import servidor.catalog.Catalogo;
import servidor.catalog.FabricaCatalogo;
import servidor.ejecutor.Resultado;
import servidor.ejecutor.xql.IteradorConsulta.Comando;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import Zql.ZDelete;
import Zql.ZExp;
import Zql.ZStatement;
import Zql.ZTuple;
 
/**
 * XQL que realiza el borrado de registros de una tabla (sentencia DELETE).
 */
public class XDelete implements XStatement {

    /**
     * El nombre de la tabla cuyos registros van a ser borrados.
     */
    private String nombreTabla;
    
    /**
     * La expresion WHERE de la sentencia.
     */
    private ZExp where;
    
    /**
     * El iterador usado para recorrer la tabla.
     */
    private IteradorConsulta iteradorConsulta = new IteradorConsultaConIndices();
    
    /**
     * Comando que realiza el borrado de un registro.
     */
    private class DeleteComando implements Comando {
    	
    	/**
    	 * variable la cantidad de registros que fueron actualizados.
    	 */
    	private int cantidad = 0;

    	/**
    	 * @see servidor.ejecutor.xql.IteradorConsulta.Comando#ejecutarAccion(servidor.tabla.Tabla, servidor.tabla.Registro, Zql.ZTuple)
    	 */
    	public void ejecutarAccion(Tabla tabla, Registro registro, ZTuple tuple) {
            tabla.borrarRegistro(registro.id());
            this.cantidad++;
    	}
    	
    	/**
    	 * @return la cantidad de registros borrados.
    	 */
    	public int cantidad() {
    		return this.cantidad;
    	}

    }
    
    /**
     * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
     */
    public void zqlToXql(ZStatement st) {
        ZDelete zql = (ZDelete) st;
        this.nombreTabla = zql.getTable();
        this.where = zql.getWhere();
    }
    
    /**
     * @see servidor.ejecutor.xql.XStatement#execute()
     */
    public Resultado execute() {
        Catalogo catalogo = FabricaCatalogo.dameInstancia();
        Tabla tabla = catalogo.dameTabla(this.nombreTabla);
        if (tabla == null) {
        	throw new RuntimeException("No table exists with name '" + this.nombreTabla + "'.");
        }
        
        DeleteComando comando = new DeleteComando();
        this.iteradorConsulta.ejecutarParaCadaCoincidencia(tabla, this.where, comando);
        
        Resultado resultado = new Resultado();
        resultado.setMensaje("Successful removal of " + comando.cantidad() +
                       " record/s from table " + this.nombreTabla);
        return resultado;
    }

    /**
     * @see servidor.ejecutor.xql.XStatement#necesitaTransaccion()
     */
    public boolean necesitaTransaccion() {
        return true;
    }

}
