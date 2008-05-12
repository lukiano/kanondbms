package servidor.ejecutor.xql;

import java.util.Vector;

import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;

/**
 * Metodos de ayuda para obtener indices para recorrer que cumplan con una expresion ZQL (tomada de un Where).
 */
class IndiceHelper {
	
	/**
	 * Constructor privado de la clase para evitar instanciamiento.
	 */
	private IndiceHelper() {};

    /**
     * Dada una expresion de un Where, y las columnas de la tabla a recorrer, trata de
     * encontrar si alguna columna se puede utilizar para recorrer sobre los indices de la misma 
     * (buscando un determinado valor) en vez de recorrer toda la tabla. 
     * Esto es muy dependiente de ZQL, tenerlo en cuenta si se cambia a otra implementacion.
     * @param exp la expresion de un Where
     * @param columnas las columnas de la tabla asociada.
     * @return un valor con el contenido a buscar en los indices de la columna que corresponda con la posicion del valor o NULL si no se encontro una expresion apropiada.
     */
    public static Valor dameIndiceSiEsPosible(ZExp exp, Columna[] columnas) {
    	if (exp == null) {
    		return null;
    	}
    	if (exp instanceof ZExpression) {
    		ZExpression expression = (ZExpression) exp;
    		String operador = expression.getOperator();
    		if ("AND".equals(operador)) {
    			// recorremos cada expresion dentro del AND para ver si hay alguna compatible.
    			Vector operandos = expression.getOperands();
    			for (int i = 0; i < operandos.size(); i++) {
        			// hay que buscar recursivamante dentro de los operandos
    				Valor valorInterno = dameIndiceSiEsPosible((ZExp)operandos.get(i), columnas);
    				if (valorInterno != null) {
    					return valorInterno;
    				}
    			}
    			
    		} else if ("=".equals(operador)) {
    			// hay un igual (que es lo que buscamos)
    			Vector operandos = expression.getOperands();
    			if (operandos.get(0) instanceof ZConstant && 
    					esColumna((ZConstant)operandos.get(0))) {
    				if (operandos.get(1) instanceof ZConstant && 
    					esValor((ZConstant)operandos.get(1))) {
    					// hay una columna igualada a un valor
    					Campo campo = dameCampo(((ZConstant)operandos.get(1))); 
    					return Valor.nuevoValor(
    							damePosicionColumna((ZConstant)operandos.get(0), columnas),
    							campo,
    							dameValor(campo, ((ZConstant)operandos.get(1))));
    				} 
    				// la columna esta igualada a otra cosa (como otra columna) => no sirve
    			} 
    			else if (operandos.get(1) instanceof ZConstant && 
    					esColumna((ZConstant)operandos.get(1))) {
    				
    				if (operandos.get(0) instanceof ZConstant && 
        					esValor((ZConstant)operandos.get(0))) {
        					// hay una columna igualada a un valor
    						Campo campo = dameCampo(((ZConstant)operandos.get(1)));
        					return Valor.nuevoValor(
        							damePosicionColumna((ZConstant)operandos.get(1), columnas),
        							campo,
        							dameValor(campo, ((ZConstant)operandos.get(0))));
        			}
    				// la columna esta igualada a otra cosa (como otra columna) => no sirve
    			}
    			// es una igualdad de constantes, no hay columnas, no sirve.
    		}
    		// es otra cosa (Ej OR)
    	}
		return null;
	}

	/**
	 * Tomando la constante y un campo determinado, se transforma la misma en un valor soportado por el motor
	 * @param campo el campo con el tipo de datos que tendra el valor destino.
	 * @param constant una constante de donde se saca el valor.
	 * @return un objeto con un valor soportado por el motor.
	 */
	private static Object dameValor(Campo campo, ZConstant constant) {
		return Conversor.conversorDeTexto().convertir(campo, constant.getValue());
	}

	/**
	 * Devuelve un campo apropiado segun la constante ZQL.
	 * @param constant una constante ZQL.
	 * @return un campo apropiado (ej, tipo de datos numerico para una constante numerica, tipo de datos CHAR para una constante con texto).
	 * @see CampoHelper#dameCampo(ZConstant)
	 */
	private static Campo dameCampo(ZConstant constant) {
		return CampoHelper.dameCampo(constant);
	}

	/**
	 * Teniendo en cuenta un arreglo de columnas y una constante que represente a una, devuelve la posicion de la columna representada.
	 * @param constant una constante que represente a una columna de una tabla.
	 * @param columnas un arreglo con los datos de las columnas de una tabla.
	 * @return un entero con la posicion de la columna referida en la constante.
	 */
	private static int damePosicionColumna(ZConstant constant, Columna[] columnas) {
		for (int i = 0; i < columnas.length; i++) {
			if (columnas[i].nombre().equalsIgnoreCase(constant.getValue().trim())) {
				return i;
			}
		}
		// no deberia llegar nunca aqui
		throw new RuntimeException("Columna invalida: " + constant.getValue());
	}

	/**
	 * @param constant una constante ZQL.
	 * @return true si esa constante representa a una columna de una tabla.
	 */
	private static boolean esColumna(ZConstant constant) {
		return constant.getType() == ZConstant.COLUMNNAME;
	}

	/**
	 * @param constant una constante ZQL.
	 * @return true si esa constante representa a un valor.
	 */
	private static boolean esValor(ZConstant constant) {
		return constant.getType() == ZConstant.NUMBER || constant.getType() == ZConstant.STRING;
	}


}
