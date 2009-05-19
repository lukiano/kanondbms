package servidor.ejecutor.xql;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import servidor.catalog.Catalogo;
import servidor.catalog.FabricaCatalogo;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.ejecutor.Resultado;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.util.Iterador;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZInsert;
import Zql.ZQuery;
import Zql.ZStatement;

/**
 * @date 21/11/2005
 */
public class XInsert implements XStatement {

    /**
     * El nombre de la tabla donde se insertaran los registros.
     */
    private String nombreTabla;
    
    /**
     * La subconsulta cuyos resultados seran insertados en la tabla.
     */
    private ZQuery subQuery;
    
    /**
     * Los valores (expresiones o constantes) tenidos en cuenta si se inserta un solo registro.
     */
    private Vector<ZExp> valoresInsercion;
    
    /**
     * las columnas involucradas en la insercion (como no se soporta null, deben ser todas, pero pueden tener otro orden).
     */
    private Vector<String> columnas;

    /**
     * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
     */
    @SuppressWarnings("unchecked")
	public void zqlToXql(ZStatement st) {
        ZInsert zql = (ZInsert) st;
        this.nombreTabla = zql.getTable();
        
        this.valoresInsercion = zql.getValues();
        this.columnas = zql.getColumns();
        if (this.valoresInsercion == null) {
            this.subQuery = zql.getQuery();
        }
    }

    
    /**
     * @see servidor.ejecutor.xql.XStatement#execute()
     */
    public Resultado execute() {
        Catalogo catalogo = FabricaCatalogo.dameInstancia();
        Tabla tabla = catalogo.dameTabla(this.nombreTabla);
        if (tabla == null) {
        	throw new RuntimeException("No table exists with name '" + this.nombreTabla + "'.");
        }
        if (this.valoresInsercion == null) {
            return manejarSubquery(tabla);
        } else {
            return manejarFilaUnica(tabla);
        }

    }

	/**
	 * Metodo cuando se inserta un unico registro en la tabla.
	 * @param tabla la tabla donde se insertara el registro.
	 * @return el resultado de la insercion.
	 */
	private Resultado manejarFilaUnica(Tabla tabla) {
		// solo una fila de valores
		Resultado rto = new Resultado();
		
		List<Valor> valores = new ArrayList<Valor>();

		Columna[] columnas = tabla.columnas();
		BitSet columnasUsadas = new BitSet(columnas.length);
		columnasUsadas.set(0, columnas.length);
		Conversor conversor = Conversor.conversorDeTexto();
		for (int i = 0; i < valoresInsercion.size(); i++) {
			ZExp value = valoresInsercion.get(i);
			if (value instanceof ZQuery) {
				throw new RuntimeException("INSERT subqueries are not supported: " + value);
			}
			if (value instanceof ZExpression) {
				value = ZQLEvalHelper.convertirExpresionAConstante((ZExpression)value);
				// aqui se convirtio la expresion en una constante
			}
		    if (value instanceof ZConstant) {
		        // es una constante
		        ZConstant constant = (ZConstant) value;
		        Campo campo = CampoHelper.dameCampo(constant);
		        if (this.columnas == null) {
		        	// no se mencionan las columnas
		        	if (columnasUsadas.isEmpty()) {
		        		throw new RuntimeException("Wrong number of columns and values in INSERT statement.");
		        	}
		            if (!CampoHelper.camposDelMismoTipo(columnas[i].campo(), campo)) {
		            	throw new RuntimeException("Se quiere insertar un elemento del tipo " + campo.toString()
		            			+ " en la columna " + columnas[i].nombre());
		            }
		            Object contenido = conversor.convertir(columnas[i].campo(), constant.getValue());
		            valores.add(Valor.nuevoValor(i, columnas[i].campo(), contenido) );
		            if (!columnasUsadas.get(i)) {
		            	throw new RuntimeException("Wrong number of columns and values in INSERT statement.");
		            }
		            columnasUsadas.clear(i);
		        } else {
		        	if (i < this.columnas.size() && this.columnas.get(i) != null) {
		        		String nombreColumna = this.columnas.get(i);
		        		boolean encontrado = false;
		        		for (int j = 0; j < columnas.length; j++) {
		        			if (nombreColumna.equals(columnas[j].nombre())) {
		                        if (!CampoHelper.camposDelMismoTipo(columnas[j].campo(), campo)) {
		                        	throw new RuntimeException("Unable to insert an element of type '" + campo.toString()
		                        			+ "' in column " + columnas[j].nombre());
		                        }
		                        Object contenido = conversor.convertir(columnas[j].campo(), constant.getValue());
		                        valores.add(Valor.nuevoValor(j, columnas[j].campo(), contenido) );
		    		            if (!columnasUsadas.get(j)) {
		    		            	throw new RuntimeException("Wrong number of columns and values in INSERT statement.");
		    		            }
		                        columnasUsadas.clear(j);
		                        encontrado = true;
		                        break;
		        			}
		        		}
		        		if (!encontrado) {
		        			throw new RuntimeException("Column '" + nombreColumna + "' not found in table '" + this.nombreTabla + "'.");
		        		}
		        	} else {
		        		throw new RuntimeException("Wrong number of columns and values in INSERT statement.");
		        	}
		        }
		    } else {
		        // es una expresion
		        throw new RuntimeException("La expresion en el INSERT no esta soportadas: " + value);
		    }
		}
		if (!columnasUsadas.isEmpty()) {
			throw new RuntimeException("Wrong number of columns and values in INSERT statement.");
		}
		tabla.insertarRegistro(valores);
		rto.setMensaje("Successful insertion of 1 record " +
		               "to table " + this.nombreTabla);
		return rto;
	}

