package org.rzy.util;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class CryptUtil
{

	final static String DES = "DES";

	final static String KEY = "0102030405060708";

	public final static String decrypt(String data)
	{
		return decrypt(data, KEY);
	}

	public final static String encrypt(String data)
	{
		return encrypt(data, KEY);
	}

	public final static String decrypt(String data, String key)
	{
		return new String(decrypt(hex2byte(data.getBytes()), key.getBytes()));
	}

	public final static String encrypt(String data, String key)
	{
		if (data != null)
		{
			return byte2hex(encrypt(data.getBytes(), key.getBytes()));
		}
		return null;
	}

	private static byte[] encrypt(byte[] src, byte[] key) throws RuntimeException
	{
		try
		{
			SecureRandom sr = new SecureRandom();
			DESKeySpec dks = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
			SecretKey securekey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(DES);
			cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
			return cipher.doFinal(src);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static byte[] decrypt(byte[] src, byte[] key) throws RuntimeException
	{
		try
		{
			SecureRandom sr = new SecureRandom();
			DESKeySpec dks = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
			SecretKey securekey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(DES);
			cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
			return cipher.doFinal(src);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static String byte2hex(byte[] b)
	{
		StringBuilder hs = new StringBuilder();
		String stmp;
		for (int n = 0; b != null && n < b.length; n++)
		{
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1)
				hs.append('0');
			hs.append(stmp);
		}
		return hs.toString().toUpperCase();
	}

	private static byte[] hex2byte(byte[] b)
	{
		if ((b.length % 2) != 0)
			throw new IllegalArgumentException();
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2)
		{
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	public static void main(String[] args)
	{
		String a = encrypt("admin162534");
		System.out.println(a);
		String b = decrypt(a);
		System.out.println(b);
	}
}
