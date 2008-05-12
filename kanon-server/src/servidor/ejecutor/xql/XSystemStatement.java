package servidor.ejecutor.xql;

import java.io.IOException;

import servidor.conexion.FabricaServidorTcp;
import servidor.ejecutor.Resultado;
import servidor.log.FabricaRecoveryManager;
import servidor.parser.impl.ZSystemStatement;
import Zql.ZStatement;

/**
 * XQL para la ejecucion de las sentencias relacionadas con el manejo del sistema (CHECKPOINT y CRASH).
 * @see servidor.parser.impl.SistemaParser
 */
public class XSystemStatement implements XStatement {
	
	/**
	 * Una cadena con el comando a ejecutar. Este comando se encuentra codificado en un lenguaje interno.
	 */
	private String comando;

	/**
	 * @see servidor.ejecutor.xql.XStatement#execute()
	 */
	public Resultado execute() {
		Resultado resultado = new Resultado();
		if (this.comando.equals("CRASH")) {
			try {
				FabricaServidorTcp.dameInstancia().apagar(true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			resultado.setMensaje("");
		} else if (this.comando.equals("CHECKPOINT")) {
			FabricaRecoveryManager.dameInstancia().checkpoint();
			resultado.setMensaje("Checkpoint realizado correctamente.");
        } else {
            throw new RuntimeException("Comando de sistema no reconocido: " + this.comando);
		}
		return resultado;
	}

	/**
	 * @see servidor.ejecutor.xql.XStatement#necesitaTransaccion()
	 */
	public boolean necesitaTransaccion() {
		return false;
	}

	/**
	 * @see servidor.ejecutor.xql.XStatement#zqlToXql(Zql.ZStatement)
	 */
	public void zqlToXql(ZStatement zql) {
		this.comando = ((ZSystemStatement)zql).getStatament();
	}

}
