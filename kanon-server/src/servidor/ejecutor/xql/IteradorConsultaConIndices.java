package servidor.ejecutor.xql;

import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.indice.hash.FabricaHashManager;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import servidor.tabla.Tabla;
import servidor.tabla.Registro.ID;
import servidor.util.Iterador;
import Zql.ZExp;

/**
 * Implementacion de IteradorConsulta que aprovecha los indices de una tabla si las condiciones
 * del Where lo permiten (ej, que haya una igualdad para indices hash).
 */
public class IteradorConsultaConIndices extends IteradorConsultaRegular {

	/**
	 * @see servidor.ejecutor.xql.IteradorConsultaRegular#dameIterador(servidor.tabla.Tabla, Zql.ZExp, servidor.tabla.Columna[])
	 */
	@Override
	protected Iterador<ID> dameIterador(Tabla tabla, ZExp expresionWhere, Columna[] columnas) {
		Valor indice = IndiceHelper.dameIndiceSiEsPosible(expresionWhere, columnas);
        if (indice != null) {
        	// se encontro un candidato. Antes de usarlo hay que comprobar si los tipos son compatibles
        	Campo campoColumna = columnas[indice.posicion()].campo();
        	if (campoColumna.tipo().equals(indice.campo().tipo())) {
        		// lo son
            	System.out.println("Index found: " + indice.posicion() + " - " + Conversor.conversorATexto().convertir(indice.campo(), indice.contenido()));
            	return FabricaHashManager.dameInstancia()
            		.dameRegistros(tabla.id(), indice.posicion(), indice.contenido());
        	}
        }
    	// no hay ningun indice para usar
    	return super.dameIterador(tabla, expresionWhere, columnas); 
	}

}
