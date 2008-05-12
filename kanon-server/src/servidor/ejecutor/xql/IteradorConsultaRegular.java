package servidor.ejecutor.xql;

import java.sql.SQLException;

import servidor.catalog.tipo.Conversor;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.tabla.Registro.ID;
import servidor.util.Iterador;
import Zql.ZEval;
import Zql.ZExp;
import Zql.ZTuple;

/**
 * Implementacion basica de IteradorConsulta que itera sobre todos los registros de la tabla ejecutando la accion correspondiente
 * para aquellos que pasen la evaluacion del Where.
 */
public class IteradorConsultaRegular implements IteradorConsulta {

	/**
	 * @see servidor.ejecutor.xql.IteradorConsulta#ejecutarParaCadaCoincidencia(servidor.tabla.Tabla, Zql.ZExp, servidor.ejecutor.xql.IteradorConsulta.Comando)
	 */
	public void ejecutarParaCadaCoincidencia(Tabla tabla, ZExp expresionWhere,
			Comando comando) {
		
        ZEval eval = new ZEval();

        Columna[] columnas = tabla.columnas();
        StringBuilder nombreColumnas = new StringBuilder();
        for (int i = 0; i < columnas.length; i++) {
        	nombreColumnas.append(columnas[i].nombre());
        	if (i < columnas.length - 1) {
        		nombreColumnas.append(',');	
        	}
        }
        ZTuple tuple = new ZTuple(nombreColumnas.toString());

        //luego iterar las filas y chequear con where, usando ZEVAL
        Iterador<Registro.ID> iterador = this.dameIterador(tabla, expresionWhere, columnas);
        
        while (iterador.hayProximo()) {
            Registro.ID idRegistro = iterador.proximo();
            // buscar mediante ZExp los registros que entran en la condicion
            // y ejecutar la accion correspondiente.
            Registro registro = tabla.registro(idRegistro);
            try {
                for (int i = 0; i < columnas.length; i++) {
                    Object valor = registro.valor(i);
                    Campo campo = columnas[i].campo();
                    valor = Conversor.conversorATexto().convertir(campo, valor);
                    tuple.setAtt(columnas[i].nombre(), valor);
                }
                if (expresionWhere == null || eval.eval(tuple, expresionWhere)) {
                	comando.ejecutarAccion(tabla, registro, tuple);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
	}

	/**
	 * @param tabla la tabla a iterar.
	 * @param expresionWhere la expresion WHERE de la consulta.
	 * @param columnas las columnas de la tabla.
	 * @return un iterador de la tabla especificada tomando en cuenta optimizaciones de ser posible (para no iterar sobre todos los elementos).
	 */
	protected Iterador<ID> dameIterador(Tabla tabla, ZExp expresionWhere, Columna[] columnas) {
		return tabla.registros();
	}

}
