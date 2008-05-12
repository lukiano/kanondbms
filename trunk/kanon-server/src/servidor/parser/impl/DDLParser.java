/**
 * 
 */
package servidor.parser.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import servidor.catalog.tipo.Tipo;
import servidor.excepciones.ParseException;
import servidor.parser.Parser;
import servidor.tabla.Columna;
import servidor.tabla.impl.ColumnaImpl;
import Zql.ZStatement;

/**
 * @author Julian R Berlin
 *
 */
class DDLParser implements Parser {

	/**
	 * 
	 */
	public DDLParser() {
		super();
	}

    /**************************************************************
     * Esta funcion realiza el parseo de la sentencia CREATE TABLE 
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @throws TypeNotSupportedException
     * @see ZStatement
     **************************************************************/
    public static ZStatement parseCreateTableStatement(String sentencia) 
                            throws ParseException {
        
        
        int createTablePos      = sentencia.toLowerCase().indexOf("create table ");
        int parentesisAbrePos   = sentencia.indexOf("(");
        int parentesisCierraPos = sentencia.lastIndexOf(")");
        int primaryKeyPos       = sentencia.toLowerCase().indexOf("primary key");
        
        List<Columna> cols = new ArrayList<Columna>();
        List<String> pks = null;

        if (createTablePos == -1 || parentesisAbrePos == -1 || parentesisAbrePos < createTablePos) {
            throw new ParseException("La sentencia se encuentra mal formada.");
        }
        // Obtengo el nombre de la tabla
        String tableName = sentencia.substring(createTablePos + "create table ".length(), parentesisAbrePos).trim();
        
        // Obtengo la definicion de las columnas de la tabla
        if (parentesisCierraPos == -1 || parentesisCierraPos < parentesisAbrePos) {
            throw new ParseException("La sentencia se encuentra mal formada.");
        }
        // Si hay una definicion de primary Key, entonces tiene que estar en la ultima posicion del arreglo.
        String restDefinition = sentencia;
        if (primaryKeyPos != -1) {
            String pkDefinition = sentencia.substring(primaryKeyPos, sentencia.lastIndexOf(")"));
            pkDefinition = pkDefinition.substring(0, pkDefinition.indexOf(")")+1);
            pks = getPKsFromString(pkDefinition.trim());
            
            restDefinition = sentencia.substring(0, primaryKeyPos);
            restDefinition = restDefinition.substring(0, restDefinition.lastIndexOf(",")) + ")";
            parentesisCierraPos = restDefinition.lastIndexOf(")"); 
        }

        String[] dataDefinition = restDefinition.substring(parentesisAbrePos + 1, parentesisCierraPos).split(",");
        // Por cada posicion del arreglo de la definicion, creo una nueva columna, si y solo si, no es
        // la definicion de la primary Key
        for (int x=0; x < dataDefinition.length; ++x) {
            if (dataDefinition[x].toLowerCase().indexOf("primary key") == -1) {
				Columna col = getColumnFromString(tableName, dataDefinition[x].trim(), x+1, pks);
                cols.add(col);
            }
        }
        
        ZCreateTable st = new ZCreateTable(tableName, cols, pks);
        
        //testeo del create table
        System.out.println("NOMBRE TABLA: "+st.getTableName());
        System.out.println("COLUMNAS: "+st.getColumnas().toString());
        System.out.println("PK: " + st.getPKs());
        return st;//new ZCreateTable(tableName, cols, pks);
    }
    
    /****************************************************************
     * Esta funcion realiza el parseo de la sentencia DROP TABLE 
     * 
     * @param sentencia - Un string que representa la sentencia a parsear
     * @return Un ZStatement obtenido a partir de la sentencia
     * @throws ParseException
     * @see ZStatement
     ****************************************************************/
    public static ZStatement parseDropTableStatement(String sentencia) throws ParseException {
        int tablePos = sentencia.toLowerCase().indexOf(" table ");
        int endCharPos = sentencia.indexOf(";");
        
        if (tablePos == -1 || endCharPos == -1 || endCharPos < tablePos) {
            throw new ParseException("La sentencia se encuentra mal formada.");
        }
        // Tomo el nombre de la tabla
        String tableName = sentencia.substring(tablePos + " table ".length(), endCharPos).trim();
        
        ZDropTable dropTable = new ZDropTable(tableName);
        
        //testeo
        System.out.println("NOMBRE TABLA: "+dropTable.getTableName() );
        
        return dropTable;
    }
    