	/**
	 * Metodo cuando se insertan varios registros en la tabla (el resultado de una subconsulta).
	 * @param tablaAInsertar la tabla donde se insertara el registro.
	 * @return el resultado de la insercion.
	 */
	private Resultado manejarSubquery(Tabla tablaAInsertar) {
		Resultado rto = new Resultado();
		// es una subquery
		
		Map<Integer, Integer> conversionColumnas = null;
		if (this.columnas == null) {
			conversionColumnas = obtenerConversionDirecta(tablaAInsertar);
		} else {
			conversionColumnas = obtenerConversionColumnas(tablaAInsertar);
		}
		
		XQuery xquery = new XQuery();
		xquery.zqlToXql(this.subQuery);
		Resultado resultadoQuery = xquery.execute();
		Tabla tablaResultado = resultadoQuery.getTabla();
		this.comprobarTipos(tablaResultado, tablaAInsertar, conversionColumnas);
		Iterador<Registro.ID> iterador = tablaResultado.registros();
		int cantidad = 0;
		Columna[] columnas = tablaAInsertar.columnas();
		try {
		    while (iterador.hayProximo()) {
		        Registro.ID idRegistro = iterador.proximo();
		        Collection<Valor> valoresAInsertar = new ArrayList<Valor>();
		        Registro registro = tablaResultado.registro(idRegistro);
		        try {
		        	for (int i = 0; i < conversionColumnas.size(); i++) {
		        		int origenI = conversionColumnas.get(i);
		        		Object contenido = registro.valor(origenI);
		        		Campo campo = columnas[i].campo();
		        		valoresAInsertar.add(Valor.nuevoValor(i, campo, contenido));
		        	}
		        } finally {
		        	tablaResultado.liberarRegistro(idRegistro);
		        }
		        tablaAInsertar.insertarRegistro(valoresAInsertar);
		        cantidad++;
		    }
		    rto.setMensaje("Successful insertion of " + cantidad + " records " +
		                   "to table " + this.nombreTabla);
		} finally {
		    iterador.cerrar();
		}
		return rto;
	}

	/**
	 * Obtiene las posiciones de columnas correspondientes cuando hay un reordenamiento de las mismas.
	 * @param tabla la tabla involucrada.
	 * @return un mapa de posicion a posicion con el reordenamiento de las columnas segun lo especificado en el INSERT.
	 */
	private Map<Integer, Integer> obtenerConversionColumnas(Tabla tabla) {
		Map<Integer, Integer> conversionColumnas = new HashMap<Integer, Integer>();
		Columna[] columnas = tabla.columnas();
		for (int i = 0; i < this.columnas.size(); i++) {
			String nombreColumna = this.columnas.get(i);
			boolean encontrado = false;
			for (int j = 0; j < columnas.length; j++) {
				if (nombreColumna.equals(columnas[j].nombre())) {
					encontrado = true;
					if (conversionColumnas.containsKey(j)) {
						throw new RuntimeException("Wrong number of columns and values in INSERT statement.");
					}
					conversionColumnas.put(j, i);
					break;
				}
			}
			if (!encontrado) {
				throw new RuntimeException("Column '" + nombreColumna + "' not found in table '" + this.nombreTabla + "'.");
			}
		}
		return conversionColumnas;
	}

	/**
	 * Obtiene las posiciones de columnas correspondientes cuando no hay un reordenamiento de las mismas.
	 * @param tabla la tabla involucrada.
	 * @return un mapa de posicion a posicion del tipo igualdas con las posiciones de las columnas.
	 */
	private Map<Integer, Integer> obtenerConversionDirecta(Tabla tabla) {
		Map<Integer, Integer> conversionColumnas = new HashMap<Integer, Integer>();
		Columna[] columnas = tabla.columnas();
		for (int i = 0; i < columnas.length; i++) {
			conversionColumnas.put(i, i);
		}
		return conversionColumnas;
	}
	
    /**
     * Comprueba compatibilidad entre tipos de las columnas si se insertan registros en una tabla desde otra (tabla resultante de la subconsulta).
     * @param tablaOriginal la tabla resultado de la subconsulta.
     * @param tablaDestino la tabla donde se van a insertar los registros del resultado.
     * @param conversionColumnas mapa con el reordenamiento de las columnas si llega a haber uno.
     */
    private void comprobarTipos(Tabla tablaOriginal, Tabla tablaDestino, Map<Integer, Integer> conversionColumnas) {
    	Columna[] columnasTablaOriginal = tablaOriginal.columnas();
    	Columna[] columnasTablaDestino = tablaDestino.columnas();
    	if (conversionColumnas.keySet().size() != columnasTablaDestino.length) {
    		throw new RuntimeException("Subquery return types don't match with table column types.");
    	}
    	for (int i = 0; i < columnasTablaDestino.length; i++) {
    		if (!CampoHelper.camposDelMismoTipo(columnasTablaOriginal[conversionColumnas.get(i)].campo(), columnasTablaDestino[i].campo())) {
    			throw new RuntimeException("Subquery return types don't match with table column types."); 
    		}
    	}
	}

    /**
     * @see servidor.ejecutor.xql.XStatement#necesitaTransaccion()
     */
    public boolean necesitaTransaccion() {
        return true;
    }

}
