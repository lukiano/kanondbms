package servidor.ejecutor.xql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import servidor.catalog.FabricaCatalogo;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.ejecutor.Resultado;
import servidor.ejecutor.xql.IteradorConsulta.Comando;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZStatement;
import Zql.ZTuple;
import Zql.ZUpdate;

/**
 * XQL que realiza la actualizacion de registros de una tabla (sentencia UPDATE).
 */
public class XUpdate implements XStatement {
	
	
	/**
	 * Estructura que contiene la expresion que va a usarse para actualizar una columna.
	 */
	private static class ColumnaAExpresion {
		
		/**
		 * El numero de la columna afectada.
		 */
		public Integer numeroColumna;
		
		/**
		 * El nombre de la columna afectada.
		 */
		public String nombreColumna;
		
		/**
		 * La expresion que va a usarse para actualizar la columna afectada.
		 */
		public ZExp expresion;
		
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() { return this.nombreColumna + "->" + this.expresion; }
	}

    /**
     * La expresion WHERE de la sentencia.
     */
    private ZExp where;
    
    /**
     * El nombre de la tabla cuyos registros van a ser actualizados.
     */
    private String nombreTabla;
    
    /**
     * Lista con las columnas afectadas en la actualizacion.
     */
    private List<ColumnaAExpresion> entradas;

    /**
     * El iterador usado para recorrer la tabla.
     */
    private IteradorConsulta iteradorConsulta = new IteradorConsultaConIndices();
    
    /**
     * Comando que realiza la actualizacion de un registro.
     */
    private class UpdateComando implements Comando {
    	
    	/**
    	 * variable la cantidad de registros que fueron actualizados.
    	 */
    	private int cantidad = 0;
    	
    	/**
    	 * @see servidor.ejecutor.xql.IteradorConsulta.Comando#ejecutarAccion(servidor.tabla.Tabla, servidor.tabla.Registro, Zql.ZTuple)
    	 */
    	public void ejecutarAccion(Tabla tabla, Registro registro, ZTuple tuple) {
    		Columna[] columnas = tabla.columnas();
    		
            Conversor conversor = Conversor.conversorDeTexto();
        	Collection<Valor> valores = new ArrayList<Valor>(entradas.size());
            for (ColumnaAExpresion entrada : entradas) {
            	// se evalua la tupla segun la expresion de la columna y se genera un nuevo valor para la entrada del registro en esa columna.
            	Campo campo;
            	String nuevoValor;
            	ZExp exp = entrada.expresion;
                if (exp instanceof ZExpression) {
                	// es una expresion
                	exp = ZQLEvalHelper.convertirExpresionAConstante((ZExpression)exp, tuple);
                }
                if (exp instanceof ZConstant) {
                	// es una constante
                    ZConstant constant = (ZConstant) exp;
                    campo = CampoHelper.dameCampo(constant);
                    nuevoValor = constant.getValue();
                } else {
                	throw new RuntimeException("Expression not supported: " + exp);
                }
                Columna columnaCorrespondiente = columnas[entrada.numeroColumna];
                if (!CampoHelper.camposDelMismoTipo(campo, columnaCorrespondiente.campo())) {
                	throw new RuntimeException("Unable to insert an element of type " + campo.toString()
                			+ " in column " + columnaCorrespondiente.nombre());
                }
                valores.add(
                		Valor.nuevoValor(entrada.numeroColumna, 
                				columnaCorrespondiente.campo(), 
                				conversor.convertir(columnaCorrespondiente.campo(), nuevoValor)
                				)
                			);
            }
            tabla.actualizarRegistro(registro.id(), valores);
            cantidad++;
    	}
    	
    	/**
    	 * @return la cantidad de registros actualizados.
    	 */
    	public int cantidad() {
    		return this.cantidad;
    	}

    }

	/**
	 * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
	 */
	@SuppressWarnings("unchecked")
	public void zqlToXql(ZStatement st) {
        ZUpdate zql = (ZUpdate) st;
        this.nombreTabla = zql.getTable();
        this.where = zql.getWhere();
        Hashtable<String, ZExp> hashtable = zql.getSet();
        this.entradas  = new ArrayList<ColumnaAExpresion>(hashtable.size());
        Set<Map.Entry<String, ZExp> > entrySet = hashtable.entrySet();
        for (Map.Entry<String, ZExp> entry : entrySet) {
            String columna = entry.getKey();
            ZExp exp = entry.getValue();
            ColumnaAExpresion entrada = new ColumnaAExpresion();
        	entrada.nombreColumna = columna;
        	entrada.expresion = exp;
            this.entradas.add(entrada);
        }
    }
    
    /**
     * @see servidor.ejecutor.xql.XStatement#execute()
     */
    public Resultado execute() {
        Tabla tabla = FabricaCatalogo.dameInstancia().dameTabla(this.nombreTabla);
        if (tabla == null) {
        	throw new RuntimeException("No table exists with name '" + this.nombreTabla + "'.");
        }
        this.chequeoColumnas(tabla);
        
		Columna[] columnas = tabla.columnas();
        for (ColumnaAExpresion entrada : entradas) {
            for (int i = 0; i < columnas.length; i++) {
                if (entrada.nombreColumna.equals(columnas[i].nombre())) {
                	entrada.numeroColumna = i;
                    break;
                }
            }
        }

        UpdateComando comando = new UpdateComando();
        this.iteradorConsulta.ejecutarParaCadaCoincidencia(tabla, this.where, comando);
        
        Resultado resultado = new Resultado();
        resultado.setMensaje("Successful update of " + comando.cantidad() + " records " +  
                "in table " + this.nombreTabla);
        return resultado;
    }

	/**
	 * Comprueba que las columnas a actualizar existan en la tabla.
	 * @param tabla la tabla contra la que se comprobara la existencia de las columnas a actualizar.
	 */
	private void chequeoColumnas(Tabla tabla) {
		Columna[] columnas = tabla.columnas();
        for (ColumnaAExpresion cae : this.entradas) {
            boolean columnaEncontrada = false;
            for (int j = 0; j < columnas.length; j++) {
                Columna col = columnas[j];
                if (cae.nombreColumna.equals(col.nombre())) {
                    columnaEncontrada = true;
                    break;
                }
            }
            if (!columnaEncontrada) {
            	throw new RuntimeException("Column '" + cae.nombreColumna + "' not found in table '" + tabla.id().nombre() + "'.");
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