    /************************************************************
     * Recibe un string de la forma "Primary Key (campo1, campo2,...,campon)" y devuelve
     * un vector con los nombres de los campos que pertenecen a la PK
     ***********************************************************/
    private static List<String> getPKsFromString(String PKs) throws ParseException {
    	List<String> ret = new ArrayList<String>();
        int parentesisAbrePos = PKs.indexOf("(");
        int parentesisCierraPos = PKs.lastIndexOf(")");
        
        if (parentesisAbrePos == -1 || parentesisCierraPos == -1 || parentesisCierraPos < parentesisAbrePos) {
            throw new ParseException("La sentencia se encuentra mal formada.");
        }
        String[] campos = PKs.substring(parentesisAbrePos+1, parentesisCierraPos).split(",");
        for (int x = 0; x < campos.length; ++x) {
            ret.add(campos[x].trim());
        }
        return ret;
    }
    
    /******************************************************
     * Recibe el nombre de la table, un string de la forma "Nombre_Columna CHAR(20) [NULL|NOT_NULL]" y
     * el orden y devuelve una columna armada con los datos de dicho string
     *******************************************************/
    private static Columna getColumnFromString(String tableName, String columna, int orden, Collection<String> PKs) 
                            throws ParseException {
        Columna ret = null;
        StringTokenizer tokensCol = new StringTokenizer(columna);
        try {
            //El primer token es el nombre de la columna
            String fieldName = tokensCol.nextToken();
            boolean isPK = false;
            if (PKs != null) {
                isPK = PKs.contains(fieldName);
            }
            
            //El segundo es el tipo que tiene que ser de la forma TIPO(tamaño)
            String tipoString       = tokensCol.nextToken();
            int parentesisAbrePos   = tipoString.indexOf("(");
            int parentesisCierraPos = tipoString.lastIndexOf(")");
            
            if (parentesisAbrePos == -1 || parentesisCierraPos == -1 || parentesisCierraPos < parentesisAbrePos) {
                throw new ParseException("La sentencia se encuentra mal formada.");
            }
            String tipo = tipoString.substring(0, parentesisAbrePos);
            int length = 0;
            try {
                length = Integer.parseInt(tipoString.substring(parentesisAbrePos+1, parentesisCierraPos).trim());
            }
            catch (NumberFormatException e) {
                throw new ParseException("La sentencia se encuentra mal formada.");
            }
            //luego hacer q funke estas lineas
            Tipo tipoDato = null ;//=  new Tipo;
            //me fijo de que tipo se trata
            if(tipo.toUpperCase().trim().equalsIgnoreCase("CHAR"))
            {
                 tipoDato = Tipo.dameTipo(String.class);
                 
            } else if ( tipo.toUpperCase().trim().equalsIgnoreCase("NUMERIC"))
            {
                 tipoDato = Tipo.dameTipo(Integer.class);
                
            }
                
            /*Tipo tipoDato = CatalogoManager.getTipoDeDato(tipo);
            if (tipoDato == null) {
                throw new TypeNotSupportedException();
            }*/
            
            List<String> constraint = new ArrayList<String>();
            //El siguiente token es la contraint de NULL o NOT_NULL, este es opcional
            if (tokensCol.hasMoreTokens()) {
                String c = tokensCol.nextToken().trim();
                // Si la siguiente palabra es NOT, entonces tiene que seguir la palabra NULL
                // sino es un error sintactico
                if (c.equalsIgnoreCase("NOT")) {
                    if (tokensCol.hasMoreTokens()) {
                        String c2 = tokensCol.nextToken().trim();
                        if (c2.equalsIgnoreCase("NULL")) {
                            constraint.add("NOT_NULL");
                        }
                        else {
                            throw new ParseException("La sentencia se encuentra mal formada.");
                        }
                    }
                    else {
                        throw new ParseException("La sentencia se encuentra mal formada.");
                    }
                }
                else {
                    // Si no es un NOT, entonces si es distinto de NULL, hay un error sintactico
                    if (!c.equalsIgnoreCase("NULL")) {
                        throw new ParseException("La sentencia se encuentra mal formada.");
                    }
                    else {
                        // Si es igual a NULL y el campo era PK, entonces hay un Error
                        if (isPK) {
                            throw new RuntimeException("Las claves primarias no se encuentran soportadas.");
                        }
                    }
                }
            }
            else {
                // Si no tenia definida un constraint sobre NULL/NOT_NULL y el campo
                // pertenece a las Primary Key, entonces lo fuerzo a que sea NOT_NULL
                if (isPK) {
                    constraint.add("NOT_NULL");
                }
            }
            if (isPK) {
                constraint.add("PK");
            }
            if (constraint.size() > 0) {
                //ColumnaImp(String nombre, int longitud, int orden, Tipo tipo)
                ret = new ColumnaImpl(fieldName,length, orden, tipoDato, constraint);
            }
            else {
                ret = new ColumnaImpl(fieldName,length, orden, tipoDato);
            }
        }
        catch (NoSuchElementException e) {
            throw new ParseException("La sentencia se encuentra mal formada.", e);
        }
        return ret;
    }


}
