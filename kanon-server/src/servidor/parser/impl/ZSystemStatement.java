package servidor.parser.impl;

import Zql.ZStatement;

public class ZSystemStatement implements ZStatement {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7897728330675095953L;
	
	private String statement;
	
	public ZSystemStatement(String statement) {
		this.statement = statement;
	}
	
	public String getStatament() {
		return this.statement;
	}

}
