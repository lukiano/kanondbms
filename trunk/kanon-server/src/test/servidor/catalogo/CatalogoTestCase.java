package test.servidor.catalogo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;
import servidor.buffer.FabricaBufferManager;
import servidor.catalog.Catalogo;
import servidor.catalog.Valor;
import servidor.catalog.impl.CatalogoImpl;
import servidor.catalog.tipo.Tipo;
import servidor.tabla.Columna;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.tabla.impl.ColumnaImpl;
import servidor.util.Iterador;

public class CatalogoTestCase extends TestCase {

	public CatalogoTestCase() {
		super();
	}

	public CatalogoTestCase(String name) {
		super(name);
	}

	public void testCatalogo() throws Exception {
		Catalogo catalogo = new CatalogoImpl();
		
		Columna[] columnas = new Columna[] {
				new ColumnaImpl("col1", 30, 1, Tipo.CHAR),
				new ColumnaImpl("col2", 1, 2, Tipo.INTEGER),
				new ColumnaImpl("col3", 10, 3, Tipo.CHAR)
		};
		catalogo.crearTabla("tabla1", columnas);
		Tabla tabla1 = catalogo.dameTabla("tabla1");
		Collection<Valor> valores = Arrays.asList(new Valor[] {
				Valor.nuevoValor(0, columnas[0].campo(), "conten1"),
				Valor.nuevoValor(1, columnas[1].campo(), 34),
				Valor.nuevoValor(2, columnas[2].campo(), "conten3")
		});
		for (int i = 0; i < 12; i++) {
			tabla1.insertarRegistro(tabla1.dameIdRegistroLibre(), valores);
		}
		
		FabricaBufferManager.dameInstancia().guardarBloquesModificados();
		
		Iterador<Registro.ID> iterador = tabla1.registros();
		tabla1.actualizarRegistro(iterador.proximo(), Collections.singleton(Valor.nuevoValor(2, columnas[2].campo(), "nuevoconten3")));
		tabla1.borrarRegistro(iterador.proximo());
		iterador.cerrar();
		
		FabricaBufferManager.dameInstancia().guardarBloquesModificados();
		
		// catalogo.borrarTabla(pepito, tabla1.id().nombre());
		
	//	userManager.borrarUsuario(pepito.nombre());
		
		FabricaBufferManager.dameInstancia().guardarBloquesModificados();
	}
	
}
