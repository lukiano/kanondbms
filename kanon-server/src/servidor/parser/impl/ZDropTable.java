package servidor.parser.impl;
import Zql.ZStatement;


/**
 * @author Julian R Berlin
 */
public class ZDropTable implements ZStatement {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9172712379507878763L;
	
	private String tableName;
	
	public ZDropTable() {
		super();
	}

	public ZDropTable(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@Override
	public String toString() {
		return "DROP TABLE " + this.tableName;
	} 
}
