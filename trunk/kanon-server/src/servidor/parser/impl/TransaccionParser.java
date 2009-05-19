package servidor.parser.impl;

import servidor.excepciones.ParseException;
import servidor.parser.Parser;
import Zql.TokenMgrError;
import Zql.ZStatement;
import Zql.ZTransactStmt;

/**
 * @author Julian R Berlin
 *
 */

class TransaccionParser implements Parser {
	
	/**
	 * 
	 */
	public TransaccionParser() {
		super();
	}

    /**************************************************************
     * Esta funcion realiza el parseo de la sentencia BEGIN TRANSACTION 
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @see ZStatement
     ***************************************************************/
    
    public static ZStatement parseBeginTransacctionStatement(String sentencia) throws ParseException, TokenMgrError {
    	int endOfStatementChar = sentencia.toLowerCase().indexOf(";");
    	if (endOfStatementChar == -1) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	sentencia = sentencia.substring(0, endOfStatementChar).trim();
    	
        //me fijo que esten las palabras clave
    	if (!sentencia.equalsIgnoreCase("begin transaction")) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	
        ZTransactStmt st = new ZTransactStmt("BEGIN");
        st.setComment("BEGIN");
        System.out.println(st.getComment());
        
        return st;
    }
    
    public static ZStatement parseSavepointStatement(String sentencia) throws ParseException, TokenMgrError {
    	int endOfStatementChar = sentencia.toLowerCase().indexOf(";");
    	if (endOfStatementChar == -1) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	sentencia = sentencia.substring(0, endOfStatementChar).trim();
    	String[] partesDeSentencia = sentencia.split(" ");
    	if (!partesDeSentencia[0].toLowerCase().equals("savepoint")) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	if (partesDeSentencia.length > 2) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	String savepoint = partesDeSentencia[1]; 
    	
    	ZTransactStmt st = new ZTransactStmt("SAVEPOINT");
        st.setComment("SAVEPOINT " + savepoint);
        System.out.println(st.getComment());
        
        return st;
    }

    public static ZStatement parseIsolationStatement(String sentencia) throws ParseException, TokenMgrError {
    	int endOfStatementChar = sentencia.toLowerCase().indexOf(";");
    	if (endOfStatementChar == -1) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	sentencia = sentencia.substring(0, endOfStatementChar).trim();
    	
    	String[] partesDeSentencia = sentencia.split(" ");
    	if (!partesDeSentencia[0].toLowerCase().equals("isolation")) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	if (partesDeSentencia.length > 2) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	
    	String aislamiento = partesDeSentencia[1]; 
    	
    	ZTransactStmt st = new ZTransactStmt("ISOLATION");
        st.setComment("ISOLATION " + aislamiento.toUpperCase());
        System.out.println(st.getComment());
        
        return st;
    }

    /**************************************************************
     * Esta funcion realiza el parseo de la sentencia COMMIT 
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @see ZStatement
     ***************************************************************/
    
    public static ZStatement parseCommitTransacctionStatement(String sentencia) throws ParseException, TokenMgrError {
    	int endOfStatementChar = sentencia.toLowerCase().indexOf(";");
    	if (endOfStatementChar == -1) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	sentencia = sentencia.substring(0, endOfStatementChar).trim();
    	
        //me fijo que esten las palabras clave
    	if (!sentencia.equalsIgnoreCase("commit transaction")) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	
    	ZTransactStmt st = new ZTransactStmt("COMMIT");
        st.setComment("COMMIT");
        System.out.println(st.getComment());
        
        return st;
        
    }
    /**************************************************************
     * Esta funcion realiza el parseo de la sentencia ABORT
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @see ZStatement
     ***************************************************************/ 
    public static ZStatement parseAbortTransacctionStatement(String sentencia) throws ParseException, TokenMgrError {
    	int endOfStatementChar = sentencia.toLowerCase().indexOf(";");
    	if (endOfStatementChar == -1) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	sentencia = sentencia.substring(0, endOfStatementChar).trim();
    	
    	String[] partesDeSentencia = sentencia.split(" ");
    	if (!partesDeSentencia[0].toLowerCase().equals("rollback")) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	if (partesDeSentencia.length > 2) {
    		throw new ParseException("Parser TXL: SQL string not recognized");
    	}
    	String savepoint = partesDeSentencia[1]; 
    	
    	ZTransactStmt st = new ZTransactStmt("SAVEPOINT");
    	if (savepoint.equalsIgnoreCase("transaction")) {
    		st.setComment("ROLLBACK");
    	} else {
            st.setComment("ROLLBACK " + savepoint);
    	}
    	
        System.out.println(st.getComment());
        return st;
     
    }       

}
