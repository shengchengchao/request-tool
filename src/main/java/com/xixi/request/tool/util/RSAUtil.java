package com.xixi.request.tool.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


/**
 * RSA公钥/私钥<b>非对称加密</b>工具类，RSA加解密通常用于<b>数字签名（Digital Signature)</b>与
 * <b>认证（Authentication）</b>。
 * <ul>
 * <li>私钥加密公钥解密</li>
 * <li>公钥加密私钥解密</li>
 * <li>数字签名和验证</li>
 * </ul>
 */
@Slf4j
public class RSAUtil {
	static final String ALGORITHM = "RSA";
	static final String CHAR_SET = "UTF-8";
	static final String SIGN_ALGORITHMS="SHA1WithRSA";

	// 密钥位数，RSA的公钥和私钥是由KeyPairGenerator生成的，获取KeyPairGenerator的实例后还需要设置其密钥位数。设置密钥位数越高，加密过程越安全，一般使用1024位
	static final int KEY_SIZE = 1024;

	/**
	 * RSA密钥类型
	 * 
	 * @author huajiejun
	 * @version 创建时间：2017年5月25日 下午1:12:52
	 */
	public enum RSAKeyType {
		/** 公钥 */
		PUBLIC,
		/** 私钥 */
		PRIVATE
	}

