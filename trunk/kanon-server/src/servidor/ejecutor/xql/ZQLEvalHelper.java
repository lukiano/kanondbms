package servidor.ejecutor.xql;

import java.sql.SQLException;

import servidor.catalog.tipo.Conversor;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import Zql.ZConstant;
import Zql.ZEval;
import Zql.ZExpression;
import Zql.ZTuple;

/**
 * Metodos de ayuda para la evaluacion de expresiones ZQL.
 */
public class ZQLEvalHelper {
	
	/**
	 * Constructor privado de la clase para evitar instanciamiento.
	 */
	private ZQLEvalHelper() {};

    /**
     * Convierte doubles y floats a integer y luego a un String.
     * @param valor el objeto que sera convertido a texto.
     * @return un string con el texto que representa al valor.
     */
    public static String valorATexto(Object valor) {
        if (valor instanceof Double) {
            Double d = (Double) valor;
            return Long.toString(Math.round(d));
        } else if (valor instanceof Float) {
            Float f = (Float) valor;
            return Integer.toString(Math.round(f));
        } else {
            return valor.toString();
        }
    }
    
    public static Object arreglarResultado(Object resultado) {
		if (resultado instanceof Number) {
			resultado = Integer.valueOf(((Number)resultado).intValue());
		}
		return resultado;
    }
    
	/**
	 * Evalua una expresion y devuelve el resultado como una constante.
	 * Se asume que la expresion no tiene variables.
	 * @param expression la expresion a evaluar.
	 * @return el resultado de la evaluacion.
	 */
	public static ZConstant convertirExpresionAConstante(ZExpression expression) {
		return convertirExpresionAConstante(expression, new ZTuple());
	}

	/**
	 * Evalua una expresion reemplazando las variables que se encuentren en la misma
	 * por los datos de la tupla y devuelve el resultado como una constante.
	 * @param expression la expresion a evaluar.
	 * @param tuple tupla con los valores de las variables.
	 * @return el resultado de la evaluacion.
	 */
	public static ZConstant convertirExpresionAConstante(ZExpression expression, ZTuple tuple) {
		ZEval eval = new ZEval();
		try {
			Object resultado = eval.evalExpValue(tuple, expression);
			String resultadoString = ZQLEvalHelper.valorATexto(resultado);
			if (resultado instanceof Number) {
				return new ZConstant(resultadoString, ZConstant.NUMBER);
			} else if (resultado instanceof String) {
				return new ZConstant(resultadoString, ZConstant.STRING);
			} else {
				throw new RuntimeException("La expresion no esta soportada: " + expression);
			}
		} catch (SQLException e) {
			throw new RuntimeException("La expresion no esta soportada: " + expression, e);
		}
	}

	/**
	 * Construye una tupla con valores sin importancia pero que respetan los campos de las columnas y toman sus nombres de las mismas.
	 * @param columnas un arreglo de columnas del cual se obtendran el nombre y los campos de las mismas.
	 * @return una tupla ZQL.
	 */
	public static ZTuple construirTupla(Columna[] columnas) {
        StringBuilder nombreColumnas = new StringBuilder();
        for (int i = 0; i < columnas.length; i++) {
        	nombreColumnas.append(columnas[i].nombre());
        	if (i < columnas.length - 1) {
        		nombreColumnas.append(',');	
        	}
        }
        ZTuple tuple = new ZTuple(nombreColumnas.toString());
        for (int i = 0; i < columnas.length; i++) {
            Campo campo = columnas[i].campo();
            Object valor = Conversor.conversorDeTexto().convertir(campo, "0");
            tuple.setAtt(columnas[i].nombre(), valor);
        }
		return tuple;
	}


}
