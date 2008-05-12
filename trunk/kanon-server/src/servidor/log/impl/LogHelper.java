package servidor.log.impl;

import java.math.BigInteger;

import servidor.catalog.Catalogo;
import servidor.catalog.tipo.ByteHelper;
import servidor.catalog.tipo.Conversor;
import servidor.catalog.tipo.Tipo;
import servidor.indice.hash.Bucket;
import servidor.indice.hash.HashColumna;
import servidor.indice.hash.RegistroIndice;
import servidor.log.LSN;
import servidor.log.Operacion;
import servidor.tabla.Campo;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.tabla.impl.CampoImpl;
import servidor.transaccion.Transaccion;

public class LogHelper {

	private LogHelper() {};
	
	public static byte[] enteroAByteArray(int i) {
		byte[] bs = BigInteger.valueOf(i).toByteArray();
		return ByteHelper.normalizarCadena(bs, Catalogo.LONGITUD_INT);
	}
	
	public static byte[] OperacionAByteArray(Operacion operacion) {
		return enteroAByteArray(operacion.ordinal());
	}

	public static Operacion byteArrayAOperacion(byte[] bs) {
		return Operacion.values()[byteArrayAEntero(bs)];
	}

	public static byte[] LSNAByteArray(LSN lsn) {
		return longAByteArray(lsn.lsn());
	}

	public static LSN byteArrayALSN(byte[] bs) {
		return LSN.nuevoLSN(byteArrayALong(bs));
	}

	public static int byteArrayAEntero(byte[] bs) {
		return new BigInteger(bs).intValue();
	}

	public static byte[] longAByteArray(long l) {
		byte[] bs = BigInteger.valueOf(l).toByteArray();
		return ByteHelper.normalizarCadena(bs, Catalogo.LONGITUD_LONG);
	}

	public static long byteArrayALong(byte[] bs) {
		return new BigInteger(bs).longValue();
	}
	
	public static byte[] IdTransaccionAByteArray(Transaccion.ID idTransaccion) {
		return enteroAByteArray(idTransaccion.numeroTransaccion());
	}

	public static Transaccion.ID byteArrayAIdTransaccion(byte[] bs) {
		return Transaccion.ID.nuevoID(byteArrayAEntero(bs));
	}

	public static byte[] IdRegistroAByteArray(Registro.ID idRegistro) {
		byte[] pagina = IdPaginaAByteArray(idRegistro.propietario());
		byte[] nroRegistro = enteroAByteArray(idRegistro.numeroRegistro());
		return concatenar(pagina, nroRegistro);
	}
	
	public static Registro.ID byteArrayAIdRegistro(byte[] bs) {
		int longitudCampoPagina = LONGITUD_PAGINA;
		byte[] pagina = separar(bs, 0, longitudCampoPagina);
		byte[] nroRegistro = separar(bs, longitudCampoPagina, Catalogo.LONGITUD_INT);
		return Registro.ID.nuevoID(byteArrayAIdPagina(pagina), byteArrayAEntero(nroRegistro));
	}

	public static byte[] IdPaginaAByteArray(Pagina.ID idPagina) {
		byte[] tabla = IdTablaAByteArray(idPagina.propietario());
		byte[] nroPagina = enteroAByteArray(idPagina.numeroPagina());
		return concatenar(tabla, nroPagina);
	}
	
	public static Pagina.ID byteArrayAIdPagina(byte[] bs) {
		byte[] tabla = separar(bs, 0, LONGITUD_TABLA);
		byte[] nroPagina = separar(bs, LONGITUD_TABLA, Catalogo.LONGITUD_INT);
		return Pagina.ID.nuevoID(byteArrayAIdTabla(tabla), byteArrayAEntero(nroPagina));
	}
	
	public static byte[] separar(byte[] bs, int desde, int longitud) {
		byte[] resultado = new byte[longitud];
		System.arraycopy(bs, desde, resultado, 0, resultado.length);
		return resultado;
	}
	
	public static byte[] concatenar(byte[] b1, byte[] b2) {
		byte[] resultado = new byte[b1.length + b2.length];
		System.arraycopy(b1, 0, resultado, 0, b1.length);
		System.arraycopy(b2, 0, resultado, b1.length, b2.length);
		return resultado;
	}
	
	public static byte[] IdTablaAByteArray(Tabla.ID idTabla) {
		byte[] nombreTabla = (byte[]) Conversor.conversorABytes().convertir(CAMPO_NOMBRE_TABLA, 
				Conversor.conversorDeTexto().convertir(CAMPO_NOMBRE_TABLA, idTabla.nombre()));
		byte[] idTablaBs = enteroAByteArray(idTabla.idTabla());
		return concatenar(idTablaBs, nombreTabla);
	}
	