	/**
	 * 创建公钥/私钥字符串，基于Base64编码
	 * <ul>
	 * <li>Map&lt;RSAKeyType,String&gt; map=RSAUtil.createKey();</li>
	 * <li>获取公钥：map.get(RSAKeyType.PUBLIC);</li>
	 * <li>获取私钥：map.get(RSAKeyType.PRIVATE);</li>
	 * </ul>
	 * 
	 * @return 返回公钥和私钥
	 * @throws NoSuchAlgorithmException
	 * @return: Map<String,String>
	 * @author: huajiejun
	 * @version 创建时间：2017年5月25日 上午11:17:39
	 */
	public static Map<RSAKeyType, String> createKey() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM);
		keyPairGen.initialize(KEY_SIZE);

		KeyPair keyPair = keyPairGen.generateKeyPair(); // 密钥对
		PublicKey publicKey = keyPair.getPublic(); // 公钥
		PrivateKey privateKey = keyPair.getPrivate(); // 私钥

		Map<RSAKeyType, String> keyMap = new HashMap<RSAKeyType, String>();
		keyMap.put(RSAKeyType.PUBLIC, getKeyString(publicKey));
		keyMap.put(RSAKeyType.PRIVATE, getKeyString(privateKey));

		return keyMap;
	}

	/**
	 * 私钥加密，其对应的解密函数为：decrypt(String content, String publicKey)
	 * 
	 * @param content 加密内容
	 * @param privateKey 私钥字符串（Base64格式）
	 * @return 加密字符串（Base64转码过）
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @return: String
	 * @author: huajiejun
	 * @version 创建时间：2017年5月25日 下午1:57:39
	 */
	public static String encrypt(String content, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		return encrypt(content, privateKey, RSAKeyType.PRIVATE);
	}
	
	/**
	 * 公钥解密，其对应的加密函数为：encrypt(String content, String privateKey)
	 * @param content 私钥加密后的密文（Base64格式）
	 * @param publicKey 公钥字符串（Base64格式）
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @return: String
	 * @author: huajiejun
	 * @version 创建时间：2017年5月25日 下午1:57:42
	 */
	public static String decrypt(String content, String publicKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException {
		return decrypt(content,publicKey, RSAKeyType.PUBLIC);
	}
	
	/**
	 * 公钥或私钥加密，其对应的解密函数为：decrypt(String content,String key,RSAKeyType rsaKeyType)
	 * <ul>
	 * <li>私钥加密请用公钥解密</li>
	 * <li>公钥加密请用私钥解密</li>
	 * </ul>
	 * 
	 * @param content 加密的内容
	 * @param key 密钥 （Base64转码过）
	 * @param rsaKeyType 指出密钥是公钥还是私钥
	 * @return 加密字符串（Base64转码过）
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @return: String
	 * @author: huajiejun
	 * @version 创建时间：2017年5月25日 下午2:24:52
	 */
	public static String encrypt(String content,String key,RSAKeyType rsaKeyType) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException{
		if (StringUtils.isEmpty(content)|| StringUtils.isBlank(key)) {
			return null;
		}
		Key k= RSAKeyType.PUBLIC.equals(rsaKeyType)?getPublicKey(key):getPrivateKey(key);
		byte[] data = encryptByKey(content, k);
		byte[] res = null;
		res = Base64.getEncoder().encode(data);
		return new String(res);
	}
	
	/**
	 * 公钥或私钥解密，其对应的解密函数为：encrypt(String content,String key,RSAKeyType rsaKeyType)
	 * <ul>
	 * <li>公钥解密对应私钥密文</li>
	 * <li>私钥解密对应公钥密文</li>
	 * </ul>
	 * 
	 * @param content 加密后的密文（Base64格式）
	 * @param key 密钥 （Base64格式）
	 * @param rsaKeyType 指出密钥是公钥还是私钥
	 * @return 解密内容
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws UnsupportedEncodingException
	 * @return: String
	 * @author: huajiejun
	 * @version 创建时间：2017年5月25日 下午2:24:54
	 */
	public static String decrypt(String content,String key,RSAKeyType rsaKeyType) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		if (StringUtils.isEmpty(content)||StringUtils.isBlank(key)) {
			return null;
		}
		Key k = RSAKeyType.PUBLIC.equals(rsaKeyType)?getPublicKey(key):getPrivateKey(key);
		byte[] data = decryptByKey(content, k);
		String res = new String(data, CHAR_SET);
		return res;
	}

	/**
	 * 私钥生成签名
	 * 
	 * @param data 数据
	 * @param privateKey 私钥
	 * @param sign_algorithms 签名算法，如：MD5withRSA、SHA1WithRSA等，如果此项为空，默认使用SHA1WithRSA
	 * @return 签名串
	 * @throws Exception
	 * @return: String
	 * @author: huajiejun
	 * @version 创建时间：2017年5月25日 下午2:44:15
	 */
	public static String sign(String data, String privateKey, String sign_algorithms) throws Exception {
		if (StringUtils.isEmpty(data)||StringUtils.isBlank(privateKey)) {
			return null;
		}
		PrivateKey key = getPrivateKey(privateKey);
		Signature signature = Signature.getInstance(StringUtils.isBlank(sign_algorithms)?SIGN_ALGORITHMS:sign_algorithms);
		signature.initSign(key);
		signature.update(data.getBytes());
		byte[] sign = signature.sign();
		byte[] encode = Base64.getEncoder().encode(sign);
		return new String(encode);
	}
	
	/**
	 * 公钥验证签名
	 * 
	 * @param data 数据
	 * @param publicKey 公钥
	 * @param sign_algorithms 签名算法，如：MD5withRSA、SHA1WithRSA等，如果此项为空，默认使用SHA1WithRSA
	 * @param signatureResult 签名串
	 * @return 验证成功返回true，验证失败返回false
	 * @throws Exception
	 * @return: boolean
	 * @author: huajiejun
	 * @version 创建时间：2017年5月25日 下午2:49:07
	 */
	public static boolean verify(String data, String publicKey, String sign_algorithms, String signatureResult) throws Exception{
		if(StringUtils.isEmpty(data)||StringUtils.isBlank(publicKey)) {
			return false;
		}
		PublicKey key = getPublicKey(publicKey);
		Signature signature = Signature.getInstance(StringUtils.isBlank(sign_algorithms)?SIGN_ALGORITHMS:sign_algorithms);
		signature.initVerify(key);
		signature.update(data.getBytes());
		byte[] sign = Base64.getDecoder().decode(signatureResult);
		if(sign==null){
			return false;
		}
		return signature.verify(sign);
	}
	
	/**
	 * 还原私钥对象，根据私钥字符串还原私钥对象
	 * 
	 * @param privateKey 私钥字符串
	 * @return 私钥对象
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @return: PrivateKey
	 * @author: huajiejun
	 * @version 创建时间：2017年5月25日 下午1:31:39
	 */
	public static PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (StringUtils.isBlank(privateKey)) {
			return null;
		}
		byte[] keyBytes;
		keyBytes = Base64.getDecoder().decode(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		PrivateKey privatekey = keyFactory.generatePrivate(keySpec);
		return privatekey;
	}

	/**
	 * 还原公钥对象，根据公钥字符串还原公钥对象
	 * 
	 * @param publicKey 公钥字符串
	 * @return 公钥对象
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @return: PublicKey
	 * @author: huajiejun
	 * @version 创建时间：2017年5月25日 下午1:31:37
	 */
	public static PublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (StringUtils.isBlank(publicKey)) {
			return null;
		}
		byte[] keyBytes;
		keyBytes = Base64.getDecoder().decode(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		PublicKey publickey = keyFactory.generatePublic(keySpec);
		return publickey;
	}

	protected static String getKeyString(Key key) {
		byte[] keyBytes = key.getEncoded();
		byte[] encode = Base64.getEncoder().encode(keyBytes);
		return new String(encode);
	}

	protected static byte[] encryptByKey(String content, Key pk)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException,
			IllegalBlockSizeException, BadPaddingException {
		Cipher ch = Cipher.getInstance(ALGORITHM);
		ch.init(Cipher.ENCRYPT_MODE, pk);
		byte[] contentBytes = content.getBytes(CHAR_SET);
		// 加密时超过117字节就报错。为此采用分段加密的办法来加密
		byte[] enBytes = null;
		for (int i = 0; i < contentBytes.length; i += 64) {
			// 注意要使用2的倍数，否则会出现加密后的内容再解密时为乱码
			byte[] doFinal = ch.doFinal(ArrayUtils.subarray(contentBytes, i, i + 64));
			enBytes = ArrayUtils.addAll(enBytes, doFinal);
		}
		return enBytes;
	}

	protected static byte[] decryptByKey(String content, Key pk) {
		try{
			InputStream ins = null;
			ByteArrayOutputStream writer = null;
			try{
				Cipher ch = Cipher.getInstance(ALGORITHM);
				ch.init(Cipher.DECRYPT_MODE, pk);
				ins = new ByteArrayInputStream(Base64.getDecoder().decode(content));
				writer = new ByteArrayOutputStream();
				// rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
				byte[] buf = new byte[128];
				int bufl;
				while ((bufl = ins.read(buf)) != -1) {
					byte[] block = null;
					if (buf.length == bufl) {
						block = buf;
					} else {
						block = new byte[bufl];
						for (int i = 0; i < bufl; i++) {
							block[i] = buf[i];
						}
					}
					writer.write(ch.doFinal(block));
				}
				return writer.toByteArray();
			}finally {
				if(null!=ins){
					ins.close();
				}
				if(null!=writer){
					writer.close();
				}
			}
		}catch(Exception e){
			log.info("报错", e);
		}
		
		return null;
	}
}
