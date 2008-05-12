package servidor.tabla.impl;

import servidor.buffer.latch.impl.TablaDecoradoraConLatch;
import servidor.indice.hash.impl.TablaDecoradoraConIndices;
import servidor.tabla.Columna;
import servidor.tabla.Tabla;

public class FabricaTablaSistema extends FabricaTablaNormal {
	
	@Override
	public Tabla dameTabla(int id, String nombreTabla, Columna[] columnas) {
		// no lleva lock
        Tabla.ID idTabla = Tabla.ID.nuevoID(nombreTabla, id);
        Tabla tabla = new TablaImpl(idTabla, this.getBufferManager(),
        		this.getRecoveryManager().log(),
        		this.getTransactionManager(), 
        		columnas);
         
    	tabla = new TablaDecoradoraConLatch(tabla, this.getBufferManager().getLatchManager());
    	tabla = new TablaDecoradoraConIndices(tabla);
    	tabla = new TablaDecoradoraConNestedTopAction(tabla, this.getTransactionManager(), this.getRecoveryManager().log());
        return tabla;
	}

}
