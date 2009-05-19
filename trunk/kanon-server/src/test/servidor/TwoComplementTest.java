package test.servidor;

import java.math.BigInteger;

import servidor.catalog.tipo.Conversor;
import servidor.catalog.tipo.Tipo;
import servidor.tabla.Campo;
import servidor.tabla.impl.CampoImpl;

public class TwoComplementTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Conversor conversor = Conversor.conversorABytes();
		Campo campo = new CampoImpl(Tipo.INTEGER, 4);
		byte[] bs = (byte[]) conversor.convertir(campo, new Integer(1231236));
		System.out.print(new String(bs));
		System.out.println("as");
		System.out.println(Integer.toHexString(1231236));
		byte[] bs2 = convert(Integer.MAX_VALUE);
		print(bs2);
		System.out.println(-65536 / 256);
		System.out.println(Integer.toBinaryString(123456));
		System.out.println(convert(convert(0)));
		System.out.println(convert(convert(1)));
		System.out.println(convert(convert(-1)));
		System.out.println(convert(convert(123456)));
		System.out.println(convert(convert(-123456)));
		System.out.println(convert(convert(Integer.MAX_VALUE)));
		System.out.println(convert(convert(Integer.MIN_VALUE)));
		System.out.println(convert(convert((long)Integer.MAX_VALUE + 54)));
		
	}

	private static void print(byte[] bs2) {
		for (int i = 0; i < bs2.length; i++) {
			System.out.print(Byte.toString(bs2[i]));
			System.out.print(' ');
		}
		System.out.println();
	}
	
	public static byte[] convert(long n) {
//		boolean neg = n < 0;
//		byte[] bs = new byte[4];
//		bs[0] = neg?(byte) (256 - (n % 256)):(byte) (n % 256);
//		n /= 256;
//		for (int i = 1; i < bs.length; i++) {
//			bs[i] = (byte) (n % 256);
//			n /= 256;
//		}
		byte[] bs = BigInteger.valueOf(n).toByteArray();
		print(bs);
		return bs;
	}

	public static long convert(byte[] bs) {
		return new BigInteger(bs).longValue();
//		int n = 0;
//		for (int i = 0; i < bs.length; i++) {
//			int t = (bs[i] < 0)?(256+bs[i]):bs[i];
//			t = bs[i];
//			for (int p = 0; p < i; p++) {
//				t *= 256;
//			}
//			n += t;
//				
//		}
//		return n;
	}

}
