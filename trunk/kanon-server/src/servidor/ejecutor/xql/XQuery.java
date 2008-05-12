package servidor.ejecutor.xql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import servidor.catalog.FabricaCatalogo;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.ejecutor.Resultado;
import servidor.ejecutor.xql.IteradorConsulta.Comando;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.tabla.impl.ColumnaImpl;
import servidor.tabla.impl.TablaEnMemoria;
import Zql.ZConstant;
import Zql.ZEval;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZStatement;
import Zql.ZTuple;

/**
 * XQL que realiza la consulta a una tabla, pues no se soporta joins (sentencia SELECT)
 */
public class XQuery implements XStatement {
    
    /**
     * El nombre de la tabla que va a ser consultada.
     */
    private String nombreTabla;
    
    /**
     * La expresion WHERE de la sentencia.
     */
    private ZExp where;
    
    /**
     * Expresiones de la proyeccion del SELECT.
     */
    private List<ZExp> proyeccion;
    
    /**
     * Indica si se desean ver todas las columnas '*';
     */
    private boolean wildcard;
    
    /**
     * El iterador usado para recorrer la tabla.
     */
    private IteradorConsulta iteradorConsulta = new IteradorConsultaConIndices();
    
    /**
     * Comando que lee los datos del registro de la tabla y los inserta en la tabla resultado, tomando en cuenta la proyeccion.
     */
    private class QueryComando implements Comando {
    	
    	/**
    	 * tabla con el resultado de la consulta. Sus columnas se corresponden con la proyeccion establecida.
    	 */
    	private Tabla tablaResultado;
    	
    	/**
    	 * Constructor de la clase.
    	 * Crea la tabla resultado tomando en cuenta la proyeccion de la sentencia para las columnas.
    	 * @param tabla la tabla a ser consultada.
    	 */
    	public QueryComando(Tabla tabla) {
            // crear las columnas a partir de la proyeccion
            Columna[] columnas = tabla.columnas();
            Columna[] columnasResultado;
            if (wildcard) {
                //todos
                columnasResultado = columnas;
            } else {
            	columnasResultado = new Columna[proyeccion.size()];
            	for (int i = 0; i < proyeccion.size(); i++) {
            		ZExp iesimo = proyeccion.get(i);
            		String nombreCol = proyeccion.get(i).toString();
            		if (iesimo instanceof ZExpression) {
            			ZTuple tuple = ZQLEvalHelper.construirTupla(columnas);
            			iesimo = ZQLEvalHelper.convertirExpresionAConstante((ZExpression)iesimo, tuple);
            		}
            		if (iesimo instanceof ZConstant) {
            			ZConstant constant = (ZConstant)iesimo;
            			if (constant.getType() == ZConstant.COLUMNNAME) {
                            boolean columnaEncontrada = false;
                            for (int j = 0; j < columnas.length; j++) {
                                Columna col = columnas[j];
                                if (nombreCol.equals(col.nombre())) {
                                    columnasResultado[i] = col;
                                    columnaEncontrada = true;
                                    break;
                                }
                            }
                            if (!columnaEncontrada) {
                            	throw new RuntimeException("La columna '" + nombreCol + "' no existe en la tabla '" + tabla.id().nombre() + "'.");
                            }
            			} else {
                    		columnasResultado[i] = new ColumnaImpl(nombreCol, CampoHelper.dameCampo(constant), i);	
            			}
            		} else {
            			throw new RuntimeException("Imposible determinar el campo para la proyeccion de '" + nombreCol + "'.");
            		}
            	}
            }

            Tabla.ID idTablaResultado = Tabla.ID.nuevoID("resultado", Integer.MAX_VALUE);
            this.tablaResultado = 
                new TablaEnMemoria(idTablaResultado, columnasResultado);
    		
    	}

		/**
		 * @see servidor.ejecutor.xql.IteradorConsulta.Comando#ejecutarAccion(servidor.tabla.Tabla, servidor.tabla.Registro, Zql.ZTuple)
		 */
		public void ejecutarAccion(Tabla tabla, Registro registro, ZTuple tuple) {
    		if (wildcard) {
    			this.tablaResultado.insertarRegistro(registro.getValores());
    		} else {
        		ZEval eval = new ZEval();
            	Collection<Valor> valores = new ArrayList<Valor>(proyeccion.size());
                for (int i = 0; i < proyeccion.size(); i++) {
    				try {
    					Object resultadoCrudo = eval.evalExpValue(tuple, proyeccion.get(i));
    					String resultado = ZQLEvalHelper.valorATexto(resultadoCrudo);
    					Campo campo = this.tablaResultado.columnas()[i].campo();
    					Object res = Conversor.conversorDeTexto().convertir(campo, resultado);
    	                valores.add(Valor.nuevoValor(i, campo, res));    
    				} catch (SQLException e) {
    					throw new RuntimeException(e);
    				}
                }
                this.tablaResultado.insertarRegistro(valores);
    		}
    	}
    	
    	/**
    	 * @return la tabla con el resultado de la consulta. Sus columnas se corresponden con la proyeccion establecida.
    	 */
    	public Tabla tablaResultado() {
    		return this.tablaResultado;
    	}

    }

    /**
     * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
     */
    @SuppressWarnings("unchecked")
	public void zqlToXql(ZStatement st) {
        ZQuery zql = (ZQuery) st;

        Vector<ZFromItem> fromVector = zql.getFrom();
        // no manejamos producto cartesiano (JOIN), suponemos que from tiene una sola tabla
        if (fromVector.size() != 1) {
            throw new RuntimeException("Cantidad de tablas no soportada en FROM: " + fromVector.size());
        }
        
        this.nombreTabla = fromVector.get(0).getTable();
        this.where = zql.getWhere();
        
        Vector<ZSelectItem> selectVector = zql.getSelect();
        this.proyeccion = new ArrayList<ZExp>(selectVector.size());
        Iterator<ZSelectItem> selectIterator = selectVector.iterator();
        while (selectIterator.hasNext()) {
            ZSelectItem selectItem = selectIterator.next();
            if (selectItem.getAggregate() != null) {
            	throw new RuntimeException("Las agregaciones en SELECT no se encuentran soportadas: " + selectItem.getAggregate());
            }
            if ("*".equals(selectItem.getColumn())) {
            	this.proyeccion.clear();
            	this.wildcard = true;
            	break;
            } else {
            	this.proyeccion.add(selectItem.getExpression());
            }
        }
    }
    
    /**
     * @see servidor.ejecutor.xql.XStatement#execute()
     */
    public Resultado execute() {
        Tabla tabla = FabricaCatalogo.dameInstancia().dameTabla(this.nombreTabla);
        if (tabla == null) {
            throw new RuntimeException("La tabla especificada no existe en el catalogo: " + this.nombreTabla);
        }
        
        QueryComando comando = new QueryComando(tabla);
        this.iteradorConsulta.ejecutarParaCadaCoincidencia(tabla, this.where, comando);

        Resultado resultado = new Resultado();
        resultado.setTabla(comando.tablaResultado());
        return resultado;
    }
    
	/**
     * @see servidor.ejecutor.xql.XStatement#necesitaTransaccion()
     */
    public boolean necesitaTransaccion() {
        return true;
    }

}
