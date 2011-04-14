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
 * <p>提供加解密的一些常用算法
 * @author Wendell
 * @version 1.0
 */
public abstract class CryptoUtilities {
	
	/**
	 * <p>使用DES进行加密
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
		// 当使用其他对称加密算法，如AES、Blowfish等时，用下述代码替换上述的获取Key的方法
		// SecretKey secretKey = new SecretKeySpec(key, algorithm);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher.doFinal(data);
	}
	
	/**
	 * <p>使用DES进行解密
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
		// 当使用其他对称加密算法，如AES、Blowfish等时，用下述代码替换上述的获取Key的方法
		// SecretKey secretKey = new SecretKeySpec(key, algorithm);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(data);
	}
	
	/**
	 * <p>使用MD5进行加密，MD5是一个不可逆的加密算法，对原文的验证通常是将原文重新加密后与密文进行对比
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
