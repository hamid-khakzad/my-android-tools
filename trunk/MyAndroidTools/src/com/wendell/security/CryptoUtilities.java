package com.wendell.security;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * <p>�ṩ�ӽ��ܵ�һЩ�����㷨
 * @author Wendell
 * @version 1.0
 */
public abstract class CryptoUtilities {
	
	/**
	 * <p>ʹ��DES���м���
	 * @param data
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static byte[] encryptByDES(byte[] data,byte[] key) throws NoSuchAlgorithmException,InvalidKeyException,InvalidKeySpecException,NoSuchPaddingException,BadPaddingException,IllegalBlockSizeException{
		String algorithm = "DES";
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
		DESKeySpec dks = new DESKeySpec(key);
		SecretKey secretKey = keyFactory.generateSecret(dks);
		// ��ʹ�������ԳƼ����㷨����AES��Blowfish��ʱ�������������滻�����Ļ�ȡKey�ķ���
		// SecretKey secretKey = new SecretKeySpec(key, algorithm);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher.doFinal(data);
	}
	
	/**
	 * <p>ʹ��DES���н���
	 * @param data
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static byte[] decryptByDES(byte[] data,byte[] key) throws NoSuchAlgorithmException,InvalidKeyException,InvalidKeySpecException,NoSuchPaddingException,BadPaddingException,IllegalBlockSizeException{
		String algorithm = "DES";
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
		DESKeySpec dks = new DESKeySpec(key);
		SecretKey secretKey = keyFactory.generateSecret(dks);
		// ��ʹ�������ԳƼ����㷨����AES��Blowfish��ʱ�������������滻�����Ļ�ȡKey�ķ���
		// SecretKey secretKey = new SecretKeySpec(key, algorithm);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(data);
	}
	
	/**
	 * <p>ʹ��MD5���м��ܣ�MD5��һ��������ļ����㷨����ԭ�ĵ���֤ͨ���ǽ�ԭ�����¼��ܺ������Ľ��жԱ�
	 * @param data
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] encryptByMD5(byte[] data) throws NoSuchAlgorithmException{
		String algorithm = "MD5";
		MessageDigest md5 = MessageDigest.getInstance(algorithm); 
		md5.update(data);
		return md5.digest();
	}
	
}
