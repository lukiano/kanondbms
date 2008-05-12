package servidor.parser.impl;

import java.io.ByteArrayInputStream;

import servidor.excepciones.ParseException;
import servidor.parser.Parser;
import Zql.TokenMgrError;
import Zql.ZDelete;
import Zql.ZInsert;
import Zql.ZQuery;
import Zql.ZStatement;
import Zql.ZUpdate;
import Zql.ZqlParser;

/**
 * @author Julian R Berlin
 *
 */

class DMLParser implements Parser {
	
	/**
	 * 
	 */
	public DMLParser() {
		super();
	}

    /**********************************************************
     * Esta funcion realiza el parseo de la sentencia INSERT 
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @see ZStatement
     ***********************************************************/
    public static ZStatement parseInsertStatement(String sentencia) throws ParseException { 
    	
        ZqlParser p = new ZqlParser();
        ZInsert ret = null;
        p.initParser(new ByteArrayInputStream(sentencia.getBytes()));
        try {
            ret =(ZInsert) p.readStatement();
        }
        catch (TokenMgrError e) {
            throw new ParseException("La sentencia se encuentra mal formada.", e);
        } catch (Zql.ParseException e) {
        	throw new ParseException("La sentencia se encuentra mal formada.", e);
		}
//      testeo de la sentencia....
        System.out.println("COLUMNAS PART:"+ ret.getColumns());
        System.out.println("VALUES PART:" + ret.getValues());
        System.out.println("TABLE PART:" + ret.getTable());
        
        return ret;
    }   
    
    
    /**************************************************************
     * Esta funcion realiza el parseo de la sentencia SELECT 
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @see ZStatement
     ***************************************************************/
    public static ZStatement parseSelectStatement(String sentencia) throws ParseException, TokenMgrError {
        ZqlParser p = new ZqlParser();
        p.initParser(new ByteArrayInputStream(sentencia.getBytes()));
        ZQuery st = null;
        try {
            try {
                st = (ZQuery) p.readStatement();
            } catch (Zql.ParseException e) {
            	throw new ParseException("Parser DML: Cadena no reconocida", e);
            }
        }
        catch (TokenMgrError e) {
            throw new ParseException("Parser DML: Cadena no reconocida", e);
        }
        
        // Agrego la verdadera parte del SELECT obtenida anteriormente.
        //st.addSelect(selectPartItems);
        
        //testeo de la sentencia....
        System.out.println("SELECT PART"+st.getSelect());
        System.out.println("FROM PART" + st.getFrom());
        System.out.println("WHERE PART" +st.getWhere());
        return st;
    }
    
    /**************************************************************
     * Esta funcion realiza el parseo de la sentencia UPDATE 
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @throws Zql.ParseException 
     * @see ZStatement
     ***************************************************************/
   
    public static ZStatement parseUpdateStatement(String sentencia) throws ParseException {
        // Si no tiene un SELECT anidado, entoces el parseo lo realiza el ZQL.
        ZqlParser p = new ZqlParser();
        p.initParser(new ByteArrayInputStream(sentencia.getBytes()));
        ZStatement st          = null ;
        try {
            st = p.readStatement();
        }
        catch (TokenMgrError e) {
            throw new ParseException("Parser DML: Cadena no reconocida", e);
        } catch (Zql.ParseException e) {
        	throw new ParseException("Parser DML: Cadena no reconocida", e);
		}
        //solo para testeo..
        ZUpdate st2=(ZUpdate)st;
        System.out.println("SET PART: "+st2.getSet());
        System.out.println("TABLE PART: "+st2.getTable());
        System.out.println("WHERE PART: "+st2.getWhere());
               
   
        return st;
    }

    /**************************************************************
     * Esta funcion realiza el parseo de la sentencia DELETE FROM TABLE
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @throws Zql.ParseException 
     * @see ZStatement
     ***************************************************************/
    public static ZStatement parseDeleteStatement(String sentencia) throws ParseException {
        //hago el parseo con el ZQL
        ZqlParser p = new ZqlParser();
        p.initParser(new ByteArrayInputStream(sentencia.getBytes()));
        ZDelete st          = null ;
        try {
            st =(ZDelete) p.readStatement();
        }
        catch (TokenMgrError e) {
            throw new ParseException("Parser DML: Cadena no reconocida", e);
        } catch (Zql.ParseException e) {
        	throw new ParseException("Parser DML: Cadena no reconocida", e);
		}
        
        //testeo de la sentencia....
        System.out.println("FROM PART: " + st.getTable());
        System.out.println("WHERE PART: " +st.getWhere());
       
        return st;
}
    
    public static void main(String args[]) throws ParseException {
        //CreadorCatalogo.makeCreation("Tablas//", ".db");
        //BufferManager.initBufferManager(1024);
        //String query = "drop table pepe;";
        String query = "UPDATE ANTIQUES SET PRICE = 500.00 WHERE ITEM = 'Chair';" ;
       /* String query = "Create table pepeTable (pepe CHAR(20) NULL, juean NUMERIC(10) NOT_NULL," +
                "pedro CHAR(254) NULL,primary key (pepe));";*/
        System.out.println(query);
        System.out.println(" ");
        System.out.println("Luego del parser....");
        System.out.println(" ");
        // parseCommitTransacctionStatement(query);
        //parseAbortTransacctionStatement(query);
        //parseBeginTransacctionStatement(query);
        //parseSelectStatement(query);
        //parseInsertStatement(query);
        parseUpdateStatement(query);//-->OK
//        ZStatement st=null;
        /*try {
            st = MainParser.initParser(query);
            System.out.println(st);
        } catch (Zql.ParseException e) {
            e.printStackTrace();
        } catch (TypeNotSupportedException e) {
            e.printStackTrace();
        } catch (PKFieldsMustBeNotNullException e) {
            e.printStackTrace();
        }*/
        /*try {
            BufferManager.stopBufferManager();
        } catch (IOException e1) {
            e1.printStackTrace();
        }*/
    }



}