	public static Tabla.ID byteArrayAIdTabla(byte[] bs) {
		byte[] idTablaBS = separar(bs, 0, Catalogo.LONGITUD_INT);
		byte[] nombreTabla = separar(bs, Catalogo.LONGITUD_INT, Catalogo.LONGITUD_CAMPO_NOMBRE_TABLA);
		String nombre = (String) Conversor.conversorATexto().convertir(CAMPO_NOMBRE_TABLA, 
				Conversor.conversorDeBytes().convertir(CAMPO_NOMBRE_TABLA, nombreTabla));
		int idTabla = byteArrayAEntero(idTablaBS);
		return Tabla.ID.nuevoID(nombre, idTabla);
	}
	
	public static byte[] idHashColumnaAByteArray(HashColumna.ID idHashColumna) {
		byte[] tabla = IdTablaAByteArray(idHashColumna.propietario());
		byte[] nroColumna = enteroAByteArray(idHashColumna.columna());
		return concatenar(tabla, nroColumna);
	}
	
	public static HashColumna.ID byteArrayAIdHashColumna(byte[] bs) {
		byte[] tabla = separar(bs, 0, LONGITUD_TABLA);
		byte[] nroColumna = separar(bs, LONGITUD_TABLA, Catalogo.LONGITUD_INT);
		return HashColumna.ID.nuevoID(byteArrayAIdTabla(tabla), byteArrayAEntero(nroColumna));
	}
	
	public static byte[] IdBucketAByteArray(Bucket.ID idBucket) {
		byte[] columna = idHashColumnaAByteArray(idBucket.propietario());
		byte[] nroHash = enteroAByteArray(idBucket.numeroHash());
		byte[] nroBucket = enteroAByteArray(idBucket.numeroBucket());
		return concatenar(columna, concatenar(nroHash, nroBucket));
	}

	public static Bucket.ID byteArrayAIdBucket(byte[] bs) {
		int longitudCampoHashColumna = LONGITUD_HASH_COLUMNA;
		byte[] columna = separar(bs, 0, longitudCampoHashColumna);
		byte[] nroHash = separar(bs, longitudCampoHashColumna, Catalogo.LONGITUD_INT);
		byte[] nroBucket = separar(bs, longitudCampoHashColumna + Catalogo.LONGITUD_INT, Catalogo.LONGITUD_INT);
		return Bucket.ID.nuevoID(byteArrayAIdHashColumna(columna), byteArrayAEntero(nroBucket), byteArrayAEntero(nroHash));
	}

	public static byte[] IdRegistroIndiceAByteArray(RegistroIndice.ID idRegistroIndice) {
		byte[] bucket = IdBucketAByteArray(idRegistroIndice.propietario());
		byte[] nroRegistroIndice = enteroAByteArray(idRegistroIndice.numeroRegistroIndice());
		return concatenar(bucket, nroRegistroIndice);
	} 

	public static RegistroIndice.ID byteArrayAIdRegistroIndice(byte[] bs) {
		int longitudCampoBucket = LONGITUD_BUCKET;
		byte[] bucket = separar(bs, 0, longitudCampoBucket);
		byte[] nroRegistroIndice = separar(bs, longitudCampoBucket, Catalogo.LONGITUD_INT);
		return RegistroIndice.ID.nuevoID(byteArrayAIdBucket(bucket), byteArrayAEntero(nroRegistroIndice));
	}

	private static Campo CAMPO_NOMBRE_TABLA = new CampoImpl(Tipo.CHAR, Catalogo.LONGITUD_CAMPO_NOMBRE_TABLA);
	
	public static int LONGITUD_TABLA = Catalogo.LONGITUD_INT + Catalogo.LONGITUD_CAMPO_NOMBRE_TABLA;
	public static int LONGITUD_PAGINA = LONGITUD_TABLA + Catalogo.LONGITUD_INT;
	public static int LONGITUD_REGISTRO = LONGITUD_PAGINA + Catalogo.LONGITUD_INT;
	public static int LONGITUD_HASH_COLUMNA = LONGITUD_TABLA + Catalogo.LONGITUD_INT;
	public static int LONGITUD_BUCKET = LONGITUD_HASH_COLUMNA + Catalogo.LONGITUD_INT + Catalogo.LONGITUD_INT;
	public static int LONGITUD_REGISTRO_INDICE = LONGITUD_BUCKET + Catalogo.LONGITUD_INT;
	

}
