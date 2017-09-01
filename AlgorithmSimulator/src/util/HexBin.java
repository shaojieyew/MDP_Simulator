package util;

import java.math.BigInteger;

public class HexBin {
	public static String hexToBin(String s) {
		  return new BigInteger(s, 16).toString(2);
	}
	public static String BinTohex(String s) {
		  return new BigInteger(s, 2).toString(16);
	}
}
