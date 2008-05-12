package servidor.parser.impl;
import java.util.Collection;
import java.util.Iterator;

import servidor.tabla.Columna;
import Zql.ZStatement;

/**
 * @author Julian R Berlin
 */
public final class ZCreateTable implements ZStatement {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3603457863680306621L;
	
	private String tableName;
	private Collection<Columna> columnas;
	private Collection<String> PKs;
	
	public ZCreateTable() {
		super();
	}
	
	public ZCreateTable(String tableName, Collection<Columna> columnas, Collection<String> pks) {
		this.tableName = tableName;
		this.columnas = columnas;
		this.PKs = pks;
	}
	
	public Collection<Columna> getColumnas() {
		return columnas;
	}

	public void setColumnas(Collection<Columna> columnas) {
		this.columnas = columnas;
	}

	public Collection<String> getPKs() {
		return PKs;
	}

	public void setPKs(Collection<String> ks) {
		PKs = ks;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@Override
	public String toString(){
		StringBuffer ret = new StringBuffer("CREATE TABLE ");
		ret.append(this.tableName + " (");
		Iterator<Columna> itCols = this.columnas.iterator();
		while (itCols.hasNext()) {
			
			Columna c = itCols.next();
            //esto hay q cambiarlo....ya que solo soporta un tipo (char)
			//ret.append( c.nombre() + " " + c.tipo().dameTipo(String.class)+ "(" + c.LongitudCampo() + ")" );
            ret.append( c.nombre() + " " + c.campo().tipo().toString()+ "(" + c.campo().longitud() + ")" );
			
			/*//TODO:la parte de constraint debe ser analizada con mas detenimiento
			Collection constraint = c.getConstraint();
			
			if (constraint != null) {
				Iterator itContraint = constraint.iterator();
				while (itContraint.hasNext()) {
					ret.append(" " + ((Constraint)itContraint.next()).getType());
				}
			}
			if (itCols.hasNext()) {
				ret.append(", ");
			}*/
		}
		if (this.PKs != null) {
			ret.append(", PRIMARY KEY (");
			Iterator<String> itPks = this.PKs.iterator();
			while (itPks.hasNext()) {
				ret.append(itPks.next());
				if (itPks.hasNext()) {
					ret.append(", ");
				}
			}
		}
		ret.append(")");
		
		return ret.toString();
	}
}
