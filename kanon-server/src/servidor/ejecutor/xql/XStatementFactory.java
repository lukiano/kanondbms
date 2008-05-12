package servidor.ejecutor.xql;

import java.util.HashMap;
import java.util.Map;

import servidor.parser.impl.ZCreateTable;
import servidor.parser.impl.ZDropTable;
import servidor.parser.impl.ZSystemStatement;
import Zql.ZDelete;
import Zql.ZInsert;
import Zql.ZLockTable;
import Zql.ZQuery;
import Zql.ZStatement;
import Zql.ZTransactStmt;
import Zql.ZUpdate;

/**
 * Fabrica que devuelve un XStatement a partir del ZStatement correspondiente.
 * Sigue el Design Pattern Strategy.
 * @see XStatement
 * @see ZStatement
 */
public final class XStatementFactory {
	
	/**
	 * Mapa donde se guarda la relacion entre ZQL y XQL.
	 */
	private static final Map<Class<? extends ZStatement>, Class<? extends XStatement> > mapper = 
		new HashMap<Class<? extends ZStatement>, Class<? extends XStatement> >();
	
	static {
		mapper.put(ZCreateTable.class, XCreateTable.class);
		mapper.put(ZDelete.class, XDelete.class);
		mapper.put(ZDropTable.class, XDropTable.class);
		mapper.put(ZInsert.class, XInsert.class);
		mapper.put(ZLockTable.class, XLockTable.class);
		mapper.put(ZQuery.class, XQuery.class);
		mapper.put(ZTransactStmt.class, XTransactStmt.class);
		mapper.put(ZUpdate.class, XUpdate.class);
		mapper.put(ZSystemStatement.class, XSystemStatement.class);
	}
	
	
	/**
	 * Metodo que devuelve una instancia de XQL en base a un ZQL.
	 * @param zstatement el ZQL que se desea ejecutar.
	 * @return una instancia de XQL.
	 */
	public static final XStatement getStatement(ZStatement zstatement) {
		Class<? extends XStatement> clazz = mapper.get(zstatement.getClass());
		if (clazz == null) {
			return null;
		}
		XStatement xstatement;
		try {
			xstatement = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		// este se puede remover para quitar la funcionalidad de sentencias atomicas.
		xstatement = new XStatement_TransaccionAutomatica_Decorador(xstatement);

		// este no se debe remover pues aborta las transacciones en caso de deadlock
		xstatement = new XStatement_ManejadorDeadlock_Decorador(xstatement);
		return xstatement;
	}

}
