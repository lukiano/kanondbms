package servidor.ejecutor.xql;

import servidor.catalog.Catalogo;
import servidor.catalog.FabricaCatalogo;
import servidor.ejecutor.Resultado;
import servidor.parser.impl.ZDropTable;
import Zql.ZStatement;

/**
 * XQL para borrar una tabla en el motor.
 * @author victor
 * @date 21/11/2005
 */
public class XDropTable implements XStatement {
    
    /**
     * Nombre de la tabla a borrar.
     */
    private String tableName;

    /**
     * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
     */
    public void zqlToXql(ZStatement st) {
        ZDropTable zql = (ZDropTable) st;
        this.tableName = zql.getTableName();
    }

    /**
     * @see servidor.ejecutor.xql.XStatement#execute()
     */
    public Resultado execute() {
        Resultado rto = new Resultado();

        Catalogo catalogo = FabricaCatalogo.dameInstancia();
        catalogo.borrarTabla(this.tableName);
        rto.setMensaje("Table '" + this.tableName + "' erased successfully.");

        return rto;
    }

    /**
     * @see servidor.ejecutor.xql.XStatement#necesitaTransaccion()
     */
    public boolean necesitaTransaccion() {
        return true;
    }

}
