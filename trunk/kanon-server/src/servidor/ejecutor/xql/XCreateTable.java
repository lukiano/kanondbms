package servidor.ejecutor.xql;

import java.util.Collection;

import servidor.catalog.Catalogo;
import servidor.catalog.FabricaCatalogo;
import servidor.ejecutor.Resultado;
import servidor.parser.impl.ZCreateTable;
import servidor.tabla.Columna;
import Zql.ZStatement;

/**
 * XQL para crear una tabla en el motor.
 * @author victor
 * @date 21/11/2005
 */
public class XCreateTable implements XStatement {

    /**
     * El nombre de la tabla a crear.
     */
    private String tableName;
    
    /**
     * Las columnas de la nueva tabla.
     */
    private Collection<Columna> columnas;

    /**
     * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
     */
    public void zqlToXql(ZStatement st) {
        ZCreateTable zql = (ZCreateTable) st;
        this.tableName = zql.getTableName();
        this.columnas = (zql.getColumnas()==null)?null:zql.getColumnas();
    }

    /**
     * @see servidor.ejecutor.xql.XStatement#execute()
     * @see Catalogo#crearTabla(String, Columna[])
     */
    public Resultado execute() {
        Resultado rto = new Resultado();
        Columna[] columnas = this.columnas.toArray(new Columna[0]);
        Catalogo catalogo = FabricaCatalogo.dameInstancia();
        catalogo.crearTabla(this.tableName, columnas);
        rto.setMensaje("Table '" + this.tableName + "' created");
        return rto;
    }
    
    /**
     * @see servidor.ejecutor.xql.XStatement#necesitaTransaccion()
     */
    public boolean necesitaTransaccion() {
        return true;
    }
    
}
