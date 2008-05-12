package servidor.parser.impl;

import servidor.excepciones.ParseException;
import servidor.parser.Parser;
import Zql.TokenMgrError;
import Zql.ZStatement;

/**
 * @author Julian R Berlin
 *
 */

class SistemaParser implements Parser {
	
	/**
	 * 
	 */
	public SistemaParser() {
		super();
	}

    /**************************************************************
     * Esta funcion realiza el parseo de la sentencia CHECKPOINT
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @see ZStatement
     ***************************************************************/
    public static ZStatement parseCheckpointStatement(String sentencia) throws ParseException, TokenMgrError {
    	int endOfStatementChar = sentencia.toLowerCase().indexOf(";");
    	if (endOfStatementChar == -1) {
    		throw new ParseException("Parser SYS: Cadena no reconocida");
    	}
    	sentencia = sentencia.substring(0, endOfStatementChar).trim();
    	
    	if (sentencia.equalsIgnoreCase("checkpoint")) {
    		return new ZSystemStatement("CHECKPOINT");
    	} else {
    		throw new ParseException("Parser SYS: Cadena no reconocida");
    	}
    }

    /**************************************************************
     * Esta funcion realiza el parseo de la sentencia CRASH
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @see ZStatement
     ***************************************************************/
    public static ZStatement parseCrashStatement(String sentencia) throws ParseException, TokenMgrError {
    	int endOfStatementChar = sentencia.toLowerCase().indexOf(";");
    	if (endOfStatementChar == -1) {
    		throw new ParseException("Parser SYS: Cadena no reconocida");
    	}
    	sentencia = sentencia.substring(0, endOfStatementChar).trim();
    	if (sentencia.equalsIgnoreCase("crash")) {
    		return new ZSystemStatement("CRASH");
    	} else {
    		throw new ParseException("Parser SYS: Cadena no reconocida");
    	}
    }

}
