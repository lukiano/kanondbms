package servidor.parser.impl;

import java.util.StringTokenizer;

import servidor.excepciones.ParseException;
import Zql.TokenMgrError;
import Zql.ZStatement;

/**
 * @author Julian R Berlin
 */
public class MainParser {
	
	/**
	 * Esta funcion se encarga de realizar el analisis Lexico y Sintactico de una sentencia
	 * 
	 * @param sentencia - Representa la sentencia sobre la que se ejecutara el Parseo
	 * @return Un ZStatement obtenido a partir de la sentencia
	 * @throws ParseException
	 * @see ZStatement
	 */
    
	public static ZStatement initParser(String sentencia) throws ParseException { 
																
		ZStatement ret = null;
        //para manejar las palabras de la cadena
		StringTokenizer toParseSentence = new StringTokenizer(sentencia);
                
		// Verifico de que tipo de sentencia se trata y ejecuto el parser correspondiente
		if (toParseSentence.hasMoreTokens()) {
			String first = toParseSentence.nextToken();
			first = first.toUpperCase();
			if (first.startsWith("SELECT")) {
				ret = DMLParser.parseSelectStatement(sentencia);
			}
			else if (first.startsWith("INSERT")) {
				ret = DMLParser.parseInsertStatement(sentencia);
			}
            else if (first.startsWith("UPDATE")) {
                ret = DMLParser.parseUpdateStatement(sentencia);
            }
            else if (first.startsWith("DELETE")) {
                ret = DMLParser.parseDeleteStatement(sentencia);
            }
            else if (first.startsWith("BEGIN")) {
                ret = TransaccionParser.parseBeginTransacctionStatement(sentencia);
            }
            else if (first.startsWith("SAVEPOINT")) {
                ret = TransaccionParser.parseSavepointStatement(sentencia);
            }
            else if (first.startsWith("ISOLATION")) {
                ret = TransaccionParser.parseIsolationStatement(sentencia);
            }
            else if (first.startsWith("COMMIT")) {
                ret = TransaccionParser.parseCommitTransacctionStatement(sentencia);
            }
            else if (first.startsWith("ROLLBACK")) {
                ret = TransaccionParser.parseAbortTransacctionStatement(sentencia);
            }
			else if ((first.startsWith("CREATE"))) {
				ret = DDLParser.parseCreateTableStatement(sentencia);
			}
			else if ((first.startsWith("DROP"))) {
				ret =DDLParser.parseDropTableStatement(sentencia);
			}
			else if ((first.startsWith("CHECKPOINT"))) {
				ret =SistemaParser.parseCheckpointStatement(sentencia);
			}
			else if ((first.startsWith("CRASH"))) {
				ret =SistemaParser.parseCrashStatement(sentencia);
			}
			else {
				throw new ParseException("SQL string not recognized.");
			}
		}
		else {
			// no hay tokens. Son espacios. Se ignoran.
		}
		return ret;
	}
	

	/** ************************************************* */
	/* CODIGO PARA TESTEO */
	/**
	 * @throws TokenMgrError 
	 * @throws ParseException  ************************************************* 
	 * @throws ColumnNotFoundException 
	 * @throws FieldLengthExceededException 
	 * @throws WhereErrorFoundException 
	 * @throws SelectErrorFoundException 
	 * @throws FromErrorFoundException 
	 * @throws InsertFieldsOutOfBoundException 
	 * @throws InsertTypeMissMatchException 
	 * @throws InsertUnknowTypeException 
	 * @throws InsertNULLNotAllowedException 
	 * @throws InsertTableNotFoundException 
	 * @throws PermissionDeniedException 
	 * @throws TableNameNotMatchException 
	 * @throws DuplicatedFieldNameDefinitionException 
	 * @throws PKNotMemberOfTableException 
	 * @throws TableAlreadyExistException 
	 * @throws IndexNameAlreadyExistException 
	 * @throws FieldNotFoundException 
	 * @throws IndexStructureNotSupportedException */

	public static void main(String args[]) throws ParseException, TokenMgrError {
		//para testear el parser
        //CreadorCatalogo.makeCreation("Tablas//", ".db");
		//BufferManager.initBufferManager(1024);
		
		//String query = "select nombre,edad from pepe where pepe.id='5';";
        //String query = "insert into pepe values( 'boludo' , 2 , '12/02/2003' );";
        //con columnas
        //String query ="INSERT INTO ANTIQUES (BUYERID, SELLERID, ITEM) VALUES (01, 21, 'Ottoman');";
        //String query ="UPDATE ANTIQUES SET PRICE = 500.00 WHERE ITEM = 'Chair';";
        //String query = "DELETE FROM ANTIQUES WHERE ITEM = 'Ottoman' AND BUYERID = 01 AND SELLERID = 21;";
		String query = "Create table cliente (nombre CHAR(20) NULL, dni NUMERIC(10) NOT NULL," +
				       "nombre CHAR(254) NULL,primary key (dni));";
        //String query = "Create user esteban , gilada ;";
	    //String query = "drop table pepe ;";
        //String query = "ROLLBACK TRANSACTION ; ";
        //String query = "COMMIT TRANSACTION ; ";
       // String query = "BEGIN TRANSACTION ; ";
		System.out.println(query);
		System.out.println(" ");
		System.out.println("Luego de la transformacion");
		System.out.println(" ");
		ZStatement st=null;
		st = MainParser.initParser(query);
		System.out.println(st);
		/*try {
			BufferManager.stopBufferManager();
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
	}

}
