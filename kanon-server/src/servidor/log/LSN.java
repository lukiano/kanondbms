/**
 * 
 */
package servidor.log;

public final class LSN implements Comparable<LSN> {
	
	public static final LSN LSN_NULO = LSN.nuevoLSN(0);
	public static final LSN PRIMER_LSN = LSN.nuevoLSN(4);
	
    private long lsn;
    
    private LSN(long lsn) {
        this.lsn = lsn;
    }
    
    public static final LSN nuevoLSN(long lsn) {
        return new LSN(lsn);
    }

    public long lsn() {
        return this.lsn;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof LSN)) {
            return false;
        }
        LSN otroID = (LSN) arg0;
        return otroID.lsn == this.lsn;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) this.lsn;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LSN(" + this.lsn + ")";
    }

	public int compareTo(LSN otroLSN) {
		if (this.lsn() < otroLSN.lsn) {
			return -1;
		}
		if (this.lsn() > otroLSN.lsn) {
			return 1;
		}
		return 0;
	}

	public LSN incrementar(long longitud) {
		return nuevoLSN(this.lsn() + longitud);
	}
	
}